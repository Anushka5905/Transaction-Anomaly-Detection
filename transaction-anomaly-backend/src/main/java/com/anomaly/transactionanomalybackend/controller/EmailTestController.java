package com.anomaly.transactionanomalybackend.controller;

import com.anomaly.transactionanomalybackend.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@PreAuthorize("hasRole('ADMIN')")
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTestEmail() {

        try {
            emailService.sendTestEmail();

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Test email sent successfully."
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message",
                            "Failed to send test email.",
                            "error",
                            e.getMessage() == null
                                    ? "Unknown email error"
                                    : e.getMessage()
                    )
            );
        }
    }
}