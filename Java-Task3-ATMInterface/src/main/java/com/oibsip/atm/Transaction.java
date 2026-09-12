package com.oibsip.atm;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String type;
    private double amount;
    private String timestamp;
    private String details;

    public Transaction(String type, double amount, String details) {
        this.type = type;
        this.amount = amount;
        this.details = details;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Used when rebuilding a transaction from a database row (timestamp already exists)
    public Transaction(String type, double amount, String details, String timestamp) {
        this.type = type;
        this.amount = amount;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDetails() {
        return details;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: $%.2f - %s", timestamp, type, amount, details);
    }
}