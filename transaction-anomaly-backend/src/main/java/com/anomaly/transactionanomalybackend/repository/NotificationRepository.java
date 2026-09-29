package com.anomaly.transactionanomalybackend.repository;

import com.anomaly.transactionanomalybackend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<Notification> findByUserEmailAndReadStatusOrderByCreatedAtDesc(
            String userEmail,
            boolean readStatus
    );

    long countByUserEmailAndReadStatus(
            String userEmail,
            boolean readStatus
    );
}