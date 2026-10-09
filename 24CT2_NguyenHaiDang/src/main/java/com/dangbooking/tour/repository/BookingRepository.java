package com.dangbooking.tour.repository;

import com.dangbooking.tour.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Integer userId);
    Optional<Booking> findByIdAndUserId(Integer id, Integer userId);
}
