package com.vaultflow.orchestrator.service;

import com.vaultflow.orchestrator.dto.AuthDTO;
import com.vaultflow.orchestrator.model.User;
import com.vaultflow.orchestrator.repository.UserRepository;
import com.vaultflow.orchestrator.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
    }

    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        validateRegister(request);

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(resolveRole(request.getRole()));
        user.setUserId(request.getUserId());
        user.setOrganizationId(request.getOrganizationId());

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser);
        auditService.logAction("USER_REGISTERED:" + savedUser.getEmail());

        return toAuthResponse("User registered successfully", savedUser, token);
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String email = request.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        String token = jwtUtil.generateToken(user);
        auditService.logAction("USER_LOGIN:" + user.getEmail());

        return toAuthResponse("Login successful", user, token);
    }

    public void logout() {
        auditService.logAction("USER_LOGOUT");
    }

    private AuthDTO.AuthResponse toAuthResponse(String message, User user, String token) {
        return new AuthDTO.AuthResponse(
                message,
                token,
                "Bearer",
                user.getId(),
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getOrganizationId()
        );
    }

    private void validateRegister(AuthDTO.RegisterRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    private String resolveRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        String normalized = role.trim().toUpperCase();
        return switch (normalized) {
            case "ADMIN", "TREASURER", "AUDITOR", "USER" -> normalized;
            default -> throw new IllegalArgumentException("Invalid role. Allowed roles: ADMIN, TREASURER, AUDITOR, USER");
        };
    }
}
