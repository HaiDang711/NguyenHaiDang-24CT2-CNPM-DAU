<?php
// Dùng chung cho mọi API trong backend/admin/api/*.php
require_once __DIR__ . '/../../config/db.php';

header('Content-Type: application/json; charset=utf-8');
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { exit(0); }

// Bắt mọi lỗi PHP không lường trước (vd bảng chưa tồn tại, sai cột...) và luôn trả về JSON,
// để trình duyệt không bao giờ nhận phải HTML lỗi khiến báo "not valid JSON" nữa.
set_exception_handler(function($e) {
    http_response_code(500);
    echo json_encode(['error' => 'Lỗi máy chủ: ' . $e->getMessage()]);
    exit;
});
error_reporting(E_ALL);
ini_set('display_errors', '0'); // không in cảnh báo PHP thô ra ngoài JSON

session_start();

// Bắt buộc phải đăng nhập admin/nhân viên. $roles = ['admin'] nếu chỉ admin mới được dùng API này.
function requireAdmin($roles = ['admin', 'staff']) {
    if (empty($_SESSION['admin_id']) || empty($_SESSION['admin_role'])) {
        http_response_code(401);
        echo json_encode(['error' => 'Vui lòng đăng nhập tài khoản quản trị.']);
        exit;
    }
    if (!in_array($_SESSION['admin_role'], $roles, true)) {
        http_response_code(403);
        echo json_encode(['error' => 'Tài khoản của bạn không có quyền thực hiện thao tác này.']);
        exit;
    }
    return ['id' => $_SESSION['admin_id'], 'role' => $_SESSION['admin_role']];
}
