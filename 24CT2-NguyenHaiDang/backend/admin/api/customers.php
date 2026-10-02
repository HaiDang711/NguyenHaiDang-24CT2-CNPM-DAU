<?php
// Admin + nhân viên đều được quản lý người dùng (khách hàng) và tạo tài khoản hộ
require_once __DIR__ . '/../config/admin_auth.php';
requireAdmin(['admin', 'staff']);
$pdo = getDB();

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $rows = $pdo->query("SELECT id, full_name, phone, email, created_at FROM users ORDER BY created_at DESC")->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode(['users' => $rows]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Nhân viên tạo tài khoản hộ khách (vd khách đặt qua điện thoại)
    $data = json_decode(file_get_contents('php://input'), true);
    $fullName = trim($data['full_name'] ?? '');
    $phone    = trim($data['phone'] ?? '');
    $email    = trim($data['email'] ?? '');
    $password = $data['password'] ?? '';

    if ($fullName === '' || $phone === '' || strlen($password) < 6) {
        http_response_code(400);
        echo json_encode(['error' => 'Vui lòng nhập đủ họ tên, số điện thoại và mật khẩu (tối thiểu 6 ký tự).']);
        exit;
    }
    $check = $pdo->prepare('SELECT id FROM users WHERE phone = ?');
    $check->execute([$phone]);
    if ($check->fetch()) {
        http_response_code(409);
        echo json_encode(['error' => 'Số điện thoại này đã có tài khoản.']);
        exit;
    }
    $hash = password_hash($password, PASSWORD_BCRYPT);
    $stmt = $pdo->prepare('INSERT INTO users (full_name, phone, email, password_hash) VALUES (?, ?, ?, ?)');
    $stmt->execute([$fullName, $phone, $email ?: null, $hash]);
    echo json_encode(['success' => true]);
    exit;
}

http_response_code(405);
echo json_encode(['error' => 'Phương thức không hợp lệ.']);
