package com.dangbooking.tour.controller;

import com.dangbooking.tour.model.*;
import com.dangbooking.tour.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

// Thay the toan bo backend/admin/api/*.php cu. Giu nguyen URL de frontend/admin khong phai sua.
@RestController
@RequestMapping("/backend/admin/api")
public class AdminController {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final TourPackageRepository packageRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder encoder;

    public AdminController(AdminRepository adminRepository, UserRepository userRepository,
                           TourRepository tourRepository, TourPackageRepository packageRepository,
                           BookingRepository bookingRepository, PasswordEncoder encoder) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
        this.packageRepository = packageRepository;
        this.bookingRepository = bookingRepository;
        this.encoder = encoder;
    }

    // ---------- tien ich ----------
    private ResponseEntity<Map<String, Object>> err(HttpStatus s, String msg) {
        Map<String, Object> b = new HashMap<>();
        b.put("error", msg);
        return ResponseEntity.status(s).body(b);
    }

    /** Tra ve null neu hop le; nguoc lai tra ve response loi (401/403). */
    private ResponseEntity<Map<String, Object>> check(HttpSession session, String... roles) {
        Object id = session.getAttribute("admin_id");
        Object role = session.getAttribute("admin_role");
        if (id == null || role == null) {
            return err(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập tài khoản quản trị.");
        }
        if (!Arrays.asList(roles).contains(String.valueOf(role))) {
            return err(HttpStatus.FORBIDDEN, "Tài khoản của bạn không có quyền thực hiện thao tác này.");
        }
        return null;
    }

    private String s(Object o) { return o == null ? "" : String.valueOf(o).trim(); }

    private Integer i(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(o).trim()); } catch (NumberFormatException e) { return null; }
    }

    // Goi y phuong tien theo so khach (giong vehicle_logic.php)
    private String suggestVehicle(int guestCount, String customRequest) {
        guestCount = Math.max(1, guestCount);
        if (customRequest != null && !customRequest.isBlank()) {
            return "Theo yêu cầu riêng của khách: \"" + customRequest + "\" (đã trả đủ chi phí phát sinh nếu có)";
        }
        if (guestCount <= 4) return "1 xe 4 chỗ";
        if (guestCount <= 7) {
            return guestCount == 7
                    ? "1 xe 7 chỗ (đủ 7 người, phù hợp nếu hành lý gọn nhẹ)"
                    : "1 xe 7 chỗ (chở " + guestCount + " người, còn dư chỗ để hành lý)";
        }
        if (guestCount <= 11) return "1 xe Limousine (9-11 chỗ)";
        int buses40 = (int) Math.ceil(guestCount / 40.0);
        int buses20 = (int) Math.ceil(guestCount / 20.0);
        if (buses40 == 1) {
            return "Gợi ý: 1 xe buýt 40 chỗ (chở vừa " + guestCount + " khách) — CẦN xác nhận với tài xế/người phụ trách";
        }
        return "Gợi ý: khoảng " + buses20 + " xe buýt (~20 khách/xe) hoặc " + buses40
                + " xe buýt 40 chỗ — linh hoạt tùy tình huống, CẦN nhân viên xác nhận thực tế trước khi duyệt";
    }

    // ---------- dang nhap / dang xuat / me ----------
    @PostMapping("/login.php")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> data, HttpSession session) {
        String username = s(data.get("username"));
        String password = data.get("password") == null ? "" : String.valueOf(data.get("password"));
        if (username.isEmpty() || password.isEmpty()) {
            return err(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên đăng nhập và mật khẩu.");
        }
        Admin admin = adminRepository.findByUsername(username).orElse(null);
        if (admin == null || !encoder.matches(password, admin.getPasswordHash())) {
            return err(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng.");
        }
        session.setAttribute("admin_id", admin.getId());
        session.setAttribute("admin_role", admin.getRole());

        Map<String, Object> a = new LinkedHashMap<>();
        a.put("id", admin.getId());
        a.put("full_name", admin.getFullName());
        a.put("username", admin.getUsername());
        a.put("role", admin.getRole());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("admin", a);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout.php")
    public Map<String, Object> logout(HttpSession session) {
        session.removeAttribute("admin_id");
        session.removeAttribute("admin_role");
        Map<String, Object> b = new HashMap<>();
        b.put("success", true);
        return b;
    }

    @GetMapping("/me.php")
    public Map<String, Object> me(HttpSession session) {
        Map<String, Object> body = new LinkedHashMap<>();
        Object id = session.getAttribute("admin_id");
        Admin admin = id == null ? null : adminRepository.findById((Integer) id).orElse(null);
        if (admin == null) {
            body.put("logged_in", false);
            return body;
        }
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("id", admin.getId());
        a.put("full_name", admin.getFullName());
        a.put("username", admin.getUsername());
        a.put("role", admin.getRole());
        body.put("logged_in", true);
        body.put("admin", a);
        return body;
    }

    // ---------- thong ke (chi admin) ----------
    @GetMapping("/stats.php")
    public ResponseEntity<?> stats(HttpSession session) {
        ResponseEntity<Map<String, Object>> denied = check(session, "admin");
        if (denied != null) return denied;

        List<Booking> all = bookingRepository.findAll();
        long revenue = all.stream().filter(b -> "confirmed".equals(b.getStatus()) && "paid".equals(b.getPaymentStatus()))
                .mapToLong(Booking::getTotalPrice).sum();
        long pending = all.stream().filter(b -> "pending".equals(b.getStatus())).count();
        long confirmed = all.stream().filter(b -> "confirmed".equals(b.getStatus())).count();
        long cancelled = all.stream().filter(b -> "cancelled".equals(b.getStatus())).count();

        List<Map<String, Object>> top = tourRepository.findAll().stream().map(t -> {
            List<Booking> bs = all.stream()
                    .filter(b -> b.getTourId().equals(t.getId()) && "confirmed".equals(b.getStatus()) && "paid".equals(b.getPaymentStatus()))
                    .collect(Collectors.toList());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", t.getName());
            m.put("bookings", bs.size());
            m.put("revenue", bs.stream().mapToLong(Booking::getTotalPrice).sum());
            return m;
        }).sorted((a, b) -> Long.compare((Long) b.get("revenue"), (Long) a.get("revenue")))
          .limit(5).collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("total_revenue", revenue);
        body.put("pending_count", pending);
        body.put("confirmed_count", confirmed);
        body.put("cancelled_count", cancelled);
        body.put("user_count", userRepository.count());
        body.put("staff_count", adminRepository.count());
        body.put("top_tours", top);
        return ResponseEntity.ok(body);
    }

    // ---------- don dat tour (admin + staff) ----------
    @GetMapping("/bookings.php")
    public ResponseEntity<?> bookings(HttpSession session) {
        ResponseEntity<Map<String, Object>> denied = check(session, "admin", "staff");
        if (denied != null) return denied;

        List<Map<String, Object>> rows = bookingRepository.findAll(Sort.by("createdAt").descending()).stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("booking_code", b.getBookingCode());
            m.put("guest_name", b.getGuestName());
            m.put("guest_phone", b.getGuestPhone());
            m.put("payment_method", b.getPaymentMethod());
            m.put("payment_status", b.getPaymentStatus());
            m.put("total_price", b.getTotalPrice());
            m.put("status", b.getStatus());
            m.put("cancel_reason", b.getCancelReason());
            m.put("created_at", b.getCreatedAt());
            m.put("guest_count", b.getGuestCount());
            m.put("departure_date", b.getDepartureDate());
            m.put("vehicle_request", b.getVehicleRequest());
            m.put("assigned_vehicle", b.getAssignedVehicle());
            Tour t = tourRepository.findById(b.getTourId()).orElse(null);
            TourPackage p = packageRepository.findById(b.getPackageId()).orElse(null);
            m.put("tour_name", t != null ? t.getName() : null);
            m.put("package_name", p != null ? p.getName() : null);
            m.put("suggested_vehicle", suggestVehicle(b.getGuestCount(), b.getVehicleRequest()));
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("bookings", rows);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/bookings.php")
    public ResponseEntity<Map<String, Object>> updateBooking(@RequestBody Map<String, Object> data, HttpSession session) {
        ResponseEntity<Map<String, Object>> denied = check(session, "admin", "staff");
        if (denied != null) return denied;

        Integer id = i(data.get("id"));
        String action = s(data.get("action"));
        String vehicle = s(data.get("assigned_vehicle"));
        if (id == null || action.isEmpty()) return err(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ.");

        Booking b = bookingRepository.findById(id).orElse(null);
        if (b == null) return err(HttpStatus.NOT_FOUND, "Không tìm thấy đơn.");

        switch (action) {
            case "confirm" -> {
                b.setStatus("confirmed");
                b.setAssignedVehicle(vehicle.isEmpty() ? null : vehicle);
            }
            case "cancel" -> b.setStatus("cancelled");
            case "reject_cancel" -> {
                b.setStatus("confirmed");
                b.setCancelReason(null);
            }
            default -> { return err(HttpStatus.BAD_REQUEST, "Hành động không hợp lệ."); }
        }
        bookingRepository.save(b);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("status", b.getStatus());
        return ResponseEntity.ok(body);
    }

    // ---------- quan ly tour (admin + staff) ----------
    @GetMapping("/tours.php")
    public ResponseEntity<?> tours(HttpSession session) {
        ResponseEntity<Map<String, Object>> denied = check(session, "admin", "staff");
        if (denied != null) return denied;

        List<Map<String, Object>> list = tourRepository.findAll(Sort.by("id").descending()).stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("place", t.getPlace());
            m.put("price_from", t.getPriceFrom());
            m.put("packages", packageRepository.findByTourId(t.getId()).stream().map(p -> {
                Map<String, Object> pm = new LinkedHashMap<>();
                pm.put("id", p.getId());
                pm.put("name", p.getName());
                pm.put("description", p.getDescription());
                pm.put("price", p.getPrice());
                return pm;
            }).collect(Collectors.toList()));
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tours", list);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/tours.php")
    public ResponseEntity<Map<String, Object>> addTourOrPackage(@RequestBody Map<String, Object> data, HttpSession session) {
        ResponseEntity<Map<String, Object>> denied = check(session, "admin", "staff");
        if (denied != null) return denied;

        String entity = s(data.get("entity"));
        Map<String, Object> body = new LinkedHashMap<>();

        if ("tour".equals(entity)) {
            String name = s(data.get("name"));
            String place = s(data.get("place"));
            Integer priceFrom = i(data.get("price_from"));
            if (name.isEmpty() || place.isEmpty() || priceFrom == null || priceFrom == 0) {
                return err(HttpStatus.BAD_REQUEST, "Vui lòng nhập đủ tên, địa điểm và giá.");
            }
            Tour t = new Tour();
            t.setName(name);
            t.setPlace(place);
            t.setAddress(s(data.get("address")));
            t.setDescription(s(data.get("description")));
            String rating = s(data.get("rating"));
            t.setRating(rating.isEmpty() ? "5.0 (0)" : rating);
            t.setPriceFrom(priceFrom);
            t.setHighlights(s(data.get("highlights")));
            tourRepository.save(t);
            body.put("success", true);
            body.put("id", t.getId());
            return ResponseEntity.ok(body);
        }

        if ("package".equals(entity)) {
            Integer tourId = i(data.get("tour_id"));
            String name = s(data.get("name"));
            Integer price = i(data.get("price"));
            if (tourId == null || tourId == 0 || name.isEmpty() || price == null || price == 0) {
                return err(HttpStatus.BAD_REQUEST, "Vui lòng nhập đủ thông tin gói tour.");
            }
            TourPackage p = new TourPackage();
            p.setTourId(tourId);
            p.setName(name);
            p.setDescription(s(data.get("description")));
            p.setPrice(price);
            packageRepository.save(p);
            body.put("success", true);
            body.put("id", p.getId());
            return ResponseEntity.ok(body);
        }

        return err(HttpStatus.BAD_REQUEST, "entity phải là \"tour\" hoặc \"package\".");
    }

    // ---------- khach hang (admin + staff) ----------
    @GetMapping("/customers.php")
    public ResponseEntity<?> customers(HttpSession session) {
        ResponseEntity<Map<String, Object>> denied = check(session, "admin", "staff");
        if (denied != null) return denied;

        List<Map<String, Object>> rows = userRepository.findAllByOrderByCreatedAtDesc().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("full_name", u.getFullName());
            m.put("phone", u.getPhone());
            m.put("email", u.getEmail());
            m.put("created_at", u.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("users", rows);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/customers.php")
    public ResponseEntity<Map<String, Object>> createCustomer(@RequestBody Map<String, Object> data, HttpSession session) {
        ResponseEntity<Map<String, Object>> denied = check(session, "admin", "staff");
        if (denied != null) return denied;

        String fullName = s(data.get("full_name"));
        String phone = s(data.get("phone"));
        String email = s(data.get("email"));
        String password = data.get("password") == null ? "" : String.valueOf(data.get("password"));
        if (fullName.isEmpty() || phone.isEmpty() || password.length() < 6) {
            return err(HttpStatus.BAD_REQUEST, "Vui lòng nhập đủ họ tên, số điện thoại và mật khẩu (tối thiểu 6 ký tự).");
        }
        if (userRepository.existsByPhone(phone)) {
            return err(HttpStatus.CONFLICT, "Số điện thoại này đã có tài khoản.");
        }
        User u = new User();
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setEmail(email.isEmpty() ? null : email);
        u.setPasswordHash(encoder.encode(password));
        userRepository.save(u);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/reset-password.php")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, Object> data, HttpSession session) {
        ResponseEntity<Map<String, Object>> denied = check(session, "admin", "staff");
        if (denied != null) return denied;

        Integer userId = i(data.get("user_id"));
        String newPassword = data.get("new_password") == null ? "" : String.valueOf(data.get("new_password"));
        if (userId == null || userId == 0 || newPassword.length() < 6) {
            return err(HttpStatus.BAD_REQUEST, "Vui lòng chọn người dùng và nhập mật khẩu mới (tối thiểu 6 ký tự).");
        }
        User u = userRepository.findById(userId).orElse(null);
        if (u == null) return err(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng.");

        u.setPasswordHash(encoder.encode(newPassword));
        userRepository.save(u);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "Đã đặt lại mật khẩu cho " + u.getFullName() + ".");
        return ResponseEntity.ok(body);
    }

    // ---------- nhan vien (chi admin) ----------
    @GetMapping("/staff.php")
    public ResponseEntity<?> staff(HttpSession session) {
        ResponseEntity<Map<String, Object>> denied = check(session, "admin");
        if (denied != null) return denied;

        List<Map<String, Object>> rows = adminRepository.findAllByOrderByCreatedAtDesc().stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("full_name", a.getFullName());
            m.put("username", a.getUsername());
            m.put("role", a.getRole());
            m.put("created_at", a.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("staff", rows);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/staff.php")
    public ResponseEntity<Map<String, Object>> createStaff(@RequestBody Map<String, Object> data, HttpSession session) {
        ResponseEntity<Map<String, Object>> denied = check(session, "admin");
        if (denied != null) return denied;

        String fullName = s(data.get("full_name"));
        String username = s(data.get("username"));
        String password = data.get("password") == null ? "" : String.valueOf(data.get("password"));
        String role = "admin".equals(s(data.get("role"))) ? "admin" : "staff";
        if (fullName.isEmpty() || username.isEmpty() || password.length() < 6) {
            return err(HttpStatus.BAD_REQUEST, "Vui lòng nhập đủ họ tên, tên đăng nhập và mật khẩu (tối thiểu 6 ký tự).");
        }
        if (adminRepository.existsByUsername(username)) {
            return err(HttpStatus.CONFLICT, "Tên đăng nhập này đã tồn tại.");
        }
        Admin a = new Admin();
        a.setFullName(fullName);
        a.setUsername(username);
        a.setPasswordHash(encoder.encode(password));
        a.setRole(role);
        a.setCreatedBy((Integer) session.getAttribute("admin_id"));
        adminRepository.save(a);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        return ResponseEntity.ok(body);
    }
}
