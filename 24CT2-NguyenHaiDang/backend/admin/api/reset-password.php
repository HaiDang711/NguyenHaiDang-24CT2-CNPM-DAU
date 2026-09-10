<?php
// Admin + nhân viên: đặt lại mật khẩu hộ khách hàng (khi khách gọi điện nhờ hỗ trợ quên mật khẩu)
require_once __DIR__ . '/../config/admin_auth.php';
requireAdmin(['admin', 'staff']);
$pdo = getDB();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Phương thức không hợp lệ.']);
    exit;
}

$data = json_decode(file_get_contents('php://input'), true);
$userId = intval($data['user_id'] ?? 0);
$newPassword = $data['new_password'] ?? '';

if (!$userId || strlen($newPassword) < 6) {
    http_response_code(400);
    echo json_encode(['error' => 'Vui lòng chọn người dùng và nhập mật khẩu mới (tối thiểu 6 ký tự).']);
    exit;
}

$check = $pdo->prepare('SELECT id, full_name FROM users WHERE id = ?');
$check->execute([$userId]);
$user = $check->fetch(PDO::FETCH_ASSOC);
if (!$user) {
    http_response_code(404);
    echo json_encode(['error' => 'Không tìm thấy người dùng.']);
    exit;
}

$hash = password_hash($newPassword, PASSWORD_BCRYPT);
$stmt = $pdo->prepare('UPDATE users SET password_hash = ? WHERE id = ?');
$stmt->execute([$hash, $userId]);

echo json_encode(['success' => true, 'message' => "Đã đặt lại mật khẩu cho {$user['full_name']}."]);
