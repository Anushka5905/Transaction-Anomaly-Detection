package com.anomaly.transactionanomalybackend.controller;

import com.anomaly.transactionanomalybackend.model.Anomaly;
import com.anomaly.transactionanomalybackend.repository.AnomalyRepository;
import com.anomaly.transactionanomalybackend.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/anomalies")
public class AnomalyReviewController {

    private final AnomalyRepository anomalyRepository;
    private final AuditLogService auditLogService;

    public AnomalyReviewController(
            AnomalyRepository anomalyRepository,
            AuditLogService auditLogService) {

        this.anomalyRepository = anomalyRepository;
        this.auditLogService = auditLogService;
    }

    // ---------------------------------------------------------
    // UPDATE ANOMALY REVIEW
    // ---------------------------------------------------------

    @PutMapping("/{id}/review")
    public ResponseEntity<?> reviewAnomaly(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        // Find anomaly
        Anomaly anomaly = anomalyRepository.findById(id).orElse(null);

        if (anomaly == null) {
            return ResponseEntity.notFound().build();
        }

        // Get requested review status
        String reviewStatus = request.get("reviewStatus");

        if (reviewStatus == null || reviewStatus.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Review status is required.")
            );
        }

        reviewStatus = reviewStatus.trim().toUpperCase();

        // Only these statuses are allowed
        if (!reviewStatus.equals("OPEN")
                && !reviewStatus.equals("INVESTIGATING")
                && !reviewStatus.equals("CONFIRMED")
                && !reviewStatus.equals("FALSE_POSITIVE")) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Invalid review status. Allowed values: OPEN, INVESTIGATING, CONFIRMED, FALSE_POSITIVE."
                    )
            );
        }

        // Get notes
        String reviewNotes = request.get("reviewNotes");

        if (reviewNotes == null) {
            reviewNotes = "";
        }

        reviewNotes = reviewNotes.trim();

        // Prevent extremely large notes
        if (reviewNotes.length() > 2000) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Review notes cannot exceed 2000 characters."
                    )
            );
        }

        // Current logged-in user
        String userEmail = authentication != null
                ? authentication.getName()
                : "UNKNOWN";

        // Update anomaly
        anomaly.setReviewStatus(reviewStatus);
        anomaly.setReviewNotes(reviewNotes);

        // If anomaly is OPEN, remove previous reviewer information
        if (reviewStatus.equals("OPEN")) {

            anomaly.setReviewedBy(null);
            anomaly.setReviewedAt(null);

        } else {

            anomaly.setReviewedBy(userEmail);
            anomaly.setReviewedAt(LocalDateTime.now());
        }

        Anomaly savedAnomaly = anomalyRepository.save(anomaly);

        // Audit log
        auditLogService.log(
                userEmail,
                "REVIEW_ANOMALY",
                "Updated anomaly #" + id
                        + " review status to " + reviewStatus,
                httpRequest
        );

        return ResponseEntity.ok(savedAnomaly);
    }

    // ---------------------------------------------------------
    // GET SINGLE ANOMALY
    // ---------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<?> getAnomaly(@PathVariable Long id) {

        return anomalyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}