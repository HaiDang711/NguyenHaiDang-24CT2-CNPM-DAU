<?php
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/db.php';

if (empty($_SESSION['user_id'])) {
    http_response_code(401);
    echo json_encode(['error' => 'Bạn cần đăng nhập trước khi đặt tour.']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);
$tourId    = intval($data['tour_id'] ?? 0);
$packageId = intval($data['package_id'] ?? 0);
$name      = trim($data['guest_name'] ?? '');
$phone     = trim($data['guest_phone'] ?? '');
$email     = trim($data['guest_email'] ?? '');
$payment   = trim($data['payment_method'] ?? 'Thanh toán khi nhận phòng');
$guestCount = max(1, intval($data['guest_count'] ?? 1));
$departureDate = trim($data['departure_date'] ?? '');
$vehicleRequest = trim($data['vehicle_request'] ?? '');

if (!$tourId || !$packageId || $name === '' || $phone === '' || $departureDate === '') {
    http_response_code(400);
    echo json_encode(['error' => 'Vui lòng chọn ngày khởi hành và điền đủ thông tin đặt tour.']);
    exit;
}

$pdo = getDB();
$pkgStmt = $pdo->prepare('SELECT price FROM tour_packages WHERE id = ? AND tour_id = ?');
$pkgStmt->execute([$packageId, $tourId]);
$package = $pkgStmt->fetch(PDO::FETCH_ASSOC);
if (!$package) { http_response_code(404); echo json_encode(['error' => 'Không tìm thấy gói tour.']); exit; }

$code = 'DangBooking' . random_int(100000, 999999);

$stmt = $pdo->prepare('INSERT INTO bookings
    (booking_code, user_id, tour_id, package_id, guest_name, guest_phone, guest_email, guest_count, departure_date, vehicle_request, payment_method, payment_status, total_price)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)');
$stmt->execute([$code, $_SESSION['user_id'], $tourId, $packageId, $name, $phone, $email ?: null, $guestCount, $departureDate, $vehicleRequest ?: null, $payment, 'unpaid', $package['price']]);

echo json_encode(['success' => true, 'booking_code' => $code, 'total_price' => $package['price']]);
