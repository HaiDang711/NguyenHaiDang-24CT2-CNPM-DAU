package com.dangbooking.tour.repository;

import com.dangbooking.tour.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);
    List<Admin> findAllByOrderByCreatedAtDesc();
}
