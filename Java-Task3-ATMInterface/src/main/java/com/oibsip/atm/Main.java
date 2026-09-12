package com.oibsip.atm;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        Bank bank = new Bank();

        try {
            db.connect();
            db.initializeSchema();
            db.seedIfEmpty();
            bank.loadFromDatabase(db);
        } catch (SQLException e) {
            System.out.println("Could not connect to the database: " + e.getMessage());
            System.out.println("Check the URL/username/password in DatabaseManager.java.");
            return;
        }

        ATM atm = new ATM(bank, db);
        atm.start();

        db.close();
    }
}