<?php
// Admin + nhân viên: quản lý tour & gói tour (thêm tour, thêm/sửa gói)
require_once __DIR__ . '/../config/admin_auth.php';
requireAdmin(['admin', 'staff']);
$pdo = getDB();

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $tours = $pdo->query("SELECT id, name, place, price_from FROM tours ORDER BY id DESC")->fetchAll(PDO::FETCH_ASSOC);
    foreach ($tours as &$t) {
        $p = $pdo->prepare('SELECT id, name, description, price FROM tour_packages WHERE tour_id = ?');
        $p->execute([$t['id']]);
        $t['packages'] = $p->fetchAll(PDO::FETCH_ASSOC);
    }
    echo json_encode(['tours' => $tours]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    $entity = $data['entity'] ?? ''; // 'tour' hoặc 'package'

    if ($entity === 'tour') {
        $name = trim($data['name'] ?? '');
        $place = trim($data['place'] ?? '');
        $address = trim($data['address'] ?? '');
        $description = trim($data['description'] ?? '');
        $rating = trim($data['rating'] ?? '5.0 (0)');
        $priceFrom = intval($data['price_from'] ?? 0);
        $highlights = trim($data['highlights'] ?? '');
        if ($name === '' || $place === '' || !$priceFrom) {
            http_response_code(400);
            echo json_encode(['error' => 'Vui lòng nhập đủ tên, địa điểm và giá.']);
            exit;
        }
        $stmt = $pdo->prepare('INSERT INTO tours (name, place, address, description, rating, price_from, highlights) VALUES (?,?,?,?,?,?,?)');
        $stmt->execute([$name, $place, $address, $description, $rating, $priceFrom, $highlights]);
        echo json_encode(['success' => true, 'id' => $pdo->lastInsertId()]);
        exit;
    }

    if ($entity === 'package') {
        $tourId = intval($data['tour_id'] ?? 0);
        $name = trim($data['name'] ?? '');
        $description = trim($data['description'] ?? '');
        $price = intval($data['price'] ?? 0);
        if (!$tourId || $name === '' || !$price) {
            http_response_code(400);
            echo json_encode(['error' => 'Vui lòng nhập đủ thông tin gói tour.']);
            exit;
        }
        $stmt = $pdo->prepare('INSERT INTO tour_packages (tour_id, name, description, price) VALUES (?,?,?,?)');
        $stmt->execute([$tourId, $name, $description, $price]);
        echo json_encode(['success' => true, 'id' => $pdo->lastInsertId()]);
        exit;
    }

    http_response_code(400);
    echo json_encode(['error' => 'entity phải là "tour" hoặc "package".']);
    exit;
}

http_response_code(405);
echo json_encode(['error' => 'Phương thức không hợp lệ.']);
