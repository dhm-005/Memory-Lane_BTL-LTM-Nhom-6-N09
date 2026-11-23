package com.mcserver.service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcserver.model.User;
import com.mcserver.network.ClientSession;
import com.mcserver.network.SessionRegistry;
import com.mcserver.repository.UserRepository;
import com.mcserver.util.AuthUtils;
import com.mcserver.util.JsonTemplates;

public final class AuthService {
    private AuthService() {}

    public static ObjectNode register(String requestId, ClientSession session, SessionRegistry registry, String username, String password) {
        if (username == null || username.length() < 3 || username.length() > 24 || password == null || password.length() < 4) {
            return JsonTemplates.error("REGISTER", requestId, "INVALID_INPUT", "Tên đăng nhập cần 3-24 ký tự, mật khẩu ít nhất 4 ký tự.");
        }
        try {
            String hashed = AuthUtils.toHashed(password);
            UserRepository.create(username.trim(), hashed);
            return login(requestId, session, registry, username.trim(), password);
        } catch (SQLIntegrityConstraintViolationException e) {
            return JsonTemplates.error("REGISTER", requestId, "USER_EXISTS", "Tài khoản đã tồn tại.");
        } catch (SQLException e) {
            return JsonTemplates.error("REGISTER", requestId, "DB_ERROR", "Lỗi đăng ký tài khoản: " + e.getMessage());
        }
    }

    public static ObjectNode login(String requestId, ClientSession session, SessionRegistry registry, String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return JsonTemplates.error("LOGIN", requestId, "INVALID_INPUT", "Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
        }
        try {
            User account = UserRepository.findByUsername(username.trim());
            if (account == null) {
                return JsonTemplates.error("LOGIN", requestId, "USER_NOT_FOUND", "Tài khoản chưa tồn tại. Vui lòng đăng ký.");
            }
            if (!AuthUtils.verifyPassword(password, account.getPasswordHash())) {
                return JsonTemplates.error("LOGIN", requestId, "WRONG_PASSWORD", "Mật khẩu không chính xác.");
            }

            // Đá phiên đăng nhập cũ nếu tài khoản đang online ở nơi khác
            registry.sendToUser(account.getId(), JsonTemplates.push("FORCE_LOGOUT", "Tài khoản vừa đăng nhập ở nơi khác."));

            session.authenticate(account.getId(), account.getUsername());
            registry.registerUser(account.getId(), session);
            UserRepository.updateLastLogin(account.getId());

            // passwordHash tự động bị ẩn khi serialize nhờ @JsonIgnore trong User
            return JsonTemplates.authSuccess("LOGIN_OK", requestId, account);
        } catch (SQLException e) {
            return JsonTemplates.error("LOGIN", requestId, "DB_ERROR", "Lỗi đăng nhập: " + e.getMessage());
        }
    }

    public static ObjectNode changePassword(String requestId, ClientSession session, String oldPassword, String newPassword) {
        if (!session.isAuthenticated()) {
            return JsonTemplates.error("CHANGE_PASSWORD", requestId, "UNAUTHENTICATED", "Vui lòng đăng nhập trước.");
        }
        if (newPassword == null || newPassword.length() < 4) {
            return JsonTemplates.error("CHANGE_PASSWORD", requestId, "INVALID_INPUT", "Mật khẩu mới phải có ít nhất 4 ký tự.");
        }
        try {
            User account = UserRepository.findById(session.getUserId());
            if (account == null || !AuthUtils.verifyPassword(oldPassword, account.getPasswordHash())) {
                return JsonTemplates.error("CHANGE_PASSWORD", requestId, "WRONG_PASSWORD", "Mật khẩu hiện tại không chính xác.");
            }
            UserRepository.updatePassword(session.getUserId(), AuthUtils.toHashed(newPassword));
            return JsonTemplates.ok("PASSWORD_CHANGED", requestId, null);
        } catch (SQLException e) {
            return JsonTemplates.error("CHANGE_PASSWORD", requestId, "DB_ERROR", "Lỗi đổi mật khẩu: " + e.getMessage());
        }
    }
}