package com.anomaly.transactionanomalybackend.repository;

import com.anomaly.transactionanomalybackend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}