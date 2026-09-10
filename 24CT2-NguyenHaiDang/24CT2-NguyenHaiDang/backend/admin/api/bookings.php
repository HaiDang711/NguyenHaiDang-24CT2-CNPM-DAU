<?php
// Admin + nhân viên: xem toàn bộ đơn, duyệt/hủy đơn, xử lý yêu cầu hủy/đổi (FR-09), xác nhận phương tiện
require_once __DIR__ . '/../config/admin_auth.php';
require_once __DIR__ . '/../config/vehicle_logic.php';
requireAdmin(['admin', 'staff']);
$pdo = getDB();

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $sql = "SELECT b.id, b.booking_code, b.guest_name, b.guest_phone, b.payment_method, b.payment_status,
                   b.total_price, b.status, b.cancel_reason, b.created_at,
                   b.guest_count, b.departure_date, b.vehicle_request, b.assigned_vehicle,
                   t.name AS tour_name, p.name AS package_name
            FROM bookings b
            JOIN tours t ON t.id = b.tour_id
            JOIN tour_packages p ON p.id = b.package_id
            ORDER BY b.created_at DESC";
    $rows = $pdo->query($sql)->fetchAll(PDO::FETCH_ASSOC);
    foreach ($rows as &$r) {
        $r['suggested_vehicle'] = suggestVehicle($r['guest_count'], $r['vehicle_request']);
    }
    echo json_encode(['bookings' => $rows]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    $id = intval($data['id'] ?? 0);
    $action = $data['action'] ?? ''; // 'confirm' | 'cancel' | 'reject_cancel'
    $assignedVehicle = trim($data['assigned_vehicle'] ?? '');

    if (!$id || !$action) {
        http_response_code(400);
        echo json_encode(['error' => 'Yêu cầu không hợp lệ.']);
        exit;
    }

    if ($action === 'confirm') {
        // Duyệt đơn (dùng cho cả đơn "chờ duyệt" bình thường)
        $stmt = $pdo->prepare("UPDATE bookings SET status = 'confirmed', assigned_vehicle = ? WHERE id = ?");
        $stmt->execute([$assignedVehicle ?: null, $id]);
        echo json_encode(['success' => true, 'status' => 'confirmed']);
        exit;
    }

    if ($action === 'cancel') {
        // Hủy đơn (dùng cho đơn "chờ duyệt" hoặc đồng ý yêu cầu hủy của khách)
        $stmt = $pdo->prepare("UPDATE bookings SET status = 'cancelled' WHERE id = ?");
        $stmt->execute([$id]);
        echo json_encode(['success' => true, 'status' => 'cancelled']);
        exit;
    }

    if ($action === 'reject_cancel') {
        // Từ chối yêu cầu hủy/đổi của khách -> trả đơn về trạng thái đã duyệt
        $stmt = $pdo->prepare("UPDATE bookings SET status = 'confirmed', cancel_reason = NULL WHERE id = ?");
        $stmt->execute([$id]);
        echo json_encode(['success' => true, 'status' => 'confirmed']);
        exit;
    }

    http_response_code(400);
    echo json_encode(['error' => 'Hành động không hợp lệ.']);
    exit;
}

http_response_code(405);
echo json_encode(['error' => 'Phương thức không hợp lệ.']);
