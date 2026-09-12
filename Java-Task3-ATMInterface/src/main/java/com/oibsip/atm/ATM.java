package com.oibsip.atm;
import java.sql.SQLException;
import java.util.Scanner;

public class ATM {
    private Bank bank;
    private DatabaseManager db;
    private Scanner scanner;
    private static final int MAX_ATTEMPTS = 3;

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
                System.out.println("Too many incorrect attempts. Returning to start screen.\n");
                continue;
            }

            System.out.println("\nLogin successful. Welcome, " + account.getAccountId() + "!");
            runMenu(account);
            System.out.println();
        }
    }

    private Account login() {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Enter User ID (or type 'quit' to exit): ");
            String userId = scanner.nextLine().trim();

            if (userId.equalsIgnoreCase("quit")) {
                throw new ExitRequestedException();
            }

            System.out.print("Enter PIN: ");
            String pin = scanner.nextLine().trim();

            if (bank.accountExists(userId)) {
                Account account = bank.getAccount(userId);
                if (account.validatePin(pin)) {
                    return account;
                }
            }
            attempts++;
            System.out.println("Incorrect User ID or PIN. Attempts remaining: " + (MAX_ATTEMPTS - attempts));
        }
        return null;
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