package com.dangbooking.tour.repository;

import com.dangbooking.tour.model.TourPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourPackageRepository extends JpaRepository<TourPackage, Integer> {
    List<TourPackage> findByTourId(Integer tourId);
    Optional<TourPackage> findByIdAndTourId(Integer id, Integer tourId);
}
