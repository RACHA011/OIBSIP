package com.oibsip.library.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.oibsip.library.model.Book;
import com.oibsip.library.model.IssueRecord;
import com.oibsip.library.model.Reservation;
import com.oibsip.library.model.User;
import com.oibsip.library.repository.BookRepository;
import com.oibsip.library.repository.IssueRecordRepository;
import com.oibsip.library.repository.ReservationRepository;

@Service
public class LibraryService {

    private static final int LOAN_PERIOD_DAYS = 14;
    private static final double FINE_PER_DAY = 5.0; // R5/day, per checklist

    private final ReservationRepository reservationRepository;

    private final BookRepository bookRepository;

    private final IssueRecordRepository issueRecordRepository;

    LibraryService(BookRepository bookRepository, IssueRecordRepository issueRecordRepository,
            ReservationRepository reservationRepository) {
        this.bookRepository = bookRepository;
        this.issueRecordRepository = issueRecordRepository;
        this.reservationRepository = reservationRepository;
    }

    // Returns an error message if the issue could not happen, or null on success.
    public String issueBook(Book book, User user) {
        if (book.getAvailableQuantity() <= 0) {
            return "This book is currently unavailable. You can place a reservation instead.";
        }
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        LocalDate today = LocalDate.now();
        IssueRecord record = new IssueRecord(book, user, today, today.plusDays(LOAN_PERIOD_DAYS));
        issueRecordRepository.save(record);
        return null;
    }

    // Returns an error message if the record was already returned, or null on
    // success.
    public String returnBook(IssueRecord record) {
        if (record.getReturnDate() != null) {
            return "This book has already been returned.";
        }
        LocalDate today = LocalDate.now();
        record.setReturnDate(today);

        long daysLate = ChronoUnit.DAYS.between(record.getDueDate(), today);
        if (daysLate > 0) {
            record.setFineAmount(daysLate * FINE_PER_DAY);
        }

        Book book = record.getBook();
        releaseCopy(book);
        bookRepository.save(book);
        issueRecordRepository.save(record);
        return null;
    }

    // Adds copies to a book's shelf count (e.g. an admin raising total quantity),
    // running each new copy through the same reservation-priority check as a
    // return.
    public void increaseAvailability(Book book, int extraCopies) {
        for (int i = 0; i < extraCopies; i++) {
            releaseCopy(book);
        }
        bookRepository.save(book);
    }

    // A copy just became free. If someone has been waiting for this exact book,
    // it's held for them (READY_FOR_PICKUP) instead of going back to the general
    // shelf count — whoever reserved it earliest gets first claim.
    private void releaseCopy(Book book) {
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        reservationRepository
                .findFirstByBookAndStatusOrderByReservationDateAsc(book, Reservation.Status.PENDING)
                .ifPresent(reservation -> {
                    reservation.setStatus(Reservation.Status.READY_FOR_PICKUP);
                    reservationRepository.save(reservation);
                    book.setAvailableQuantity(book.getAvailableQuantity() - 1);
                });
    }

    // Converts a READY_FOR_PICKUP reservation into an actual issue for that user.
    // Returns an error message if it isn't ready yet, or null on success.
    public String issueFromReservation(Reservation reservation) {
        if (reservation.getStatus() != Reservation.Status.READY_FOR_PICKUP) {
            return "This reservation isn't ready for pickup yet.";
        }
        LocalDate today = LocalDate.now();
        IssueRecord record = new IssueRecord(reservation.getBook(), reservation.getUser(), today,
                today.plusDays(LOAN_PERIOD_DAYS));
        issueRecordRepository.save(record);

        reservation.setStatus(Reservation.Status.FULFILLED);
        reservationRepository.save(reservation);
        return null;
    }

    public List<IssueRecord> currentlyIssued() {
        return issueRecordRepository.findByReturnDateIsNull();
    }

    public List<IssueRecord> historyForUser(User user) {
        return issueRecordRepository.findByUser(user);
    }
}