# DangBooking — bản Spring Boot (chuyển từ PHP)

## Đã làm được (giống PHP)
- Đăng ký / đăng nhập / đăng xuất / kiểm tra phiên đăng nhập (dùng session, giống `$_SESSION` của PHP)
- Danh sách tour + lọc theo địa điểm + sắp xếp theo giá (FR-07)
- Xem chi tiết 1 tour (gói tour, đánh giá trung bình, danh sách đánh giá)
- Đặt tour (yêu cầu đăng nhập)
- Xem danh sách đơn đã đặt của mình
- Gửi yêu cầu huỷ/đổi tour (FR-09)
- Thanh toán mô phỏng (FR-10) — chưa nối cổng thanh toán thật, y hệt bản PHP
- Đánh giá dịch vụ sau khi đơn được duyệt (FR-12)

## CHƯA làm (để sau, nếu cần)
- Toàn bộ khu vực quản trị (admin/nhân viên): đăng nhập admin, duyệt đơn, quản lý tour/nhân viên/khách hàng, thống kê.
  → Các trang `frontend/admin/*.html` vẫn còn trong project (không bị xoá), nhưng API `backend/admin/api/*` CHƯA được viết lại, nên các trang đó sẽ không hoạt động cho tới khi làm tiếp.

## Cách các URL hoạt động
Để **không phải sửa gì code HTML/JS cũ**, các endpoint Java được đặt đúng tên y hệt file PHP cũ,
ví dụ `/backend/api/login.php`, `/backend/api/tours.php`... — về bản chất đây chỉ là tên đường dẫn (URL),
không phải file PHP thật, do Spring Boot xử lý.

## Cài đặt & chạy trong Eclipse
1. Giải nén project này, import như các lần trước:
   **File > Import > Maven > Existing Maven Projects** → trỏ tới thư mục `demo` (chứa `pom.xml`).
2. Mở **XAMPP Control Panel**, bấm **Start** dòng **MySQL** (không cần Apache, vì giờ chạy bằng Spring Boot/Tomcat nhúng sẵn).
3. Chạy app: chuột phải `TourAppApplication.java` > **Run As > Java Application**.
   - App sẽ tự tạo database `travel_app_java` và các bảng cần thiết (khác với database `travel_app` của bản PHP cũ, để tránh đụng nhau).
4. Sau khi thấy dòng `Started TourAppApplication` trong Console, mở phpMyAdmin
   (`http://localhost/phpmyadmin`), chọn database `travel_app_java`, vào tab **SQL**,
   copy toàn bộ nội dung file `database/seed-data.sql` dán vào rồi bấm **Go**
   — bước này chỉ cần làm **1 lần** để có sẵn 4 tour mẫu để test.
5. Mở trình duyệt vào: `http://localhost:8080/frontend/index.html`

## Tài khoản test
Tạo tài khoản mới qua trang **Đăng ký** (`register.html`) — mật khẩu PHP cũ (bcrypt) không
dùng lại được vì database mới trống, cần đăng ký lại từ đầu.

## Ghi chú
- Vì đơn đặt tour mặc định ở trạng thái `pending` (chờ duyệt), và việc "duyệt đơn" vốn thuộc
  khu vực admin (chưa làm) — nên để test tiếp thanh toán/đánh giá, bạn có thể vào phpMyAdmin,
  bảng `bookings`, sửa tay cột `status` của 1 đơn từ `pending` thành `confirmed` để test thử.
