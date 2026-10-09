-- CHAY FILE NAY 1 LAN DUY NHAT, SAU KHI da khoi dong app Spring Boot it nhat 1 lan
-- (de Hibernate tu tao san cac bang tours, tour_packages, users, bookings, reviews).
-- Mo phpMyAdmin (http://localhost/phpmyadmin) -> chon database travel_app_java -> tab SQL -> dan toan bo noi dung nay -> Go.

USE travel_app_java;

INSERT INTO tours (name, place, address, description, rating, price_from, color_from, color_to, highlights) VALUES
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

INSERT INTO tour_packages (tour_id, name, description, price) VALUES
(1, 'Phòng Deluxe hướng biển', '32m² · 1 giường đôi · Ban công', 1200000),
(1, 'Phòng Suite gia đình', '48m² · 2 giường đôi · Bồn tắm', 2100000),
(2, 'Phòng Cozy đơn', '24m² · 1 giường đôi · View vườn', 980000),
(2, 'Villa 2 phòng ngủ', '70m² · 2 phòng ngủ · Sân riêng', 2850000),
(3, 'Phòng Garden view', '40m² · 1 giường đôi · Sân vườn', 1650000),
(3, 'Pool Villa riêng', '85m² · Hồ bơi riêng · 2 phòng ngủ', 4200000),
(4, 'Phòng Superior', '26m² · 1 giường đôi · View sông', 850000),
(4, 'Phòng Family', '38m² · 2 giường đôi', 1450000);
