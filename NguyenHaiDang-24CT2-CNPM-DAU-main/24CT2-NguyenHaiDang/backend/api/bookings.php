<?php
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/db.php';

if (empty($_SESSION['user_id'])) {
    http_response_code(401);
    echo json_encode(['success' => false, 'error' => 'Bạn chưa đăng nhập.']);
    exit;
}

$userId = (int) $_SESSION['user_id'];
$pdo = getDB();

try {
    $stmt = $pdo->prepare('
        SELECT
            b.id, b.booking_code, b.user_id, b.tour_id, b.package_id,
            b.guest_name, b.guest_phone, b.guest_email,
            b.guest_count, b.departure_date, b.vehicle_request, b.assigned_vehicle,
            b.payment_method, b.payment_status, b.total_price,
            b.status, b.cancel_reason, b.created_at,
            t.name AS tour_name, t.place AS tour_place, t.address AS tour_address,
            p.name AS package_name,
            r.id AS review_id
        FROM bookings b
        LEFT JOIN tours t ON t.id = b.tour_id
        LEFT JOIN tour_packages p ON p.id = b.package_id
        LEFT JOIN reviews r ON r.booking_id = b.id
        WHERE b.user_id = ?
        ORDER BY b.created_at DESC
    ');
    $stmt->execute([$userId]);
    $bookings = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        'success' => true,
        'current_user_id' => $userId,
        'count' => count($bookings),
        'bookings' => $bookings
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'error' => $e->getMessage()]);
}
