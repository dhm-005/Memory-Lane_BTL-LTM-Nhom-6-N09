package com.cardgame.server.service;


import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.network.ClientHandler;
import com.cardgame.server.repository.UserRepository;
import com.cardgame.server.model.User;
import com.cardgame.server.util.Json;
import com.cardgame.server.util.PasswordHasher;
import java.sql.SQLIntegrityConstraintViolationException;
import com.cardgame.server.network.OnlineUsers;
import static com.cardgame.server.MainServer.error;
public final class AuthService {
    private AuthService() {
    }
    public static void register(ClientHandler client, String username, String password) throws Exception {
        if (client.userId > 0 || client.room != null) {
            client.send(error("Kết nối này đã đăng nhập. Vui lòng đăng xuất trước."));
            return;
        }
        if (username.length() < 3 || username.length() > 24 || password.length() < 4) {
            client.send(error("Tên đăng nhập cần 3-24 ký tự, mật khẩu ít nhất 4 ký tự."));
            return;
        }
        try {
            UserRepository.create(username, PasswordHasher.hash(password));
            login(client, username, password);
        } catch (SQLIntegrityConstraintViolationException e) {
            client.send(error("Tài khoản đã tồn tại."));
        }
    }
    public static void login(ClientHandler client, String username, String password) throws Exception {
        if (client.userId > 0 || client.room != null) {
            client.send(error("Kết nối này đã đăng nhập. Vui lòng đăng xuất trước."));
            return;
        }
        User account = UserRepository.findByUsername(username);
        if (account == null) {
            client.send(error("Tên đăng nhập hoặc mật khẩu không chính xác."));
            return;
        }
        if (!PasswordHasher.verify(password, account.getPasswordHash())) {
            client.send(error("Tên đăng nhập hoặc mật khẩu không chính xác."));
            return;
        }
        if (PasswordHasher.needsUpgrade(account.getPasswordHash())) {
            UserRepository.updatePassword(account.getId(), PasswordHasher.hash(password));
        }
        ClientHandler previous = OnlineUsers.all().put(account.getId(), client);
        if (previous != null && previous != client) {
            ObjectNode replaced = Json.object("SESSION_REPLACED");
            replaced.put("message", "Tài khoản đã được đăng nhập trên thiết bị khác.");
            previous.send(replaced);
            try {
                previous.socket.close();
            } catch (Exception ignored) {
            }
        }
        client.userId = account.getId();
        client.username = account.getUsername();
        client.soloProgressLevel = 0;
        client.soloRunStartedAtNanos = 0;
        client.soloLevelStartedAtNanos = 0;
        int currentCoins = UserRepository.findCoins(account.getId());
        ObjectNode response = Json.object("LOGIN_OK");
        response.put("userId", account.getId());
        response.put("username", account.getUsername());
        response.put("coins", currentCoins);
        response.put("points", account.getRankingPoints());
        client.send(response);
    }
    public static void changePassword(ClientHandler client, String oldPassword, String newPassword) throws Exception {
        if (client.userId <= 0) {
            client.send(error("Vui lòng đăng nhập trước."));
            return;
        }
        if (newPassword == null || newPassword.length() < 4) {
            client.send(error("Mật khẩu mới phải có ít nhất 4 ký tự."));
            return;
        }
        String currentHash = UserRepository.findPasswordHash(client.userId);
        if (currentHash == null || !PasswordHasher.verify(oldPassword, currentHash)) {
            client.send(error("Mật khẩu hiện tại không chính xác."));
            return;
        }
        UserRepository.updatePassword(client.userId, PasswordHasher.hash(newPassword));
        client.send(Json.object("PASSWORD_CHANGED"));
    }
    public static void logout(ClientHandler client) {
        if (client.room != null) client.room.leaveWaiting(client);
        if (client.userId > 0) OnlineUsers.all().remove(client.userId, client);
        client.userId = -1;
        client.username = "";
        client.soloProgressLevel = 0;
        client.soloRunStartedAtNanos = 0;
        client.soloLevelStartedAtNanos = 0;
        client.send(Json.object("LOGOUT_OK"));
    }

}
