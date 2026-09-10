<?php
// FR-09: khách gửi yêu cầu hủy hoặc thay đổi tour — nhân viên/admin sẽ duyệt yêu cầu này
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/db.php';

if (empty($_SESSION['user_id'])) {
    http_response_code(401);
    echo json_encode(['error' => 'Bạn cần đăng nhập.']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);
$bookingId = intval($data['booking_id'] ?? 0);
$reason = trim($data['reason'] ?? '');

if (!$bookingId) {
    http_response_code(400);
    echo json_encode(['error' => 'Thiếu mã đơn.']);
    exit;
}

$pdo = getDB();
$stmt = $pdo->prepare('SELECT id, status, departure_date FROM bookings WHERE id = ? AND user_id = ?');
$stmt->execute([$bookingId, $_SESSION['user_id']]);
$booking = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$booking) { http_response_code(404); echo json_encode(['error' => 'Không tìm thấy đơn đặt tour.']); exit; }

// Chỉ cho hủy/đổi khi đơn đang chờ duyệt hoặc đã duyệt, và tour chưa khởi hành
if (!in_array($booking['status'], ['pending', 'confirmed'], true)) {
    http_response_code(400);
    echo json_encode(['error' => 'Đơn này không thể gửi yêu cầu hủy/đổi ở trạng thái hiện tại.']);
    exit;
}
if ($booking['departure_date'] && strtotime($booking['departure_date']) < strtotime(date('Y-m-d'))) {
    http_response_code(400);
    echo json_encode(['error' => 'Tour đã khởi hành, không thể gửi yêu cầu hủy/đổi.']);
    exit;
}

$upd = $pdo->prepare("UPDATE bookings SET status = 'cancel_requested', cancel_reason = ? WHERE id = ?");
$upd->execute([$reason ?: null, $bookingId]);

echo json_encode(['success' => true, 'message' => 'Đã gửi yêu cầu hủy/đổi tour, nhân viên sẽ xử lý sớm.']);
