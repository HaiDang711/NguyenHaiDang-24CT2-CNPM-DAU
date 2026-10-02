<?php
// CHỈ ADMIN mới được cấp/xem tài khoản nhân viên
require_once __DIR__ . '/../config/admin_auth.php';
$me = requireAdmin(['admin']);
$pdo = getDB();

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $rows = $pdo->query("SELECT id, full_name, username, role, created_at FROM admins ORDER BY created_at DESC")->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode(['staff' => $rows]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    $fullName = trim($data['full_name'] ?? '');
    $username = trim($data['username'] ?? '');
    $password = $data['password'] ?? '';
    $role     = ($data['role'] ?? 'staff') === 'admin' ? 'admin' : 'staff';

    if ($fullName === '' || $username === '' || strlen($password) < 6) {
        http_response_code(400);
        echo json_encode(['error' => 'Vui lòng nhập đủ họ tên, tên đăng nhập và mật khẩu (tối thiểu 6 ký tự).']);
        exit;
    }
    $check = $pdo->prepare('SELECT id FROM admins WHERE username = ?');
    $check->execute([$username]);
    if ($check->fetch()) {
        http_response_code(409);
        echo json_encode(['error' => 'Tên đăng nhập này đã tồn tại.']);
        exit;
    }
    $hash = password_hash($password, PASSWORD_BCRYPT);
    $stmt = $pdo->prepare('INSERT INTO admins (full_name, username, password_hash, role, created_by) VALUES (?, ?, ?, ?, ?)');
    $stmt->execute([$fullName, $username, $hash, $role, $me['id']]);
    echo json_encode(['success' => true]);
    exit;
}

http_response_code(405);
echo json_encode(['error' => 'Phương thức không hợp lệ.']);
