<?php
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/db.php';

$pdo = getDB();

// FR-07: lọc theo địa điểm + sắp xếp theo giá
$place = trim($_GET['place'] ?? '');
$sort  = trim($_GET['sort'] ?? ''); // 'price_asc' | 'price_desc' | '' (mặc định)

$sql = "SELECT id, name, place, rating, price_from, color_from, color_to FROM tours";
$params = [];
if ($place !== '') {
    $sql .= " WHERE place LIKE ?";
    $params[] = "%$place%";
}
if ($sort === 'price_asc')       $sql .= " ORDER BY price_from ASC";
elseif ($sort === 'price_desc')  $sql .= " ORDER BY price_from DESC";
else                              $sql .= " ORDER BY id DESC";

$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$tours = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Danh sách địa điểm để đổ vào bộ lọc trên giao diện
$places = $pdo->query("SELECT DISTINCT place FROM tours ORDER BY place")->fetchAll(PDO::FETCH_COLUMN);

echo json_encode(['tours' => $tours, 'places' => $places]);
