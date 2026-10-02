<?php
require_once __DIR__ . '/../config/admin_auth.php';

$data = json_decode(file_get_contents('php://input'), true);
$username = trim($data['username'] ?? '');
$password = $data['password'] ?? '';

if ($username === '' || $password === '') {
    http_response_code(400);
    echo json_encode(['error' => 'Vui lòng nhập tên đăng nhập và mật khẩu.']);
    exit;
}

$pdo = getDB();
$stmt = $pdo->prepare('SELECT * FROM admins WHERE username = ? LIMIT 1');
$stmt->execute([$username]);
$admin = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$admin || !password_verify($password, $admin['password_hash'])) {
    http_response_code(401);
    echo json_encode(['error' => 'Tên đăng nhập hoặc mật khẩu không đúng.']);
    exit;
}

$_SESSION['admin_id'] = $admin['id'];
$_SESSION['admin_role'] = $admin['role'];

echo json_encode([
    'success' => true,
    'admin' => ['id' => $admin['id'], 'full_name' => $admin['full_name'], 'username' => $admin['username'], 'role' => $admin['role']]
]);
