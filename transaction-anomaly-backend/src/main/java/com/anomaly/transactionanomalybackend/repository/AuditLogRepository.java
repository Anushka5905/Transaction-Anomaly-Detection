package com.anomaly.transactionanomalybackend.repository;

import com.anomaly.transactionanomalybackend.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByTimestampDesc();

    List<AuditLog> findByUserEmailOrderByTimestampDesc(
            String userEmail
    );

    List<AuditLog> findByActionOrderByTimestampDesc(
            String action
    );
}