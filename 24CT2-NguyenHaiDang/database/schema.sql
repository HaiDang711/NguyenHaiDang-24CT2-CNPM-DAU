-- Import file này bằng phpMyAdmin (http://localhost/phpmyadmin) trước khi chạy web
CREATE DATABASE IF NOT EXISTS travel_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE travel_app;

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(150) NOT NULL,
  phone VARCHAR(15) NOT NULL UNIQUE,
  email VARCHAR(150) NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE hotels (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  place VARCHAR(100) NOT NULL,
  address VARCHAR(255) NOT NULL,
  description TEXT,
  rating VARCHAR(20),
  price_from INT NOT NULL,
  color_from VARCHAR(20) DEFAULT '#14A098',
  color_to VARCHAR(20) DEFAULT '#0E4749',
  amenities TEXT COMMENT 'phân cách bằng dấu phẩy'
);

CREATE TABLE rooms (
  id INT AUTO_INCREMENT PRIMARY KEY,
  hotel_id INT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(255),
  price INT NOT NULL,
  FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE
);

CREATE TABLE bookings (
  id INT AUTO_INCREMENT PRIMARY KEY,
  booking_code VARCHAR(20) NOT NULL UNIQUE,
  user_id INT NOT NULL,
  hotel_id INT NOT NULL,
  room_id INT NOT NULL,
  guest_name VARCHAR(150) NOT NULL,
  guest_phone VARCHAR(15) NOT NULL,
  guest_email VARCHAR(150),
  payment_method VARCHAR(30) NOT NULL,
  total_price INT NOT NULL,
  status VARCHAR(20) DEFAULT 'confirmed',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (hotel_id) REFERENCES hotels(id),
  FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- Dữ liệu mẫu
INSERT INTO hotels (name, place, address, description, rating, price_from, color_from, color_to, amenities) VALUES
('Fusion Suites Đà Nẵng Beach', 'Đà Nẵng', 'Đường Võ Nguyên Giáp, Ngũ Hành Sơn, Đà Nẵng',
 'Khách sạn bãi biển hiện đại với hồ bơi vô cực nhìn ra biển Mỹ Khê, cách trung tâm thành phố 10 phút di chuyển.',
 '4.8 (320)', 1200000, '#14A098', '#0E4749', 'Hồ bơi vô cực,Wifi miễn phí,Bãi biển riêng,Gym 24/7,Nhà hàng buffet,Bãi đỗ xe miễn phí'),
('Ana Mandara Đà Lạt Villa', 'Đà Lạt', 'Đường Lê Lai, Phường 5, Đà Lạt',
 'Biệt thự phong cách Pháp cổ điển giữa rừng thông, không gian yên tĩnh, gần hồ Xuân Hương.',
 '4.7 (198)', 980000, '#F2A93B', '#DE9226', 'Lò sưởi trong phòng,Wifi miễn phí,Vườn riêng,Nhà hàng Âu - Việt,Đưa đón sân bay,Bãi đỗ xe miễn phí'),
('Salinda Resort Phú Quốc', 'Phú Quốc', 'Bãi Trường, An Thới, Phú Quốc',
 'Resort 5 sao ngay mặt biển Bãi Trường, phòng theo phong cách Đông Dương, gần cáp treo Hòn Thơm.',
 '4.9 (410)', 1650000, '#0E4749', '#0B2B2C', 'Hồ bơi vô cực,Wifi miễn phí,Spa,Bãi biển riêng,3 nhà hàng,Đưa đón sân bay'),
('Little Riverside Hội An', 'Hội An', 'Đường Nguyễn Phúc Chu, phố cổ Hội An',
 'Khách sạn boutique bên sông Hoài, đi bộ 5 phút tới phố cổ, phong cách kiến trúc truyền thống.',
 '4.6 (256)', 850000, '#DCF2EF', '#14A098', 'Hồ bơi ngoài trời,Wifi miễn phí,Thuê xe đạp,Nhà hàng địa phương,Đưa đón phố cổ,Bãi đỗ xe');

INSERT INTO rooms (hotel_id, name, description, price) VALUES
(1, 'Phòng Deluxe hướng biển', '32m² · 1 giường đôi · Ban công', 1200000),
(1, 'Phòng Suite gia đình', '48m² · 2 giường đôi · Bồn tắm', 2100000),
(2, 'Phòng Cozy đơn', '24m² · 1 giường đôi · View vườn', 980000),
(2, 'Villa 2 phòng ngủ', '70m² · 2 phòng ngủ · Sân riêng', 2850000),
(3, 'Phòng Garden view', '40m² · 1 giường đôi · Sân vườn', 1650000),
(3, 'Pool Villa riêng', '85m² · Hồ bơi riêng · 2 phòng ngủ', 4200000),
(4, 'Phòng Superior', '26m² · 1 giường đôi · View sông', 850000),
(4, 'Phòng Family', '38m² · 2 giường đôi', 1450000);

-- ===================== NÂNG CẤP: khu vực quản trị (admin/nhân viên) =====================
-- Nếu bạn đã có database travel_app từ trước, copy đoạn dưới đây dán vào phpMyAdmin (tab SQL) và chạy 1 lần.

ALTER TABLE bookings MODIFY status VARCHAR(20) DEFAULT 'pending';
-- status có 3 giá trị: pending (chờ duyệt) / confirmed (đã duyệt) / cancelled (đã hủy)
-- Nếu bạn có đơn cũ đang là 'confirmed' mà thực ra chưa ai duyệt, có thể chạy thêm dòng dưới để đưa hết về "chờ duyệt":
-- UPDATE bookings SET status = 'pending';

CREATE TABLE IF NOT EXISTS admins (
  id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(150) NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('admin','staff') NOT NULL DEFAULT 'staff',
  created_by INT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tài khoản admin mặc định — username: admin / mật khẩu: admin123
-- ĐỔI MẬT KHẨU NÀY ngay khi có thể (sửa trực tiếp trong phpMyAdmin, bảng admins).
INSERT INTO admins (full_name, username, password_hash, role)
VALUES ('Quản trị viên', 'admin', '$2b$10$x.G6jHJccmzme2kA65NNmeoBPRvvwqWei5NDTjTEFzV7RAHbpn2Yq', 'admin')
ON DUPLICATE KEY UPDATE username = username;

-- ===================== NÂNG CẤP: số khách + phương tiện di chuyển =====================
-- Copy đoạn dưới đây dán vào tab SQL của database travel_app trong phpMyAdmin và chạy 1 lần.

ALTER TABLE bookings
  ADD COLUMN IF NOT EXISTS guest_count INT NOT NULL DEFAULT 1 AFTER guest_email,
  ADD COLUMN IF NOT EXISTS vehicle_request VARCHAR(255) NULL AFTER guest_count,
  ADD COLUMN IF NOT EXISTS assigned_vehicle VARCHAR(255) NULL AFTER vehicle_request;

-- ===================== NÂNG CẤP: ngày khởi hành (đúng luồng đặt Tour) =====================
ALTER TABLE bookings
  ADD COLUMN IF NOT EXISTS departure_date DATE NULL AFTER guest_count;

-- ======================================================================
-- NÂNG CẤP LỚN: đổi tên hotel -> tour toàn bộ + FR-07, FR-09, FR-10, FR-12
-- Copy TOÀN BỘ khối bên dưới, dán vào tab SQL của database `travel_app`
-- trong phpMyAdmin, bấm Go. Chỉ chạy đúng 1 lần.
-- ======================================================================

-- 1) Dọn lỗi cột trạng thái (nếu trước đó bị lỗi cột "Pending" thừa)
ALTER TABLE bookings DROP COLUMN IF EXISTS Pending;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'pending';

-- 2) Đổi tên bảng/cột từ "khách sạn" sang đúng nghiệp vụ "Tour du lịch"
RENAME TABLE hotels TO tours;
RENAME TABLE rooms TO tour_packages;
ALTER TABLE tours CHANGE COLUMN amenities highlights TEXT;
ALTER TABLE tour_packages CHANGE COLUMN hotel_id tour_id INT NOT NULL;
ALTER TABLE bookings CHANGE COLUMN hotel_id tour_id INT NOT NULL;
ALTER TABLE bookings CHANGE COLUMN room_id package_id INT NOT NULL;

-- 3) Cột phục vụ Hủy/Đổi tour (FR-09) và Thanh toán (FR-10)
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(255) NULL AFTER status;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) NOT NULL DEFAULT 'unpaid' AFTER cancel_reason;

-- 4) Bảng đánh giá dịch vụ (FR-12)
CREATE TABLE IF NOT EXISTS reviews (
  id INT AUTO_INCREMENT PRIMARY KEY,
  booking_id INT NOT NULL UNIQUE,
  user_id INT NOT NULL,
  tour_id INT NOT NULL,
  rating INT NOT NULL,
  comment TEXT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (tour_id) REFERENCES tours(id)
);
