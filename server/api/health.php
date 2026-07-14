<?php
declare(strict_types=1);
require_once __DIR__ . '/bootstrap.php';

try {
    db()->query('SELECT 1');
    json_response([
        'success' => true,
        'service' => 'HazardScout API',
        'server_time' => date('d M Y, h:i:s A'),
    ]);
} catch (Throwable $e) {
    json_response([
        'success' => false,
        'message' => 'Database connection failed. Check server/config/database.php and import schema.sql.',
    ], 500);
}
