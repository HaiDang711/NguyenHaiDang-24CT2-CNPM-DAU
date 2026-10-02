package com.dangbooking.tour.repository;

import com.dangbooking.tour.model.Tour;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Integer> {

    List<Tour> findByPlaceContainingIgnoreCase(String place, Sort sort);

    @Query("select distinct t.place from Tour t order by t.place")
    List<String> findDistinctPlaces();
}
