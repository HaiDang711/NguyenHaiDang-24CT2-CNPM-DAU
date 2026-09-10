<?php
// Cấu hình kết nối MySQL mặc định của XAMPP (root, không mật khẩu).
// Nếu bạn đặt mật khẩu MySQL khác, sửa DB_PASS bên dưới.
define('DB_HOST', 'localhost');
define('DB_NAME', 'travel_app');
define('DB_USER', 'root');
define('DB_PASS', '');

function getDB() {
    static $pdo = null;
    if ($pdo === null) {
        try {
            $pdo = new PDO(
                "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4",
                DB_USER,
                DB_PASS,
                [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
            );
        } catch (PDOException $e) {
            http_response_code(500);
            echo json_encode(['error' => 'Không kết nối được database. Kiểm tra XAMPP đã bật MySQL và đã import schema.sql chưa.']);
            exit;
        }
    }
    return $pdo;
}
