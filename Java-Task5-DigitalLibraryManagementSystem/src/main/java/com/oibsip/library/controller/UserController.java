package com.oibsip.library.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oibsip.library.model.Book;
import com.oibsip.library.model.ContactMessage;
import com.oibsip.library.model.Reservation;
import com.oibsip.library.model.User;
import com.oibsip.library.repository.ContactMessageRepository;
import com.oibsip.library.repository.IssueRecordRepository;
import com.oibsip.library.service.BookService;
import com.oibsip.library.service.LibraryService;
import com.oibsip.library.service.ReservationService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    private final BookService bookService;
    private final LibraryService libraryService;
    private final ReservationService reservationService;
    private final IssueRecordRepository issueRecordRepository;
    private final ContactMessageRepository contactMessageRepository;

    UserController(BookService bookService, IssueRecordRepository issueRecordRepository,
            ContactMessageRepository contactMessageRepository, ReservationService reservationService,
            LibraryService libraryService) {
        this.bookService = bookService;
        this.issueRecordRepository = issueRecordRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.reservationService = reservationService;
        this.libraryService = libraryService;
    }

    private User currentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    @ModelAttribute("currentUserName")
    public String currentUserName(HttpSession session) {
        User u = currentUser(session);
        return u != null ? u.getFullName() : "";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = currentUser(session);
        model.addAttribute("user", user);
        model.addAttribute("myIssuedBooks", issueRecordRepository.findByUserAndReturnDateIsNull(user));
        model.addAttribute("myReservations", reservationService.forUser(user));
        return "user/dashboard";
    }

    @GetMapping("/browse")
    public String browse(@RequestParam(required = false) String category, Model model) {
        List<Book> books = (category == null || category.isBlank())
                ? bookService.listAll()
                : bookService.findByCategory(category);
        model.addAttribute("books", books);
        model.addAttribute("category", category);
        return "user/browse";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        List<Book> results = (q == null || q.isBlank()) ? List.of() : bookService.search(q);
        model.addAttribute("books", results);
        model.addAttribute("query", q);
        return "user/browse";
    }

    @PostMapping("/issue/{bookId}")
    public String issue(@PathVariable Long bookId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = currentUser(session);
        Optional<Book> book = bookService.findById(bookId);
        if (book.isPresent()) {
            String error = libraryService.issueBook(book.get(), user);
            if (error != null)
                redirectAttributes.addFlashAttribute("error", error);
        }
        return "redirect:/user/dashboard";
    }

    @PostMapping("/return/{issueId}")
    public String returnBook(@PathVariable Long issueId) {
        issueRecordRepository.findById(issueId).ifPresent(libraryService::returnBook);
        return "redirect:/user/dashboard";
    }

    @PostMapping("/pickup/{reservationId}")
    public String pickup(@PathVariable Long reservationId, RedirectAttributes redirectAttributes) {
        Optional<Reservation> reservation = reservationService.findById(reservationId);
        if (reservation.isPresent()) {
            String error = libraryService.issueFromReservation(reservation.get());
            if (error != null)
                redirectAttributes.addFlashAttribute("error", error);
        }
        return "redirect:/user/dashboard";
    }

    @PostMapping("/reserve/{bookId}")
    public String reserve(@PathVariable Long bookId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = currentUser(session);
        Optional<Book> book = bookService.findById(bookId);
        if (book.isPresent()) {
            String error = reservationService.reserve(book.get(), user);
            if (error != null)
                redirectAttributes.addFlashAttribute("error", error);
        }
        return "redirect:/user/browse";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "user/contact";
    }

    @PostMapping("/contact")
    public String submitContact(@RequestParam String name, @RequestParam String email,
            @RequestParam String message, RedirectAttributes redirectAttributes) {
        contactMessageRepository.save(new ContactMessage(name, email, message));
        redirectAttributes.addFlashAttribute("success", "Your message has been sent to the library admin.");
        return "redirect:/user/contact";
    }
}