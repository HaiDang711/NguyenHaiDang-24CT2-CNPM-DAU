<?php
// File này được include ở đầu mỗi API để trả JSON và cho phép session hoạt động.
// Vì frontend và backend chạy CHUNG một Apache (XAMPP), thường không cần CORS.
// Nếu bạn mở frontend bằng công cụ khác cổng (vd Live Server :5500), bật đoạn CORS bên dưới.
header('Content-Type: application/json; charset=utf-8');

$allowedOrigin = 'http://localhost:5500'; // đổi theo cổng Live Server nếu bạn dùng
if (isset($_SERVER['HTTP_ORIGIN'])) {
    header("Access-Control-Allow-Origin: " . $_SERVER['HTTP_ORIGIN']);
    header('Access-Control-Allow-Credentials: true');
    header('Access-Control-Allow-Headers: Content-Type');
    header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
}
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

// Bắt mọi lỗi PHP không lường trước và luôn trả JSON, tránh lỗi "not valid JSON" ở frontend.
set_exception_handler(function($e) {
    http_response_code(500);
    echo json_encode(['error' => 'Lỗi máy chủ: ' . $e->getMessage()]);
    exit;
});
error_reporting(E_ALL);
ini_set('display_errors', '0');

session_start();
