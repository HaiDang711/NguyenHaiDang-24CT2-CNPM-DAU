<?php
require_once __DIR__ . '/../config/admin_auth.php';

if (empty($_SESSION['admin_id'])) {
    echo json_encode(['logged_in' => false]);
    exit;
}
$pdo = getDB();
$stmt = $pdo->prepare('SELECT id, full_name, username, role FROM admins WHERE id = ?');
$stmt->execute([$_SESSION['admin_id']]);
$admin = $stmt->fetch(PDO::FETCH_ASSOC);
echo json_encode(['logged_in' => (bool)$admin, 'admin' => $admin ?: null]);
