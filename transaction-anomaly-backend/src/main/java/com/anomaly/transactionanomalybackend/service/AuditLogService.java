package com.anomaly.transactionanomalybackend.service;

import com.anomaly.transactionanomalybackend.model.AuditLog;
import com.anomaly.transactionanomalybackend.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(
            String userEmail,
            String action,
            String description,
            HttpServletRequest request) {

        String ipAddress = getClientIpAddress(request);

        AuditLog auditLog = new AuditLog(
                userEmail,
                action,
                description,
                ipAddress,
                LocalDateTime.now()
        );

        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository
                .findAllByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsByUser(String userEmail) {
        return auditLogRepository
                .findByUserEmailOrderByTimestampDesc(userEmail);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository
                .findByActionOrderByTimestampDesc(action);
    }

    private String getClientIpAddress(
            HttpServletRequest request) {

        if (request == null) {
            return "UNKNOWN";
        }

        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null
                && !forwardedFor.isBlank()) {

            return forwardedFor.split(",")[0].trim();
        }

        String realIp =
                request.getHeader("X-Real-IP");

        if (realIp != null
                && !realIp.isBlank()) {

            return realIp;
        }

        return request.getRemoteAddr();
    }
}