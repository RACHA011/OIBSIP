package com.oibsip.atm;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bank {
    private Map<String, Account> accounts;

    public Bank() {
        accounts = new HashMap<>();
    }

    // Populates this bank's accounts (and their transaction history) from MySQL.
    public void loadFromDatabase(DatabaseManager db) throws SQLException {
        for (Object[] row : db.fetchAllAccounts()) {
            String accountId = (String) row[0];
            String pin = (String) row[1];
            double balance = (Double) row[2];

            Account account = new Account(accountId, pin, balance);
            List<Transaction> history = db.fetchTransactions(accountId);
            for (Transaction t : history) {
                account.restoreTransaction(t);
            }
            addAccount(account);
        }
    }

    public void addAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public boolean accountExists(String accountId) {
        return accounts.containsKey(accountId);
    }

    public boolean transfer(Account from, String toAccountId, double amount) {
        Account to = accounts.get(toAccountId);
        if (to == null) {
            return false;
        }
        if (amount > from.getBalance()) {
            return false;
        }
        from.deductForTransfer(amount, toAccountId);
        to.receiveTransfer(amount, from.getAccountId());
        return true;
    }
}