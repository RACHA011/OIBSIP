package com.oibsip.library.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oibsip.library.model.Book;
import com.oibsip.library.model.Reservation;
import com.oibsip.library.model.User;
import com.oibsip.library.repository.ReservationRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    // Returns an error message if reservation isn't applicable, or null on success.
    public String reserve(Book book, User user) {
        if (book.getAvailableQuantity() > 0) {
            return "This book is currently available - you can issue it directly instead of reserving it.";
        }
        Reservation reservation = new Reservation(book, user, LocalDate.now());
        reservationRepository.save(reservation);
        return null;
    }

    public List<Reservation> forUser(User user) {
        return reservationRepository.findByUser(user);
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> all() {
        return reservationRepository.findAll();
    }
}