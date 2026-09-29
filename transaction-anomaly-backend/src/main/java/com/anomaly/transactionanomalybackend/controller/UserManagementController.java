package com.anomaly.transactionanomalybackend.controller;

import com.anomaly.transactionanomalybackend.dto.UserResponse;
import com.anomaly.transactionanomalybackend.model.User;
import com.anomaly.transactionanomalybackend.repository.UserRepository;
import com.anomaly.transactionanomalybackend.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public UserManagementController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    // =========================================================
    // GET ALL USERS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(UserResponse::fromUser)
                .toList();

        return ResponseEntity.ok(users);
    }

    // =========================================================
    // GET USER BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id) {

        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(
                        UserResponse.fromUser(user)
                ))
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }

    // =========================================================
    // UPDATE USER
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser,
            Authentication authentication,
            HttpServletRequest request) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // -----------------------------------------------------
        // Validate name
        // -----------------------------------------------------

        if (updatedUser.getName() != null) {

            String name = updatedUser.getName().trim();

            if (name.isBlank() || name.length() < 2) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "message",
                                "Name must contain at least 2 characters."
                        )
                );
            }

            user.setName(name);
        }

        // -----------------------------------------------------
        // Validate email
        // -----------------------------------------------------

        if (updatedUser.getEmail() != null) {

            String email = updatedUser.getEmail()
                    .trim()
                    .toLowerCase();

            if (email.isBlank()
                    || !EMAIL_PATTERN.matcher(email).matches()) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "message",
                                "Please provide a valid email address."
                        )
                );
            }

            // Check whether another user already owns this email
            User existingUser =
                    userRepository.findByEmail(email).orElse(null);

            if (existingUser != null
                    && !existingUser.getId().equals(user.getId())) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "message",
                                "Another user already exists with this email."
                        )
                );
            }

            user.setEmail(email);
        }

        // -----------------------------------------------------
        // Validate role
        // -----------------------------------------------------

        if (updatedUser.getRole() != null) {

            String role = updatedUser.getRole()
                    .trim()
                    .toUpperCase();

            if (!role.equals("ADMIN")
                    && !role.equals("ANALYST")) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "message",
                                "Invalid role. Allowed roles are ADMIN and ANALYST."
                        )
                );
            }

            // Prevent an admin from removing the last admin
            if (user.getRole().equals("ADMIN")
                    && role.equals("ANALYST")) {

                long adminCount = userRepository.findAll()
                        .stream()
                        .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                        .count();

                if (adminCount <= 1) {

                    return ResponseEntity.badRequest().body(
                            Map.of(
                                    "message",
                                    "The last administrator cannot be changed to ANALYST."
                            )
                    );
                }
            }

            user.setRole(role);
        }

        // -----------------------------------------------------
        // Validate password
        // -----------------------------------------------------

        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {

            String password = updatedUser.getPassword();

            if (password.length() < 8) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "message",
                                "Password must contain at least 8 characters."
                        )
                );
            }

            user.setPassword(
                    passwordEncoder.encode(password)
            );
        }

        // -----------------------------------------------------
        // Save user
        // -----------------------------------------------------

        User savedUser = userRepository.save(user);

        // -----------------------------------------------------
        // Audit log
        // -----------------------------------------------------

        String adminEmail = authentication.getName();

        auditLogService.log(
                adminEmail,
                "UPDATE_USER",
                "Updated user account: " + savedUser.getEmail(),
                request
        );

        return ResponseEntity.ok(
                UserResponse.fromUser(savedUser)
        );
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {

            return ResponseEntity.notFound().build();
        }

        String loggedInEmail = authentication.getName();

        // -----------------------------------------------------
        // Prevent admin from deleting their own account
        // -----------------------------------------------------

        if (user.getEmail().equalsIgnoreCase(loggedInEmail)) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "You cannot delete your own account."
                    )
            );
        }

        // -----------------------------------------------------
        // Prevent deleting the last administrator
        // -----------------------------------------------------

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {

            long adminCount = userRepository.findAll()
                    .stream()
                    .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                    .count();

            if (adminCount <= 1) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "message",
                                "The last administrator cannot be deleted."
                        )
                );
            }
        }

        // -----------------------------------------------------
        // Delete user
        // -----------------------------------------------------

        String deletedEmail = user.getEmail();

        userRepository.deleteById(id);

        // -----------------------------------------------------
        // Audit log
        // -----------------------------------------------------

        auditLogService.log(
                loggedInEmail,
                "DELETE_USER",
                "Deleted user account: " + deletedEmail,
                request
        );

        return ResponseEntity.noContent().build();
    }
}