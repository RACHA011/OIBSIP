package com.oibsip.library.config;

import com.oibsip.library.model.Book;
import com.oibsip.library.model.User;
import com.oibsip.library.repository.BookRepository;
import com.oibsip.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Seeds one admin account and a few sample books on first run so the app
// is demo-ready immediately (idempotent — safe to restart repeatedly).
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, BookRepository bookRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@library.com").isEmpty()) {
            User admin = new User();
            admin.setFullName("Library Admin");
            admin.setEmail("admin@library.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);

            // Save the admin user to the database
            userRepository.save(admin);
        }

        if (bookRepository.count() == 0) {
            Book book1 = new Book();
            Book book2 = new Book();
            Book book3 = new Book();
            Book book4 = new Book();

            // book1
            book1.setTitle("Clean Code");
            book1.setAuthor("Robert C. Martin");
            book1.setIsbn("9780132350884");
            book1.setCategory("Technology");
            book1.setTotalQuantity(3);

            // book2 new Book("Sapiens", "Yuval Noah Harari", "9780062316097",
            // "Non-Fiction", 2)
            book2.setTitle("Sapiens");
            book2.setAuthor("Yuval Noah Harari");
            book2.setIsbn("9780062316097");
            book2.setCategory("Non-Fiction");
            book2.setTotalQuantity(2);

            // book3 (new Book("Dune", "Frank Herbert", "9780441172719", "Fiction", 1)
            book3.setTitle("Dune");
            book3.setAuthor("Frank Herbert");
            book3.setIsbn("9780441172719");
            book3.setCategory("Fiction");
            book3.setTotalQuantity(1);

            // book4 new Book("A Brief History of Time", "Stephen Hawking", "9780553380163",
            // "Science", 2)
            book4.setTitle("A Brief History of Time");
            book4.setAuthor("Stephen Hawking");
            book4.setIsbn("9780553380163");
            book4.setCategory("Science");
            book4.setTotalQuantity(2);

            // book5 new Book("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565",
            // "Fiction", 3)

            bookRepository.save(book1);
            bookRepository.save(book2);
            bookRepository.save(book3);
            bookRepository.save(book4);
        }
    }
}