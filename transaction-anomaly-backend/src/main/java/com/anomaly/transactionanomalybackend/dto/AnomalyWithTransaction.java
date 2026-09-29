package com.anomaly.transactionanomalybackend.dto;

import com.anomaly.transactionanomalybackend.model.Anomaly;
import com.anomaly.transactionanomalybackend.model.Transaction;

public class AnomalyWithTransaction {

    private Anomaly anomaly;
    private Transaction transaction;

    public AnomalyWithTransaction(
            Anomaly anomaly,
            Transaction transaction) {

        this.anomaly = anomaly;
        this.transaction = transaction;
    }

    public Anomaly getAnomaly() {
        return anomaly;
    }

    public void setAnomaly(Anomaly anomaly) {
        this.anomaly = anomaly;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}