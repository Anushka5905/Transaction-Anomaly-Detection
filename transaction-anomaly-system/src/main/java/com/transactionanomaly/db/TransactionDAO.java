package com.transactionanomaly.db;

import com.transactionanomaly.model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * "Data Access Object" - the only class that talks SQL.
 * Everything else in the app works with Transaction objects, not raw rows.
 */
public class TransactionDAO {

    /** Adds one new transaction to the database. */
    public void insertTransaction(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, amount, transaction_time, type, location) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getUserId());
            ps.setDouble(2, t.getAmount());
            ps.setTimestamp(3, Timestamp.valueOf(t.getTransactionTime()));
            ps.setString(4, t.getType());
            ps.setString(5, t.getLocation());
            ps.executeUpdate();
        }
    }

    /** Returns every transaction in the database, oldest first. */
    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> results = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_time";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    /** After running anomaly detection, saves the score/flag back to the database. */
    public void updateAnomalyResult(int id, double score, boolean isAnomaly) throws SQLException {
        String sql = "UPDATE transactions SET anomaly_score = ?, is_anomaly = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, score);
            ps.setBoolean(2, isAnomaly);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    /** Turns one row of the ResultSet into a Transaction object. */
    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getString("user_id"));
        t.setAmount(rs.getDouble("amount"));
        t.setTransactionTime(rs.getTimestamp("transaction_time").toLocalDateTime());
        t.setType(rs.getString("type"));
        t.setLocation(rs.getString("location"));
        t.setAnomalyScore(rs.getDouble("anomaly_score"));
        t.setAnomaly(rs.getBoolean("is_anomaly"));
        return t;
    }
}
