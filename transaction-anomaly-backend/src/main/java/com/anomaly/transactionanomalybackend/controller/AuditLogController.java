package com.anomaly.transactionanomalybackend.controller;

import com.anomaly.transactionanomalybackend.model.AuditLog;
import com.anomaly.transactionanomalybackend.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(
                auditLogService.getAllLogs()
        );
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<AuditLog>> getLogsByUser(
            @PathVariable String email) {

        return ResponseEntity.ok(
                auditLogService.getLogsByUser(email)
        );
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getLogsByAction(
            @PathVariable String action) {

        return ResponseEntity.ok(
                auditLogService.getLogsByAction(action)
        );
    }
}