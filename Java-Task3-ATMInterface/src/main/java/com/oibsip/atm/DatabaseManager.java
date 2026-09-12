package com.oibsip.atm;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // TODO: update these to match your local MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/atm_db?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "racha";
    private static final String PASSWORD = "password";

    private Connection connection;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing database connection: " + e.getMessage());
        }
    }

    public void initializeSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "account_id VARCHAR(20) PRIMARY KEY, " +
                    "pin VARCHAR(10) NOT NULL, " +
                    "balance DECIMAL(15,2) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "account_id VARCHAR(20) NOT NULL, " +
                    "type VARCHAR(20) NOT NULL, " +
                    "amount DECIMAL(15,2) NOT NULL, " +
                    "details VARCHAR(255), " +
                    "timestamp VARCHAR(30), " +
                    "FOREIGN KEY (account_id) REFERENCES accounts(account_id))");
        }
    }

    // Inserts sample accounts only on first run (table empty), so restarts don't reset balances.
    public void seedIfEmpty() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO accounts (account_id, pin, balance) VALUES (?, ?, ?)")) {
                    Object[][] sampleAccounts = {
                            {"1001", "1234", 500.00},
                            {"1002", "5678", 1000.00},
                            {"1003", "0000", 250.00}
                    };
                    for (Object[] acc : sampleAccounts) {
                        ps.setString(1, (String) acc[0]);
                        ps.setString(2, (String) acc[1]);
                        ps.setDouble(3, (Double) acc[2]);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    public List<Object[]> fetchAllAccounts() throws SQLException {
        List<Object[]> accounts = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT account_id, pin, balance FROM accounts")) {
            while (rs.next()) {
                accounts.add(new Object[]{
                        rs.getString("account_id"),
                        rs.getString("pin"),
                        rs.getDouble("balance")
                });
            }
        }
        return accounts;
    }

    public List<Transaction> fetchTransactions(String accountId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT type, amount, details, timestamp FROM transactions WHERE account_id = ? ORDER BY id ASC")) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getString("type"),
                            rs.getDouble("amount"),
                            rs.getString("details"),
                            rs.getString("timestamp")));
                }
            }
        }
        return transactions;
    }

    public void insertTransaction(String accountId, Transaction t) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO transactions (account_id, type, amount, details, timestamp) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, accountId);
            ps.setString(2, t.getType());
            ps.setDouble(3, t.getAmount());
            ps.setString(4, t.getDetails());
            ps.setString(5, t.getTimestamp());
            ps.executeUpdate();
        }
    }

    public void updateBalance(String accountId, double newBalance) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE accounts SET balance = ? WHERE account_id = ?")) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accountId);
            ps.executeUpdate();
        }
    }
}