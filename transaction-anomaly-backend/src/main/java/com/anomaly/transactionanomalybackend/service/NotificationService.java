package com.anomaly.transactionanomalybackend.service;

import com.anomaly.transactionanomalybackend.model.Notification;
import com.anomaly.transactionanomalybackend.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(
            String userEmail,
            String message,
            String type,
            Long transactionId,
            Long anomalyId
    ) {
        Notification notification = new Notification(
                userEmail,
                message,
                type,
                transactionId,
                anomalyId
        );

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(String userEmail) {
        return notificationRepository
                .findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public List<Notification> getUnreadNotifications(String userEmail) {
        return notificationRepository
                .findByUserEmailAndReadStatusOrderByCreatedAtDesc(
                        userEmail,
                        false
                );
    }

    public long getUnreadCount(String userEmail) {
        return notificationRepository
                .countByUserEmailAndReadStatus(userEmail, false);
    }

    public boolean markAsRead(Long notificationId, String userEmail) {

        return notificationRepository.findById(notificationId)
                .map(notification -> {

                    // Prevent one user from modifying another user's notification
                    if (!notification.getUserEmail().equalsIgnoreCase(userEmail)) {
                        return false;
                    }

                    notification.setReadStatus(true);
                    notificationRepository.save(notification);

                    return true;
                })
                .orElse(false);
    }

    public int markAllAsRead(String userEmail) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserEmailAndReadStatusOrderByCreatedAtDesc(
                                userEmail,
                                false
                        );

        for (Notification notification : notifications) {
            notification.setReadStatus(true);
        }

        notificationRepository.saveAll(notifications);

        return notifications.size();
    }
}