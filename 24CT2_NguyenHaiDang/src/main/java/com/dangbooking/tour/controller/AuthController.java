package com.dangbooking.tour.controller;

import com.dangbooking.tour.model.User;
import com.dangbooking.tour.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/backend/api")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }

    @PostMapping("/register.php")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> data) {
        String fullName = String.valueOf(data.getOrDefault("full_name", "")).trim();
        String phone = String.valueOf(data.getOrDefault("phone", "")).trim();
        String email = String.valueOf(data.getOrDefault("email", "")).trim();
        String password = String.valueOf(data.getOrDefault("password", ""));

        if (fullName.isEmpty() || phone.isEmpty() || password.length() < 6) {
            return error(HttpStatus.BAD_REQUEST,
                "Vui lòng nhập đủ họ tên, số điện thoại và mật khẩu (tối thiểu 6 ký tự).");
        }
        if (!phone.matches("^[0-9]{9,10}$")) {
            return error(HttpStatus.BAD_REQUEST, "Số điện thoại không hợp lệ.");
        }
        if (userRepository.existsByPhone(phone)) {
            return error(HttpStatus.CONFLICT, "Số điện thoại này đã được đăng ký.");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email.isEmpty() ? null : email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Đăng ký thành công.");
        return ResponseEntity.ok(body);
    }

    @PostMapping("/login.php")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> data, HttpSession session) {
        String identifier = String.valueOf(data.getOrDefault("identifier", "")).trim();
        String password = String.valueOf(data.getOrDefault("password", ""));

        if (identifier.isEmpty() || password.isEmpty()) {
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("error", "Vui lòng nhập số điện thoại/email và mật khẩu.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        User user = userRepository.findByPhoneOrEmail(identifier, identifier).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("error", "Số điện thoại/email hoặc mật khẩu không đúng.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        // Luu id user vao session (tuong duong $_SESSION['user_id'] cua PHP)
        session.setAttribute("user_id", user.getId());

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Đăng nhập thành công.");
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("full_name", user.getFullName());
        userMap.put("phone", user.getPhone());
        userMap.put("email", user.getEmail());
        body.put("user", userMap);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout.php")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Đăng xuất thành công.");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me.php")
    public ResponseEntity<Map<String, Object>> me(HttpSession session) {
        Object userIdObj = session.getAttribute("user_id");
        Map<String, Object> body = new HashMap<>();
        if (userIdObj == null) {
            body.put("logged_in", false);
            body.put("user", null);
            return ResponseEntity.ok(body);
        }
        User user = userRepository.findById((Integer) userIdObj).orElse(null);
        if (user == null) {
            session.removeAttribute("user_id");
            body.put("logged_in", false);
            body.put("user", null);
            return ResponseEntity.ok(body);
        }
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("full_name", user.getFullName());
        userMap.put("phone", user.getPhone());
        userMap.put("email", user.getEmail());
        userMap.put("created_at", user.getCreatedAt());
        body.put("logged_in", true);
        body.put("user", userMap);
        return ResponseEntity.ok(body);
    }
}
