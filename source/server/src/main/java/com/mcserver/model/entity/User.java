package com.mcserver.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private long id;
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
    
}
