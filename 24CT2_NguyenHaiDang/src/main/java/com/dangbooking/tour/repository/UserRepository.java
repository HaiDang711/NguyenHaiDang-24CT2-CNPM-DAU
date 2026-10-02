package com.dangbooking.tour.repository;

import com.dangbooking.tour.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByPhone(String phone);
    Optional<User> findByPhoneOrEmail(String phone, String email);
}
