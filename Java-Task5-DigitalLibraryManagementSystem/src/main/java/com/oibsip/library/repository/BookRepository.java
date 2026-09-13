package com.oibsip.library.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oibsip.library.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByCategoryIgnoreCase(String category);
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
}