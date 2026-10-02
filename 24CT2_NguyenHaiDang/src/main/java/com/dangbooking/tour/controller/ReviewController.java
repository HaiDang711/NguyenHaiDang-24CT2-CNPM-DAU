package com.dangbooking.tour.controller;

import com.dangbooking.tour.model.Booking;
import com.dangbooking.tour.model.Review;
import com.dangbooking.tour.repository.BookingRepository;
import com.dangbooking.tour.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/backend/api")
public class ReviewController {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    public ReviewController(BookingRepository bookingRepository, ReviewRepository reviewRepository) {
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }

    // FR-12: chi cho danh gia 1 lan/don, va chi khi don da duoc duyet
    @PostMapping("/review.php")
    public ResponseEntity<Map<String, Object>> review(@RequestBody Map<String, Object> data, HttpSession session) {
        Object userIdObj = session.getAttribute("user_id");
        if (userIdObj == null) {
            return error(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập.");
        }
        Integer userId = (Integer) userIdObj;

        Integer bookingId = data.get("booking_id") == null ? null
                : Integer.valueOf(String.valueOf(data.get("booking_id")));
        Integer rating = data.get("rating") == null ? 0
                : Integer.valueOf(String.valueOf(data.get("rating")));
        String comment = data.get("comment") == null ? "" : String.valueOf(data.get("comment")).trim();

        if (rating < 1 || rating > 5) {
            return error(HttpStatus.BAD_REQUEST, "Vui lòng chọn số sao từ 1 đến 5.");
        }

        Booking booking = bookingId == null ? null : bookingRepository.findByIdAndUserId(bookingId, userId).orElse(null);
        if (booking == null) {
            return error(HttpStatus.NOT_FOUND, "Không tìm thấy đơn đặt tour.");
        }
        if (!"confirmed".equals(booking.getStatus())) {
            return error(HttpStatus.BAD_REQUEST, "Chỉ có thể đánh giá sau khi đơn đã được duyệt.");
        }
        if (reviewRepository.findByBookingId(bookingId).isPresent()) {
            return error(HttpStatus.CONFLICT, "Bạn đã đánh giá đơn này rồi.");
        }

        Review review = new Review();
        review.setBookingId(bookingId);
        review.setUserId(userId);
        review.setTourId(booking.getTourId());
        review.setRating(rating);
        review.setComment(comment.isEmpty() ? null : comment);
        reviewRepository.save(review);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Cảm ơn bạn đã đánh giá!");
        return ResponseEntity.ok(body);
    }
}
