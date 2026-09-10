<?php
require_once __DIR__ . '/../config/cors.php';
require_once __DIR__ . '/../config/db.php';

$id = intval($_GET['id'] ?? 0);
if (!$id) { http_response_code(400); echo json_encode(['error' => 'Thiếu id tour.']); exit; }

$pdo = getDB();
$stmt = $pdo->prepare('SELECT * FROM tours WHERE id = ?');
$stmt->execute([$id]);
$tour = $stmt->fetch(PDO::FETCH_ASSOC);
if (!$tour) { http_response_code(404); echo json_encode(['error' => 'Không tìm thấy tour.']); exit; }

$tour['highlights'] = $tour['highlights'] ? explode(',', $tour['highlights']) : [];

$pkgStmt = $pdo->prepare('SELECT id, name, description, price FROM tour_packages WHERE tour_id = ?');
$pkgStmt->execute([$id]);
$packages = $pkgStmt->fetchAll(PDO::FETCH_ASSOC);

// FR-12: đánh giá dịch vụ — điểm trung bình + danh sách bình luận của tour này
$ratingRow = $pdo->prepare('SELECT COUNT(*) AS total, AVG(rating) AS avg_rating FROM reviews WHERE tour_id = ?');
$ratingRow->execute([$id]);
$ratingInfo = $ratingRow->fetch(PDO::FETCH_ASSOC);

$reviewStmt = $pdo->prepare('
    SELECT r.rating, r.comment, r.created_at, u.full_name
    FROM reviews r JOIN users u ON u.id = r.user_id
    WHERE r.tour_id = ? ORDER BY r.created_at DESC LIMIT 20');
$reviewStmt->execute([$id]);
$reviews = $reviewStmt->fetchAll(PDO::FETCH_ASSOC);

echo json_encode([
    'tour' => $tour,
    'packages' => $packages,
    'review_summary' => [
        'total' => (int)$ratingInfo['total'],
        'average' => $ratingInfo['avg_rating'] ? round($ratingInfo['avg_rating'], 1) : null
    ],
    'reviews' => $reviews
]);
