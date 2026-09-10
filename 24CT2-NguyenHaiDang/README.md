# DangBooking — Web đặt tour du lịch (frontend/backend tách riêng, chạy trên XAMPP)

## Cấu trúc thư mục
```
travel-app/
├── frontend/            ← HTML/CSS/JS thuần, người dùng nhìn thấy
│   ├── index.html       (trang chủ — danh sách tour, lọc/sắp xếp)
│   ├── register.html
│   ├── tour.html        (chi tiết tour + đánh giá dịch vụ)
│   ├── booking.html     (đặt tour)
│   ├── confirm.html
│   ├── profile.html     (trang cá nhân: lịch sử, hủy/đổi, thanh toán, đánh giá)
│   ├── admin/           (khu vực quản trị — admin & nhân viên)
│   ├── css/style.css
│   └── js/*.js
├── backend/
│   ├── config/db.php, cors.php
│   ├── api/             (API khách hàng: register, login, tours, book, cancel-request, pay, review...)
│   └── admin/           (API quản trị viên & nhân viên)
└── database/schema.sql
```

## Cài đặt / cập nhật

1. Copy toàn bộ thư mục `travel-app` vào `htdocs` của XAMPP, bật Apache + MySQL.
2. **QUAN TRỌNG:** vào `http://localhost/phpmyadmin` → chọn database `travel_app` → tab **SQL** →
   copy **toàn bộ khối** nằm dưới dòng `-- NÂNG CẤP LỚN: đổi tên hotel -> tour...` trong file
   `database/schema.sql` → dán vào và bấm **Go**. Chỉ chạy đúng 1 lần.
   - Script này: sửa cột trạng thái đơn hàng, đổi tên bảng `hotels`→`tours`, `rooms`→`tour_packages`,
     đổi cột `hotel_id`→`tour_id`, `room_id`→`package_id`, thêm cột hủy/đổi + thanh toán, và tạo bảng `reviews`.
3. Truy cập: `http://localhost/travel-app/frontend/index.html`

## Các chức năng đã có (theo mã yêu cầu FR)

| Mã | Chức năng | Vị trí |
|---|---|---|
| FR-03 | Xem chi tiết tour, điểm nổi bật | `tour.html` |
| FR-05/06 | Tìm tour, đặt tour theo ngày khởi hành + số người | `booking.html` |
| FR-07 | **Lọc theo địa điểm, sắp xếp theo giá** | `index.html` (2 ô lọc phía trên danh sách tour) |
| FR-08 | Lịch sử đặt tour | `profile.html` |
| FR-09 | **Khách gửi yêu cầu hủy/đổi tour** — nhân viên duyệt (đồng ý hủy / từ chối) | nút "Yêu cầu hủy/đổi" trong `profile.html`, xử lý tại `admin/bookings.html` |
| FR-10 | **Thanh toán** — chỉ là bản MÔ PHỎNG (đánh dấu đã thanh toán), CHƯA nối cổng ngân hàng thật | nút "Thanh toán ngay" trong `profile.html`, chỉ hiện khi đơn đã được duyệt |
| FR-11 | Quản lý thông tin cá nhân | `profile.html` |
| FR-12 | **Đánh giá dịch vụ** (1-5 sao + bình luận), chỉ được đánh giá sau khi đơn đã duyệt, 1 lần/đơn | nút "Đánh giá tour này" trong `profile.html`, hiển thị công khai ở `tour.html` |
| FR-13/14/15/16/17 | Phân quyền Khách hàng / Nhân viên / Admin, quản lý đơn - người dùng - tour - tài khoản nhân viên - doanh thu | `frontend/admin/` |

Đăng nhập khách hàng, nhân viên, admin dùng **chung 1 ô đăng nhập** ở trang chủ (hệ thống tự nhận diện loại tài khoản).
Tài khoản admin mặc định: `admin` / `admin123`.

## Quy trình đơn đặt tour

`pending` (chờ duyệt) → nhân viên/admin **Duyệt** → `confirmed` (đã duyệt) → khách **Thanh toán** → `paid`.
Khách có thể **Yêu cầu hủy/đổi** ở trạng thái `pending` hoặc `confirmed` (chưa khởi hành) → chuyển sang
`cancel_requested` → nhân viên **Đồng ý hủy** (→ `cancelled`) hoặc **Từ chối** (→ quay lại `confirmed`).

## Còn thiếu (chưa làm)

- Thanh toán thật qua cổng ngân hàng/VNPay/Momo (hiện chỉ là nút mô phỏng).
- Nhân viên chỉnh sửa/xóa tour đã tạo (hiện chỉ thêm mới được).
- Thông báo qua email/SMS khi đơn được duyệt hoặc bị từ chối.

## Muốn đưa web lên mạng (chạy vĩnh viễn)

XAMPP chỉ chạy trên máy bạn. Khi sẵn sàng public: thuê hosting hỗ trợ PHP + MySQL, upload `frontend/`
và `backend/`, import lại `schema.sql` (bản đầy đủ, không chỉ phần nâng cấp) vào MySQL của hosting,
sửa `backend/config/db.php` theo thông tin hosting cấp, mua domain + bật SSL.
