-- Run this once in MySQL before starting the app.

CREATE DATABASE IF NOT EXISTS transaction_anomaly_db;
USE transaction_anomaly_db;

CREATE TABLE IF NOT EXISTS transactions (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    user_id            VARCHAR(50)     NOT NULL,
    amount             DOUBLE          NOT NULL,
    transaction_time   DATETIME        NOT NULL,
    type               VARCHAR(50)     NOT NULL,
    location            VARCHAR(100)    NOT NULL,
    anomaly_score       DOUBLE          DEFAULT 0,
    is_anomaly          BOOLEAN         DEFAULT FALSE
);

-- A few sample rows so the dashboard has something to show immediately.
-- Notice row 6 and 10 are deliberately unusual (very high amount / odd hour).
INSERT INTO transactions (user_id, amount, transaction_time, type, location) VALUES
('U100', 45.50,   '2026-08-01 09:15:00', 'PURCHASE', 'Mumbai'),
('U100', 60.00,   '2026-08-02 10:05:00', 'PURCHASE', 'Mumbai'),
('U100', 38.75,   '2026-08-03 09:40:00', 'PURCHASE', 'Mumbai'),
('U100', 52.10,   '2026-08-04 11:00:00', 'PURCHASE', 'Mumbai'),
('U100', 41.00,   '2026-08-05 09:55:00', 'PURCHASE', 'Mumbai'),
('U100', 9800.00, '2026-08-06 03:20:00', 'PURCHASE', 'Delhi'),
('U101', 120.00,  '2026-08-01 14:10:00', 'TRANSFER', 'Pune'),
('U101', 135.50,  '2026-08-02 14:45:00', 'TRANSFER', 'Pune'),
('U101', 110.25,  '2026-08-03 13:30:00', 'TRANSFER', 'Pune'),
('U101', 7600.00, '2026-08-04 02:05:00', 'TRANSFER', 'Chennai');
