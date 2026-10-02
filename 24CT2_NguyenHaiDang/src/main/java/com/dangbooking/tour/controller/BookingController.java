package com.dangbooking.tour.controller;

import com.dangbooking.tour.model.Booking;
import com.dangbooking.tour.model.Tour;
import com.dangbooking.tour.model.TourPackage;
import com.dangbooking.tour.repository.BookingRepository;
import com.dangbooking.tour.repository.ReviewRepository;
import com.dangbooking.tour.repository.TourPackageRepository;
import com.dangbooking.tour.repository.TourRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/backend/api")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final TourPackageRepository tourPackageRepository;
    private final ReviewRepository reviewRepository;

    public BookingController(BookingRepository bookingRepository,
                              TourRepository tourRepository,
                              TourPackageRepository tourPackageRepository,
                              ReviewRepository reviewRepository) {
        this.bookingRepository = bookingRepository;
        this.tourRepository = tourRepository;
        this.tourPackageRepository = tourPackageRepository;
        this.reviewRepository = reviewRepository;
    }

    private Integer currentUserId(HttpSession session) {
        Object v = session.getAttribute("user_id");
        return v == null ? null : (Integer) v;
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }

    @PostMapping("/book.php")
    public ResponseEntity<Map<String, Object>> book(@RequestBody Map<String, Object> data, HttpSession session) {
        Integer userId = currentUserId(session);
        if (userId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập trước khi đặt tour.");
        }

        Integer tourId = asInt(data.get("tour_id"));
        Integer packageId = asInt(data.get("package_id"));
        String name = str(data.get("guest_name")).trim();
        String phone = str(data.get("guest_phone")).trim();
        String email = str(data.get("guest_email")).trim();
        String payment = str(data.getOrDefault("payment_method", "Thanh toán khi nhận phòng")).trim();
        Integer rawGuestCount = asInt(data.get("guest_count"));
        int guestCount = Math.max(1, rawGuestCount == null ? 1 : rawGuestCount);
        String departureDateStr = str(data.get("departure_date")).trim();
        String vehicleRequest = str(data.get("vehicle_request")).trim();

        if (tourId == null || packageId == null || name.isEmpty() || phone.isEmpty() || departureDateStr.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "Vui lòng chọn ngày khởi hành và điền đủ thông tin đặt tour.");
        }

        LocalDate departureDate;
        try {
            departureDate = LocalDate.parse(departureDateStr);
        } catch (DateTimeParseException e) {
            return error(HttpStatus.BAD_REQUEST, "Ngày khởi hành không hợp lệ.");
        }

        TourPackage pkg = tourPackageRepository.findByIdAndTourId(packageId, tourId).orElse(null);
        if (pkg == null) {
            return error(HttpStatus.NOT_FOUND, "Không tìm thấy gói tour.");
        }

        String code = "DangBooking" + (100000 + new Random().nextInt(900000));

        Booking booking = new Booking();
        booking.setBookingCode(code);
        booking.setUserId(userId);
        booking.setTourId(tourId);
        booking.setPackageId(packageId);
        booking.setGuestName(name);
        booking.setGuestPhone(phone);
        booking.setGuestEmail(email.isEmpty() ? null : email);
        booking.setGuestCount(guestCount);
        booking.setDepartureDate(departureDate);
        booking.setVehicleRequest(vehicleRequest.isEmpty() ? null : vehicleRequest);
        booking.setPaymentMethod(payment);
        booking.setPaymentStatus("unpaid");
        booking.setTotalPrice(pkg.getPrice());
        booking.setStatus("pending");
        bookingRepository.save(booking);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("booking_code", code);
        body.put("total_price", pkg.getPrice());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/bookings.php")
    public ResponseEntity<Map<String, Object>> bookings(HttpSession session) {
        Integer userId = currentUserId(session);
        if (userId == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("error", "Bạn chưa đăng nhập.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        List<Booking> list = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = list.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("booking_code", b.getBookingCode());
            m.put("user_id", b.getUserId());
            m.put("tour_id", b.getTourId());
            m.put("package_id", b.getPackageId());
            m.put("guest_name", b.getGuestName());
            m.put("guest_phone", b.getGuestPhone());
            m.put("guest_email", b.getGuestEmail());
            m.put("guest_count", b.getGuestCount());
            m.put("departure_date", b.getDepartureDate());
            m.put("vehicle_request", b.getVehicleRequest());
            m.put("assigned_vehicle", b.getAssignedVehicle());
            m.put("payment_method", b.getPaymentMethod());
            m.put("payment_status", b.getPaymentStatus());
            m.put("total_price", b.getTotalPrice());
            m.put("status", b.getStatus());
            m.put("cancel_reason", b.getCancelReason());
            m.put("created_at", b.getCreatedAt());

            Tour tour = tourRepository.findById(b.getTourId()).orElse(null);
            m.put("tour_name", tour != null ? tour.getName() : null);
            m.put("tour_place", tour != null ? tour.getPlace() : null);
            m.put("tour_address", tour != null ? tour.getAddress() : null);

            TourPackage pkg = tourPackageRepository.findById(b.getPackageId()).orElse(null);
            m.put("package_name", pkg != null ? pkg.getName() : null);

            Integer reviewId = reviewRepository.findByBookingId(b.getId()).map(r -> r.getId()).orElse(null);
            m.put("review_id", reviewId);

            return m;
        }).collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("current_user_id", userId);
        body.put("count", result.size());
        body.put("bookings", result);
        return ResponseEntity.ok(body);
    }

    // FR-09: khach gui yeu cau huy hoac thay doi tour
    @PostMapping("/cancel-request.php")
    public ResponseEntity<Map<String, Object>> cancelRequest(@RequestBody Map<String, Object> data, HttpSession session) {
        Integer userId = currentUserId(session);
        if (userId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập.");
        }
        Integer bookingId = asInt(data.get("booking_id"));
        String reason = str(data.get("reason")).trim();

        if (bookingId == null) {
            return error(HttpStatus.BAD_REQUEST, "Thiếu mã đơn.");
        }

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId).orElse(null);
        if (booking == null) {
            return error(HttpStatus.NOT_FOUND, "Không tìm thấy đơn đặt tour.");
        }
        if (!List.of("pending", "confirmed").contains(booking.getStatus())) {
            return error(HttpStatus.BAD_REQUEST, "Đơn này không thể gửi yêu cầu hủy/đổi ở trạng thái hiện tại.");
        }
        if (booking.getDepartureDate() != null && booking.getDepartureDate().isBefore(LocalDate.now())) {
            return error(HttpStatus.BAD_REQUEST, "Tour đã khởi hành, không thể gửi yêu cầu hủy/đổi.");
        }

        booking.setStatus("cancel_requested");
        booking.setCancelReason(reason.isEmpty() ? null : reason);
        bookingRepository.save(booking);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Đã gửi yêu cầu hủy/đổi tour, nhân viên sẽ xử lý sớm.");
        return ResponseEntity.ok(body);
    }

    // FR-10: Thanh toan (mo phong, chua noi cong thanh toan that)
    @PostMapping("/pay.php")
    public ResponseEntity<Map<String, Object>> pay(@RequestBody Map<String, Object> data, HttpSession session) {
        Integer userId = currentUserId(session);
        if (userId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập.");
        }
        Integer bookingId = asInt(data.get("booking_id"));
        Booking booking = bookingId == null ? null : bookingRepository.findByIdAndUserId(bookingId, userId).orElse(null);
        if (booking == null) {
            return error(HttpStatus.NOT_FOUND, "Không tìm thấy đơn đặt tour.");
        }
        if (!"confirmed".equals(booking.getStatus())) {
            return error(HttpStatus.BAD_REQUEST, "Chỉ có thể thanh toán khi đơn đã được nhân viên duyệt.");
        }
        if ("paid".equals(booking.getPaymentStatus())) {
            return error(HttpStatus.BAD_REQUEST, "Đơn này đã được thanh toán rồi.");
        }

        booking.setPaymentStatus("paid");
        bookingRepository.save(booking);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Thanh toán thành công (mô phỏng).");
        return ResponseEntity.ok(body);
    }

    private Integer asInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(o).trim()); } catch (NumberFormatException e) { return null; }
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
