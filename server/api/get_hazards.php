<?php
declare(strict_types=1);
require_once __DIR__ . '/bootstrap.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    json_response(['success' => false, 'message' => 'GET method required.'], 405);
}

try {
    $sql = "SELECT id, user_name,
                   DATE_FORMAT(reported_at, '%d %b %Y, %h:%i %p') AS reported_at,
                   user_agent, device_info, app_version, location_text,
                   latitude, longitude, hazard_category, hazard_description
            FROM hazard_reports
            ORDER BY reported_at DESC, id DESC";
    $rows = db()->query($sql)->fetchAll();

    foreach ($rows as &$row) {
        $row['id'] = (int)$row['id'];
        $row['latitude'] = (float)$row['latitude'];
        $row['longitude'] = (float)$row['longitude'];
    }

    json_response([
        'success' => true,
        'count' => count($rows),
        'data' => $rows,
    ]);
} catch (Throwable $e) {
    json_response([
        'success' => false,
        'message' => 'Unable to retrieve hazard records.',
    ], 500);
}
