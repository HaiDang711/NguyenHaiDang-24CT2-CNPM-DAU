<?php
// CHỈ ADMIN mới xem được doanh thu tổng / báo cáo
require_once __DIR__ . '/../config/admin_auth.php';
requireAdmin(['admin']);
$pdo = getDB();

$totalRevenue = $pdo->query("SELECT COALESCE(SUM(total_price),0) FROM bookings WHERE status = 'confirmed'")->fetchColumn();
$pendingCount = $pdo->query("SELECT COUNT(*) FROM bookings WHERE status = 'pending'")->fetchColumn();
$confirmedCount = $pdo->query("SELECT COUNT(*) FROM bookings WHERE status = 'confirmed'")->fetchColumn();
$cancelledCount = $pdo->query("SELECT COUNT(*) FROM bookings WHERE status = 'cancelled'")->fetchColumn();
$userCount = $pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();
$staffCount = $pdo->query("SELECT COUNT(*) FROM admins")->fetchColumn();

$topTours = $pdo->query("
    SELECT t.name, COUNT(b.id) AS bookings, COALESCE(SUM(b.total_price),0) AS revenue
    FROM tours t
    LEFT JOIN bookings b ON b.tour_id = t.id AND b.status = 'confirmed'
    GROUP BY t.id ORDER BY revenue DESC LIMIT 5
")->fetchAll(PDO::FETCH_ASSOC);

echo json_encode([
    'total_revenue' => (int)$totalRevenue,
    'pending_count' => (int)$pendingCount,
    'confirmed_count' => (int)$confirmedCount,
    'cancelled_count' => (int)$cancelledCount,
    'user_count' => (int)$userCount,
    'staff_count' => (int)$staffCount,
    'top_tours' => $topTours
]);
