package com.oibsip.library.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oibsip.library.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}