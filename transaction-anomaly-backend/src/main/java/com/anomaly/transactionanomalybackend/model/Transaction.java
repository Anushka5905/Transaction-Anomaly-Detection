package com.anomaly.transactionanomalybackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive(message = "User ID must be greater than 0.")
    private Long userId;

    @NotNull(message = "Transaction amount is required.")
    @Positive(message = "Transaction amount must be greater than 0.")
    private Double amount;

    @NotBlank(message = "Transaction type is required.")
    @Size(max = 50, message = "Transaction type cannot exceed 50 characters.")
    private String transactionType;

    @Size(max = 100, message = "Location cannot exceed 100 characters.")
    private String location;

    @Size(max = 150, message = "Merchant name cannot exceed 150 characters.")
    private String merchant;

    @Size(max = 50, message = "Payment method cannot exceed 50 characters.")
    private String paymentMethod;

    @NotNull(message = "Transaction time is required.")
    private LocalDateTime transactionTime;

    private Double accountBalance;

    public Transaction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public Double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(Double accountBalance) {
        this.accountBalance = accountBalance;
    }
}