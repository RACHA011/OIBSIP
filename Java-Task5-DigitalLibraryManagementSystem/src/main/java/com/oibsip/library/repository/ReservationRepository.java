package com.oibsip.library.repository;

import com.oibsip.library.model.Book;
import com.oibsip.library.model.Reservation;
import com.oibsip.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByBookAndStatus(Book book, Reservation.Status status);

    // Used to pick who gets a newly-freed copy: whoever reserved it first.
    Optional<Reservation> findFirstByBookAndStatusOrderByReservationDateAsc(Book book, Reservation.Status status);
}