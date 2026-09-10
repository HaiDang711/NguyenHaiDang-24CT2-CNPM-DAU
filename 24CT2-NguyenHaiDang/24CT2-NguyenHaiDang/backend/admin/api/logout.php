<?php
require_once __DIR__ . '/../config/admin_auth.php';
unset($_SESSION['admin_id'], $_SESSION['admin_role']);
echo json_encode(['success' => true]);
