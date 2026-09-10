<?php
// FR-12: đánh giá dịch vụ — chỉ cho đánh giá 1 lần/đơn, và chỉ khi đơn đã được duyệt (đã dùng dịch vụ)
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/db.php';

if (empty($_SESSION['user_id'])) {
    http_response_code(401);
    echo json_encode(['error' => 'Bạn cần đăng nhập.']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);
$bookingId = intval($data['booking_id'] ?? 0);
$rating = intval($data['rating'] ?? 0);
$comment = trim($data['comment'] ?? '');

if ($rating < 1 || $rating > 5) {
    http_response_code(400);
    echo json_encode(['error' => 'Vui lòng chọn số sao từ 1 đến 5.']);
    exit;
}

$pdo = getDB();
$stmt = $pdo->prepare('SELECT id, tour_id, status FROM bookings WHERE id = ? AND user_id = ?');
$stmt->execute([$bookingId, $_SESSION['user_id']]);
$booking = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$booking) { http_response_code(404); echo json_encode(['error' => 'Không tìm thấy đơn đặt tour.']); exit; }
if ($booking['status'] !== 'confirmed') {
    http_response_code(400);
    echo json_encode(['error' => 'Chỉ có thể đánh giá sau khi đơn đã được duyệt.']);
    exit;
}

$check = $pdo->prepare('SELECT id FROM reviews WHERE booking_id = ?');
$check->execute([$bookingId]);
if ($check->fetch()) {
    http_response_code(409);
    echo json_encode(['error' => 'Bạn đã đánh giá đơn này rồi.']);
    exit;
}

$stmt = $pdo->prepare('INSERT INTO reviews (booking_id, user_id, tour_id, rating, comment) VALUES (?, ?, ?, ?, ?)');
$stmt->execute([$bookingId, $_SESSION['user_id'], $booking['tour_id'], $rating, $comment ?: null]);

echo json_encode(['success' => true, 'message' => 'Cảm ơn bạn đã đánh giá!']);
