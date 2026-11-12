package com.dangbooking.tour.config;

import com.dangbooking.tour.model.Admin;
import com.dangbooking.tour.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

// Tu tao tai khoan admin mac dinh (admin / admin123) neu bang admins dang trong.
// NHO doi mat khau nay sau khi nop bai / khi dung that.
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner createDefaultAdmin(AdminRepository adminRepository, PasswordEncoder encoder) {
        return args -> {
            if (adminRepository.count() == 0) {
                Admin a = new Admin();
                a.setFullName("Quản trị viên");
                a.setUsername("admin");
                a.setPasswordHash(encoder.encode("admin123"));
                a.setRole("admin");
                adminRepository.save(a);
            }
        };
    }
}
