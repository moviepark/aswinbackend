package com.vaultflow.orchestrator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Role is required")
    @Column(nullable = false)
    private String role;

    private String userId;

    private String organizationId;

    private LocalDateTime createdAt;

    public User() {
    }

    public User(String name, String email, String password, String role, String userId, String organizationId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.userId = userId;
        this.organizationId = organizationId;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.role == null || this.role.isBlank()) {
            this.role = "USER";
        }
        this.role = this.role.toUpperCase();
        if (this.userId == null || this.userId.isBlank()) {
            this.userId = "USR-FIN-" + System.currentTimeMillis();
        }
        if (this.organizationId == null || this.organizationId.isBlank()) {
            this.organizationId = "CORP-VAULT-01";
        }
    }

    @PreUpdate
    public void onUpdate() {
        if (this.role != null) {
            this.role = this.role.toUpperCase();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
