package com.anomaly.transactionanomalybackend.repository;

import com.anomaly.transactionanomalybackend.model.Anomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnomalyRepository extends JpaRepository<Anomaly, Long> {

    List<Anomaly> findByStatus(String status);

    List<Anomaly> findByUserId(Long userId);

    List<Anomaly> findByRiskScoreGreaterThanEqual(Double riskScore);

    boolean existsByTransactionId(Long transactionId);
}