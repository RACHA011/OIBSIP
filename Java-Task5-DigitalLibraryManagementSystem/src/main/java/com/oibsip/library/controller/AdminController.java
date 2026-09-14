package com.oibsip.library.controller;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.oibsip.library.model.Book;
import com.oibsip.library.model.User;
import com.oibsip.library.repository.ContactMessageRepository;
import com.oibsip.library.repository.IssueRecordRepository;
import com.oibsip.library.service.BookService;
import com.oibsip.library.service.LibraryService;
import com.oibsip.library.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookService bookService;
    private final LibraryService libraryService;
    private final UserService userService;
    private final ContactMessageRepository contactMessageRepository;
    private final IssueRecordRepository issueRecordRepository;

    AdminController(IssueRecordRepository issueRecordRepository, BookService bookService, UserService userService,
            LibraryService libraryService, ContactMessageRepository contactMessageRepository) {
        this.issueRecordRepository = issueRecordRepository;
        this.bookService = bookService;
        this.userService = userService;
        this.libraryService = libraryService;
        this.contactMessageRepository = contactMessageRepository;
    }

    @ModelAttribute("currentUserName")
    public String currentUserName(HttpSession session) {
        User u = (User) session.getAttribute("currentUser");
        return u != null ? u.getFullName() : "Admin";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalBooks", bookService.listAll().size());
        model.addAttribute("totalMembers", userService.listAllMembers().size());
        model.addAttribute("currentlyIssued", libraryService.currentlyIssued().size());
        return "admin/dashboard";
    }

    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.listAll());
        return "admin/books";
    }

    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "admin/book-form";
    }

    @GetMapping("/books/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        Optional<Book> book = bookService.findById(id);
        if (book.isEmpty())
            return "redirect:/admin/books";
        model.addAttribute("book", book.get());
        return "admin/book-form";
    }

    @PostMapping("/books/save")
    public String saveBook(@ModelAttribute Book book, Model model) {
        try {
            if (book.getId() != null) {
                Optional<Book> existingOpt = bookService.findById(book.getId());
                if (existingOpt.isPresent()) {
                    Book existing = existingOpt.get();
                    int delta = book.getTotalQuantity() - existing.getTotalQuantity();

                    existing.setTitle(book.getTitle());
                    existing.setAuthor(book.getAuthor());
                    existing.setIsbn(book.getIsbn());
                    existing.setCategory(book.getCategory());
                    existing.setTotalQuantity(book.getTotalQuantity());

                    if (delta > 0) {
                        // New copies go through the same reservation-priority check as a
                        // return, so anyone waiting on this book gets first claim.
                        libraryService.increaseAvailability(existing, delta);
                    } else if (delta < 0) {
                        existing.setAvailableQuantity(Math.max(0, existing.getAvailableQuantity() + delta));
                        bookService.save(existing);
                    } else {
                        bookService.save(existing);
                    }
                }
            } else {
                book.setAvailableQuantity(book.getTotalQuantity());
                bookService.save(book);
            }
        } catch (DataIntegrityViolationException e) {
            // Most likely cause: the ISBN is already used by another book (unique
            // constraint).
            model.addAttribute("book", book);
            model.addAttribute("error", "A book with this ISBN already exists. Please use a different ISBN.");
            return "admin/book-form";
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.delete(id);
        return "redirect:/admin/books";
    }

    @GetMapping("/issued")
    public String issuedBooks(Model model) {
        model.addAttribute("issuedRecords", libraryService.currentlyIssued());
        return "admin/issued";
    }

    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("members", userService.listAllMembers());
        return "admin/members";
    }

    @PostMapping("/fines/{issueId}/pay")
    public String markFinePaid(@PathVariable Long issueId) {
        issueRecordRepository.findById(issueId).ifPresent(record -> {
            record.setFinePaid(true);
            issueRecordRepository.save(record);
        });
        return "redirect:/admin/issued";
    }

    @GetMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("messages", contactMessageRepository.findAll());
        return "admin/messages";
    }
}