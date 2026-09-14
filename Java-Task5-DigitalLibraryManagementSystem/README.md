# Digital Library Management System

This project was completed as Task 5 of the Java Development track for the OIB-SIP internship.

It is a web-based library management system built with Spring Boot. The application has separate Admin and User functions for managing books, members, borrowing, returns, fines, reservations and library queries.

## Features

### User

- User registration and login
- Passwords stored using BCrypt hashing
- Browse the book catalogue
- Filter books by category
- Search for books by title or author
- Issue available books
- Return issued books
- View current issued books
- Reserve unavailable books
- Pick up a book when a reservation becomes ready
- View reservations from the user dashboard
- Submit a contact/query message to the library

### Admin

- Admin dashboard
- Add new books
- Edit existing books
- Delete books
- Manage book quantities
- View currently issued books and due dates
- View registered members
- View messages submitted by users
- View generated fines
- Mark fines as paid

## Borrowing and Fine Rules

Books are issued for 14 days.

When a book is returned after its due date, the system calculates a fine of R5.00 for every day overdue.

When a returned copy has an existing reservation, the copy is held for the earliest pending reservation instead of immediately becoming available to everyone.

## Technologies Used

- Java 21
- Spring Boot 3.3.2
- Spring MVC
- Spring Data JPA
- Thymeleaf
- Spring Security password encoding
- MySQL
- Maven
- Lombok
- HTML and CSS

## Project Structure

The backend is separated into the following main packages:

```text
com.oibsip.library
├── config
├── controller
├── model
├── repository
└── service
```

The main application class is:

```text
LibraryApplication.java
```

The main domain models include:

- `Book`
- `User`
- `IssueRecord`
- `Reservation`
- `ContactMessage`

## Database Configuration

The application uses MySQL. Database settings are stored in:

```text
src/main/resources/application.properties
```

Update the following values to match your local MySQL installation:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

Hibernate is configured to update the database schema automatically:

```properties
spring.jpa.hibernate.ddl-auto=update
```

The application runs on port `8081`.

## Running the Project

1. Make sure Java 21, Maven and MySQL are installed.
2. Start MySQL Server.
3. Update the database username and password in `application.properties`.
4. Open a terminal in the project directory.
5. Run:

```bash
mvn spring-boot:run
```

6. Open the application in a browser at:

```text
http://localhost:8081
```

New users can register through the registration page and then log in.

## How the System Works

A registered user can browse or search the catalogue and issue a book when a copy is available. Issuing a book reduces its available quantity and creates an issue record with an issue date and due date.

When the user returns the book, the system records the return date and checks whether the book is overdue. If it is overdue, the fine is calculated automatically.

If all copies of a book are already issued, a user can place a reservation. When a copy becomes available, the earliest pending reservation is moved to a ready-for-pickup state. Once collected, the reservation is converted into an issue record.

The admin side provides the controls needed to maintain the catalogue, monitor issued books, view members and messages, and manage fines.

## Project Purpose

The project demonstrates a complete Java web application using a layered Spring Boot structure. It combines authentication, role-based application flows, database persistence, CRUD operations and business rules for borrowing, returns, reservations and fines.
