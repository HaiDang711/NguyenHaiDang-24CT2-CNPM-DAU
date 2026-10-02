<?php
// FR-10: Thanh toán — đây là bản MÔ PHỎNG (demo), CHƯA nối với ngân hàng/cổng thanh toán thật.
// Mục đích: cho hệ thống có luồng trạng thái "đã thanh toán" để kiểm thử, không xử lý tiền thật.
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/db.php';

if (empty($_SESSION['user_id'])) {
    http_response_code(401);
    echo json_encode(['error' => 'Bạn cần đăng nhập.']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);
$bookingId = intval($data['booking_id'] ?? 0);

$pdo = getDB();
$stmt = $pdo->prepare('SELECT id, status, payment_status FROM bookings WHERE id = ? AND user_id = ?');
$stmt->execute([$bookingId, $_SESSION['user_id']]);
$booking = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$booking) { http_response_code(404); echo json_encode(['error' => 'Không tìm thấy đơn đặt tour.']); exit; }
if ($booking['status'] !== 'confirmed') {
    http_response_code(400);
    echo json_encode(['error' => 'Chỉ có thể thanh toán khi đơn đã được nhân viên duyệt.']);
    exit;
}
if ($booking['payment_status'] === 'paid') {
    http_response_code(400);
    echo json_encode(['error' => 'Đơn này đã được thanh toán rồi.']);
    exit;
}

$upd = $pdo->prepare("UPDATE bookings SET payment_status = 'paid' WHERE id = ?");
$upd->execute([$bookingId]);

echo json_encode(['success' => true, 'message' => 'Thanh toán thành công (mô phỏng).']);
