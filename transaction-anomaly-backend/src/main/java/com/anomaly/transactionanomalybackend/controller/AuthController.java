package com.anomaly.transactionanomalybackend.controller;

import com.anomaly.transactionanomalybackend.dto.LoginRequest;
import com.anomaly.transactionanomalybackend.dto.LoginResponse;
import com.anomaly.transactionanomalybackend.model.User;
import com.anomaly.transactionanomalybackend.repository.UserRepository;
import com.anomaly.transactionanomalybackend.security.JwtService;
import com.anomaly.transactionanomalybackend.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditLogService auditLogService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {

            auditLogService.log(
                    request.getEmail(),
                    "LOGIN_FAILED",
                    "Login failed: user not found",
                    httpRequest
            );

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Invalid email or password."
                    ));
        }

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {

            auditLogService.log(
                    user.getEmail(),
                    "LOGIN_FAILED",
                    "Login failed: incorrect password",
                    httpRequest
            );

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Invalid email or password."
                    ));
        }

        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        user.getRole()
                );

        auditLogService.log(
                user.getEmail(),
                "LOGIN",
                "User logged in successfully",
                httpRequest
        );

        return ResponseEntity.ok(
                new LoginResponse(
                        token,
                        user.getName(),
                        user.getEmail(),
                        user.getRole()
                )
        );
    }
}