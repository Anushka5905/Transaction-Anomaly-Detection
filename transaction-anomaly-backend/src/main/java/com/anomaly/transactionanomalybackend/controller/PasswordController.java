package com.anomaly.transactionanomalybackend.controller;

import com.anomaly.transactionanomalybackend.dto.ChangePasswordRequest;
import com.anomaly.transactionanomalybackend.model.User;
import com.anomaly.transactionanomalybackend.repository.UserRepository;
import com.anomaly.transactionanomalybackend.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class PasswordController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public PasswordController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        // -----------------------------------------------------
        // Authentication check
        // -----------------------------------------------------

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Authentication is required."
                            )
                    );
        }

        // -----------------------------------------------------
        // Get currently logged-in user's email
        // -----------------------------------------------------

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    "User account not found."
                            )
                    );
        }

        // -----------------------------------------------------
        // Validate new password length
        // -----------------------------------------------------

        if (request.getNewPassword() == null
                || request.getNewPassword().length() < 8) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "New password must contain at least 8 characters."
                            )
                    );
        }

        // -----------------------------------------------------
        // Verify current password
        // -----------------------------------------------------

        boolean currentPasswordCorrect =
                passwordEncoder.matches(
                        request.getCurrentPassword(),
                        user.getPassword()
                );

        if (!currentPasswordCorrect) {

            auditLogService.log(
                    email,
                    "PASSWORD_CHANGE_FAILED",
                    "Password change failed: incorrect current password.",
                    httpRequest
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message",
                                    "Current password is incorrect."
                            )
                    );
        }

        // -----------------------------------------------------
        // Prevent password reuse
        // -----------------------------------------------------

        boolean samePassword =
                passwordEncoder.matches(
                        request.getNewPassword(),
                        user.getPassword()
                );

        if (samePassword) {

            auditLogService.log(
                    email,
                    "PASSWORD_CHANGE_FAILED",
                    "Password change failed: new password is same as current password.",
                    httpRequest
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message",
                                    "New password must be different from the current password."
                            )
                    );
        }

        // -----------------------------------------------------
        // Hash new password using BCrypt
        // -----------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        // -----------------------------------------------------
        // Audit successful password change
        // -----------------------------------------------------

        auditLogService.log(
                email,
                "PASSWORD_CHANGED",
                "User changed their account password successfully.",
                httpRequest
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password changed successfully."
                )
        );
    }
}