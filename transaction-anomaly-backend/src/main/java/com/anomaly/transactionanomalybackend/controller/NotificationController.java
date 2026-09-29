package com.anomaly.transactionanomalybackend.controller;

import com.anomaly.transactionanomalybackend.model.Notification;
import com.anomaly.transactionanomalybackend.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Get all notifications for logged-in user
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                notificationService.getNotifications(userEmail)
        );
    }

    // Get only unread notifications
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            Authentication authentication) {

        String userEmail = authentication.getName();

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userEmail)
        );
    }

    // Get unread notification count
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            Authentication authentication) {

        String userEmail = authentication.getName();

        long count = notificationService.getUnreadCount(userEmail);

        return ResponseEntity.ok(
                Map.of("count", count)
        );
    }

    // Mark one notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();

        boolean updated =
                notificationService.markAsRead(id, userEmail);

        if (!updated) {
            return ResponseEntity.status(404)
                    .body(Map.of(
                            "message",
                            "Notification not found or access denied."
                    ));
        }

        return ResponseEntity.ok(
                Map.of("message", "Notification marked as read.")
        );
    }

    // Mark all notifications as read
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            Authentication authentication) {

        String userEmail = authentication.getName();

        int updatedCount =
                notificationService.markAllAsRead(userEmail);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Notifications marked as read.",
                        "updatedCount", updatedCount
                )
        );
    }
}