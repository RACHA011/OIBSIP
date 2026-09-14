package com.oibsip.atm;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ATM {
    private Bank bank;
    private DatabaseManager db;
    private Scanner scanner;

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCKOUT_SECONDS = 30;

    // In-memory only — a lockout doesn't need to survive restarting the app.
    private Map<String, Integer> failedAttempts = new HashMap<>();
    private Map<String, LocalDateTime> lockedUntil = new HashMap<>();

    public ATM(Bank bank, DatabaseManager db) {
        this.bank = bank;
        this.db = db;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== Welcome to the ATM ===");
        while (true) {
            Account account;
            try {
                account = login();
            } catch (ExitRequestedException e) {
                System.out.println("Shutting down ATM. Goodbye!");
                break;
            }

            if (account == null) {
                // login() already explained why (wrong PIN, or locked) — just re-prompt.
                continue;
            }

            System.out.println("\nLogin successful. Welcome, " + account.getAccountId() + "!");
            runMenu(account);
            System.out.println();
        }
    }

    // A single login attempt: one User ID prompt, one PIN prompt (unless locked).
    // Returns the account on success, or null on any failure — the message
    // explaining exactly what went wrong is printed here, not by the caller.
    private Account login() {
        System.out.print("Enter User ID (or type 'quit' to exit): ");
        String userId = scanner.nextLine().trim();

        if (userId.equalsIgnoreCase("quit")) {
            throw new ExitRequestedException();
        }

        if (isLocked(userId)) {
            System.out.println("This account is locked due to too many failed attempts. "
                    + "Try again in " + secondsUntilUnlock(userId) + " second(s), or use a different account.");
            return null;
        }

        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine().trim();

        if (bank.accountExists(userId)) {
            Account account = bank.getAccount(userId);
            if (account.validatePin(pin)) {
                failedAttempts.remove(userId);
                lockedUntil.remove(userId);
                return account;
            }
        }

        recordFailedAttempt(userId);
        if (isLocked(userId)) {
            System.out.println(
                    "Incorrect User ID or PIN. This account is now locked for " + LOCKOUT_SECONDS + " seconds.");
        } else {
            int remaining = MAX_ATTEMPTS - failedAttempts.getOrDefault(userId, 0);
            System.out.println("Incorrect User ID or PIN. Attempts remaining before lockout: " + remaining);
        }
        return null;
    }

    private boolean isLocked(String userId) {
        LocalDateTime until = lockedUntil.get(userId);
        return until != null && LocalDateTime.now().isBefore(until);
    }

    private long secondsUntilUnlock(String userId) {
        LocalDateTime until = lockedUntil.get(userId);
        return Duration.between(LocalDateTime.now(), until).getSeconds() + 1;
    }

    private void recordFailedAttempt(String userId) {
        int attempts = failedAttempts.getOrDefault(userId, 0) + 1;
        if (attempts >= MAX_ATTEMPTS) {
            lockedUntil.put(userId, LocalDateTime.now().plusSeconds(LOCKOUT_SECONDS));
            failedAttempts.remove(userId); // fresh cycle once the lockout expires
        } else {
            failedAttempts.put(userId, attempts);
        }
    }

    private void runMenu(Account account) {
        boolean sessionActive = true;
        while (sessionActive) {
            System.out.println("\n===== Main Menu (" + account.getAccountId() + ") =====");
            System.out.println("1. Transaction History");
            System.out.println("2. Withdraw");
            System.out.println("3. Deposit");
            System.out.println("4. Transfer");
            System.out.println("5. Quit (log out — lets another user log in)");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showHistory(account);
                    break;
                case "2":
                    withdraw(account);
                    break;
                case "3":
                    deposit(account);
                    break;
                case "4":
                    transfer(account);
                    break;
                case "5":
                    sessionActive = false;
                    System.out.println("Logged out. Thank you, " + account.getAccountId() + "!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void showHistory(Account account) {
        System.out.println("\n--- Transaction History ---");
        if (account.getTransactionHistory().isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (Transaction t : account.getTransactionHistory()) {
                System.out.println(t);
            }
        }
    }

    private void withdraw(Account account) {
        System.out.print("Enter amount to withdraw: $");
        double amount = readAmount();
        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }
        if (account.withdraw(amount)) {
            persist(account);
            System.out.printf("Withdrawal successful. New balance: $%.2f%n", account.getBalance());
        } else {
            System.out.println("Insufficient Funds.");
        }
    }

    private void deposit(Account account) {
        System.out.print("Enter amount to deposit: $");
        double amount = readAmount();
        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }
        account.deposit(amount);
        persist(account);
        System.out.printf("Deposit successful. New balance: $%.2f%n", account.getBalance());
    }

    private void transfer(Account account) {
        System.out.print("Enter recipient account ID: ");
        String toId = scanner.nextLine().trim();
        System.out.print("Enter amount to transfer: $");
        double amount = readAmount();

        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }
        if (toId.equals(account.getAccountId())) {
            System.out.println("Cannot transfer to your own account.");
            return;
        }

        boolean success = bank.transfer(account, toId, amount);
        if (success) {
            persist(account);
            persist(bank.getAccount(toId));
            System.out.printf("Transfer successful. New balance: $%.2f%n", account.getBalance());
        } else if (!bank.accountExists(toId)) {
            System.out.println("Recipient account not found.");
        } else {
            System.out.println("Insufficient Funds.");
        }
    }

    private void persist(Account account) {
        try {
            db.updateBalance(account.getAccountId(), account.getBalance());
            Transaction latest = account.getTransactionHistory()
                    .get(account.getTransactionHistory().size() - 1);
            db.insertTransaction(account.getAccountId(), latest);
        } catch (SQLException e) {
            System.out.println("Warning: could not save to database — " + e.getMessage());
        }
    }

    private double readAmount() {
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}