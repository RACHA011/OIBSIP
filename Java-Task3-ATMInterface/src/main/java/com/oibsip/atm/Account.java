package com.oibsip.atm;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private String accountId;
    private String pin;
    private double balance;
    private List<Transaction> transactionHistory;

    public Account(String accountId, String pin, double initialBalance) {
        this.accountId = accountId;
        this.pin = pin;
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
    }

    public boolean validatePin(String enteredPin) {
        return this.pin.equals(enteredPin);
    }

    public String getAccountId() {
        return accountId;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public void deposit(double amount) {
        balance += amount;
        transactionHistory.add(new Transaction("DEPOSIT", amount,
                "Balance: $" + String.format("%.2f", balance)));
    }

    public boolean withdraw(double amount) {
        if (amount > balance) {
            return false;
        }
        balance -= amount;
        transactionHistory.add(new Transaction("WITHDRAW", amount,
                "Balance: $" + String.format("%.2f", balance)));
        return true;
    }

    // Adds a transaction to the history WITHOUT touching the balance —
    // used only when replaying past transactions loaded from the database.
    public void restoreTransaction(Transaction t) {
        transactionHistory.add(t);
    }

    void deductForTransfer(double amount, String toAccountId) {
        balance -= amount;
        transactionHistory.add(new Transaction("TRANSFER OUT", amount,
                "To: " + toAccountId + " | Balance: $" + String.format("%.2f", balance)));
    }

    void receiveTransfer(double amount, String fromAccountId) {
        balance += amount;
        transactionHistory.add(new Transaction("TRANSFER IN", amount,
                "From: " + fromAccountId + " | Balance: $" + String.format("%.2f", balance)));
    }
}