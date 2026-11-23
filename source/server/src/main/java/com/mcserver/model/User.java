package com.mcserver.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String displayName;
    private String username;
    private String passwordHash;
    
    private String email;
    private String phone;
    private LocalDate dob;
    
    
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    private int totalScore;
    private int playerRank;
    
    public enum Role {PLAYER, ADMIN}
    private Role role;
    
    private int coins;
    
}
