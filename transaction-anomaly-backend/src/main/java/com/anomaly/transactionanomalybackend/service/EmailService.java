package com.anomaly.transactionanomalybackend.service;

import com.anomaly.transactionanomalybackend.model.Anomaly;
import com.anomaly.transactionanomalybackend.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${alert.email.to:}")
    private String alertEmailTo;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a test email to verify SMTP configuration.
     */
    public void sendTestEmail() {

        if (alertEmailTo == null || alertEmailTo.isBlank()) {
            throw new IllegalStateException(
                    "ALERT_EMAIL_TO is not configured."
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(alertEmailTo);

        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }

        message.setSubject(
                "Transaction Anomaly System - Email Test"
        );

        message.setText(
                "Hello,\n\n" +
                "This is a test email from the " +
                "AI Transaction Anomaly Detection System.\n\n" +
                "If you received this email, your SMTP " +
                "configuration is working correctly.\n\n" +
                "HIGH-RISK transaction alerts are now ready " +
                "to be tested.\n\n" +
                "Transaction Anomaly Detection System"
        );

        mailSender.send(message);

        System.out.println(
                "Test email sent successfully to: " +
                alertEmailTo
        );
    }

    /**
     * Sends an alert when a HIGH_RISK transaction is detected.
     */
    public void sendHighRiskAlert(
            Transaction transaction,
            Anomaly anomaly) {

        if (alertEmailTo == null || alertEmailTo.isBlank()) {

            System.out.println(
                    "High-risk email alert skipped: " +
                    "ALERT_EMAIL_TO is not configured."
            );

            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(alertEmailTo);

        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }

        message.setSubject(
                "HIGH-RISK Transaction Alert - TX-" +
                        transaction.getId()
        );

        String emailBody =
                "HIGH-RISK TRANSACTION DETECTED\n\n" +

                "Transaction ID: TX-" +
                transaction.getId() + "\n" +

                "User ID: " +
                transaction.getUserId() + "\n" +

                "Amount: ₹" +
                transaction.getAmount() + "\n" +

                "Transaction Type: " +
                transaction.getTransactionType() + "\n" +

                "Merchant: " +
                transaction.getMerchant() + "\n" +

                "Location: " +
                transaction.getLocation() + "\n" +

                "Payment Method: " +
                transaction.getPaymentMethod() + "\n" +

                "Transaction Time: " +
                transaction.getTransactionTime() + "\n\n" +

                "ML Risk Score: " +
                anomaly.getMlScore() + "\n" +

                "Rule Risk Score: " +
                anomaly.getRuleScore() + "\n" +

                "Final Risk Score: " +
                anomaly.getRiskScore() + "\n" +

                "Status: " +
                anomaly.getStatus() + "\n\n" +

                "Reasons:\n" +
                anomaly.getReasons() + "\n\n" +

                "Please review this transaction in the " +
                "AI Transaction Anomaly Detection System.";

        message.setText(emailBody);

        try {

            mailSender.send(message);

            System.out.println(
                    "HIGH-RISK email alert sent for TX-" +
                            transaction.getId()
            );

        } catch (Exception e) {

            System.err.println(
                    "=============================================="
            );
        
            System.err.println(
                    "FAILED TO SEND HIGH-RISK EMAIL"
            );
        
            System.err.println(
                    "Recipient: " + alertEmailTo
            );
        
            System.err.println(
                    "Sender: " + mailFrom
            );
        
            System.err.println(
                    "Error: " + e.getMessage()
            );
        
            e.printStackTrace();
        
            System.err.println(
                    "=============================================="
            );
        }
    }
}