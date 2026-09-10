<?php
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/db.php';

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
if (!preg_match('/^[0-9]{9,10}$/', $phone)) {
    http_response_code(400);
    echo json_encode(['error' => 'Số điện thoại không hợp lệ.']);
    exit;
}

$pdo = getDB();
$check = $pdo->prepare('SELECT id FROM users WHERE phone = ?');
$check->execute([$phone]);
if ($check->fetch()) {
    http_response_code(409);
    echo json_encode(['error' => 'Số điện thoại này đã được đăng ký.']);
    exit;
}

$hash = password_hash($password, PASSWORD_BCRYPT);
$stmt = $pdo->prepare('INSERT INTO users (full_name, phone, email, password_hash) VALUES (?, ?, ?, ?)');
$stmt->execute([$fullName, $phone, $email ?: null, $hash]);

echo json_encode(['success' => true, 'message' => 'Đăng ký thành công.']);
