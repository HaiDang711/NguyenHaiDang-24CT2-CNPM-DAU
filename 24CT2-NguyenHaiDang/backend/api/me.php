<?php
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/db.php';

header('Content-Type: application/json; charset=utf-8');

if (empty($_SESSION['user_id'])) {
    echo json_encode([
        'logged_in' => false,
        'user' => null
    ]);
    exit;
}

$pdo = getDB();

$stmt = $pdo->prepare('
    SELECT id, full_name, phone, email, created_at
    FROM users
    WHERE id = ?
');

$stmt->execute([$_SESSION['user_id']]);

$user = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$user) {
    unset($_SESSION['user_id']);

    echo json_encode([
        'logged_in' => false,
        'user' => null
    ]);
    exit;
}

echo json_encode([
    'logged_in' => true,
    'user' => $user
]);