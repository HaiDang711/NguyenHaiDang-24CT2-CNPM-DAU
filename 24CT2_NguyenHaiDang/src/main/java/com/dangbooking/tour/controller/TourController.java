package com.dangbooking.tour.controller;

import com.dangbooking.tour.model.Review;
import com.dangbooking.tour.model.Tour;
import com.dangbooking.tour.model.TourPackage;
import com.dangbooking.tour.model.User;
import com.dangbooking.tour.repository.ReviewRepository;
import com.dangbooking.tour.repository.TourPackageRepository;
import com.dangbooking.tour.repository.TourRepository;
import com.dangbooking.tour.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/backend/api")
public class TourController {

    private final TourRepository tourRepository;
    private final TourPackageRepository tourPackageRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public TourController(TourRepository tourRepository,
                           TourPackageRepository tourPackageRepository,
                           ReviewRepository reviewRepository,
                           UserRepository userRepository) {
        this.tourRepository = tourRepository;
        this.tourPackageRepository = tourPackageRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    // FR-07: loc theo dia diem + sap xep theo gia
    @GetMapping("/tours.php")
    public Map<String, Object> tours(@RequestParam(required = false, defaultValue = "") String place,
                                      @RequestParam(required = false, defaultValue = "") String sort) {
        Sort sortSpec = switch (sort) {
            case "price_asc" -> Sort.by("priceFrom").ascending();
            case "price_desc" -> Sort.by("priceFrom").descending();
            default -> Sort.by("id").descending();
        };

        List<Tour> tours = place.isBlank()
                ? tourRepository.findAll(sortSpec)
                : tourRepository.findByPlaceContainingIgnoreCase(place, sortSpec);

        List<Map<String, Object>> tourList = tours.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("place", t.getPlace());
            m.put("rating", t.getRating());
            m.put("price_from", t.getPriceFrom());
            m.put("color_from", t.getColorFrom());
            m.put("color_to", t.getColorTo());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tours", tourList);
        body.put("places", tourRepository.findDistinctPlaces());
        return body;
    }

    @GetMapping("/tour.php")
    public ResponseEntity<Map<String, Object>> tourDetail(@RequestParam(required = false, defaultValue = "0") Integer id) {
        if (id == null || id == 0) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Thiếu id tour.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
        Tour tour = tourRepository.findById(id).orElse(null);
        if (tour == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Không tìm thấy tour.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

        Map<String, Object> tourMap = new LinkedHashMap<>();
        tourMap.put("id", tour.getId());
        tourMap.put("name", tour.getName());
        tourMap.put("place", tour.getPlace());
        tourMap.put("address", tour.getAddress());
        tourMap.put("description", tour.getDescription());
        tourMap.put("rating", tour.getRating());
        tourMap.put("price_from", tour.getPriceFrom());
        tourMap.put("color_from", tour.getColorFrom());
        tourMap.put("color_to", tour.getColorTo());
        List<String> highlights = (tour.getHighlights() == null || tour.getHighlights().isBlank())
                ? List.of()
                : Arrays.asList(tour.getHighlights().split(","));
        tourMap.put("highlights", highlights);

        List<TourPackage> packages = tourPackageRepository.findByTourId(id);
        List<Map<String, Object>> packageList = packages.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("description", p.getDescription());
            m.put("price", p.getPrice());
            return m;
        }).collect(Collectors.toList());

        // FR-12: danh gia dich vu
        List<Review> reviews = reviewRepository.findByTourIdOrderByCreatedAtDesc(id);
        int total = reviews.size();
        Double average = total == 0 ? null
                : Math.round(reviews.stream().mapToInt(Review::getRating).average().orElse(0) * 10.0) / 10.0;

        List<Map<String, Object>> reviewList = reviews.stream().limit(20).map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rating", r.getRating());
            m.put("comment", r.getComment());
            m.put("created_at", r.getCreatedAt());
            User reviewer = userRepository.findById(r.getUserId()).orElse(null);
            m.put("full_name", reviewer != null ? reviewer.getFullName() : null);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> reviewSummary = new LinkedHashMap<>();
        reviewSummary.put("total", total);
        reviewSummary.put("average", average);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tour", tourMap);
        body.put("packages", packageList);
        body.put("review_summary", reviewSummary);
        body.put("reviews", reviewList);
        return ResponseEntity.ok(body);
    }
}
