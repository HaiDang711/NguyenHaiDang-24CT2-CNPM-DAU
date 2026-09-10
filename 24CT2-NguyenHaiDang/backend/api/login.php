<?php
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/db.php';

header('Content-Type: application/json; charset=utf-8');

$data = json_decode(file_get_contents('php://input'), true);

$identifier = trim($data['identifier'] ?? '');
$password = $data['password'] ?? '';

if ($identifier === '' || $password === '') {
    http_response_code(400);

    echo json_encode([
        'success' => false,
        'error' => 'Vui lòng nhập số điện thoại/email và mật khẩu.'
    ]);
    exit;
}

$pdo = getDB();

$stmt = $pdo->prepare('
    SELECT *
    FROM users
    WHERE phone = ? OR email = ?
    LIMIT 1
');

$stmt->execute([$identifier, $identifier]);

$user = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$user || !password_verify($password, $user['password_hash'])) {
    http_response_code(401);

    echo json_encode([
        'success' => false,
        'error' => 'Số điện thoại/email hoặc mật khẩu không đúng.'
    ]);
    exit;
}

session_regenerate_id(true);

$_SESSION['user_id'] = $user['id'];

echo json_encode([
    'success' => true,
    'message' => 'Đăng nhập thành công.',
    'user' => [
        'id' => $user['id'],
        'full_name' => $user['full_name'],
        'phone' => $user['phone'],
        'email' => $user['email']
    ]
]);