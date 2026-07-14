<?php
declare(strict_types=1);
require_once __DIR__ . '/bootstrap.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_response(['success' => false, 'message' => 'POST method required.'], 405);
}

$allowedCategories = ['Road Hazards', 'Environmental Hazards', 'Building Hazards'];

$userName = post_string('user_name', 100);
$userAgent = post_string('user_agent', 500);
$deviceInfo = post_string('device_info', 255);
$appVersion = post_string('app_version', 30);
$locationText = post_string('location_text', 180);
$category = post_string('hazard_category', 50);
$description = post_string('hazard_description', 1000);
$latitudeRaw = $_POST['latitude'] ?? null;
$longitudeRaw = $_POST['longitude'] ?? null;

$errors = [];
if (text_length($userName) < 2) $errors[] = 'A valid user name is required.';
if (text_length($locationText) < 3) $errors[] = 'A clear location or landmark is required.';
if (text_length($description) < 8) $errors[] = 'A clearer hazard description is required.';
if (!in_array($category, $allowedCategories, true)) $errors[] = 'Invalid hazard category.';
if (!is_numeric($latitudeRaw) || !is_numeric($longitudeRaw)) {
    $errors[] = 'Valid GPS coordinates are required.';
}

$latitude = is_numeric($latitudeRaw) ? (float)$latitudeRaw : 0.0;
$longitude = is_numeric($longitudeRaw) ? (float)$longitudeRaw : 0.0;
if ($latitude < -90 || $latitude > 90 || $longitude < -180 || $longitude > 180) {
    $errors[] = 'GPS coordinates are outside the valid range.';
}

if ($errors !== []) {
    json_response([
        'success' => false,
        'message' => implode(' ', $errors),
        'errors' => $errors,
    ], 422);
}

if ($userAgent === '') {
    $userAgent = substr((string)($_SERVER['HTTP_USER_AGENT'] ?? 'Unknown Android client'), 0, 500);
}

try {
    $stmt = db()->prepare(
        'INSERT INTO hazard_reports
        (user_name, reported_at, user_agent, device_info, app_version, location_text,
         latitude, longitude, hazard_category, hazard_description)
        VALUES (:user_name, NOW(), :user_agent, :device_info, :app_version, :location_text,
                :latitude, :longitude, :hazard_category, :hazard_description)'
    );
    $stmt->execute([
        ':user_name' => $userName,
        ':user_agent' => $userAgent,
        ':device_info' => $deviceInfo,
        ':app_version' => $appVersion,
        ':location_text' => $locationText,
        ':latitude' => $latitude,
        ':longitude' => $longitude,
        ':hazard_category' => $category,
        ':hazard_description' => $description,
    ]);

    json_response([
        'success' => true,
        'message' => 'Hazard report saved successfully.',
        'id' => (int)db()->lastInsertId(),
    ], 201);
} catch (Throwable $e) {
    json_response([
        'success' => false,
        'message' => 'The server could not save this hazard report.',
    ], 500);
}
