# ATM Interface

This project was completed as Task 3 of the Java Development track for the OIB-SIP internship.

It is a console-based ATM simulation written in Java. The application allows users to log in with an account ID and PIN, view transaction history, withdraw money, deposit money, transfer funds to another account, and log out so that another user can use the ATM.

The project also uses MySQL to keep account balances and transaction records available after the application is closed and started again.

## Features

- Login using account ID and PIN
- Maximum of three failed login attempts before a temporary account lock
- 30-second lockout after repeated failed login attempts
- Transaction history
- Withdraw funds with balance validation
- Deposit funds
- Transfer funds between existing accounts
- Prevent transfers to the same account
- Prevent withdrawals or transfers when funds are insufficient
- MySQL persistence for account balances and transaction history
- Session logout so another user can use the ATM
- Option to shut down the ATM from the login screen

## Technologies Used

- Java
- JDBC
- MySQL
- Object-Oriented Programming
- Java Collections

## Main Classes

- `Main` - starts the application and prepares the database connection
- `ATM` - handles login, menu navigation and user input
- `Bank` - manages accounts and transfers
- `Account` - represents an ATM account and keeps its transaction history
- `Transaction` - represents a deposit, withdrawal or transfer
- `DatabaseManager` - handles the MySQL connection, table creation and persistence

## Database

The application uses a MySQL database named:

```sql
atm_db
```

Create the database before running the application:

```sql
CREATE DATABASE atm_db;
```

The application creates the required `accounts` and `transactions` tables when it starts.

Update the database connection details in `DatabaseManager.java` so that they match your local MySQL setup:

```java
private static final String URL =
        "jdbc:mysql://localhost:3306/atm_db?useSSL=false&serverTimezone=UTC";

private static final String USERNAME = "your_mysql_username";
private static final String PASSWORD = "your_mysql_password";
```

## Sample Accounts

On the first run, the program inserts three sample accounts if the accounts table is empty:

| Account ID | PIN  | Starting Balance |
|------------|------|------------------|
| 1001       | 1234 | 500.00           |
| 1002       | 5678 | 1000.00          |
| 1003       | 0000 | 250.00           |

These records are only inserted when the table is empty, so restarting the application does not reset balances.

## Running the Project

1. Make sure MySQL Server is running.
2. Create the `atm_db` database.
3. Update the MySQL username and password in `DatabaseManager.java`.
4. Add MySQL Connector/J to the project classpath.
5. Run `Main.java`.
6. Log in using one of the sample accounts.

At the login screen, type `quit` as the user ID if you want to stop the ATM application.

## Transaction History

Transactions are kept in an `ArrayList<Transaction>` for use by the application and are also saved to MySQL. Existing transaction records are loaded from the database when the application starts.

The history records deposits, withdrawals, incoming transfers and outgoing transfers together with the transaction amount, balance details and timestamp.

## Project Purpose

The main purpose of the project is to practise Java object-oriented programming by separating ATM operations into different classes while handling user input, validation, collections, JDBC database access and transaction persistence.
