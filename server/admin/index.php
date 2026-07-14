<?php
declare(strict_types=1);
date_default_timezone_set('Asia/Kuala_Lumpur');
require_once __DIR__ . '/../config/database.php';

$category = trim((string)($_GET['category'] ?? ''));
$search = trim((string)($_GET['search'] ?? ''));
$allowed = ['Road Hazards', 'Environmental Hazards', 'Building Hazards'];

$where = [];
$params = [];
if (in_array($category, $allowed, true)) {
    $where[] = 'hazard_category = :category';
    $params[':category'] = $category;
}
if ($search !== '') {
    $where[] = '(user_name LIKE :search OR location_text LIKE :search OR hazard_description LIKE :search)';
    $params[':search'] = '%' . $search . '%';
}

$sql = 'SELECT * FROM hazard_reports';
if ($where) $sql .= ' WHERE ' . implode(' AND ', $where);
$sql .= ' ORDER BY reported_at DESC, id DESC';
$stmt = db()->prepare($sql);
$stmt->execute($params);
$rows = $stmt->fetchAll();

$counts = ['All' => 0, 'Road Hazards' => 0, 'Environmental Hazards' => 0, 'Building Hazards' => 0];
foreach (db()->query('SELECT hazard_category, COUNT(*) AS total FROM hazard_reports GROUP BY hazard_category') as $row) {
    $counts[$row['hazard_category']] = (int)$row['total'];
    $counts['All'] += (int)$row['total'];
}

function e(string $value): string { return htmlspecialchars($value, ENT_QUOTES, 'UTF-8'); }
function badge_class(string $category): string {
    return match ($category) {
        'Environmental Hazards' => 'badge environment',
        'Building Hazards' => 'badge building',
        default => 'badge road',
    };
}
?>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>HazardScout Server Dashboard</title>
    <link rel="stylesheet" href="assets/style.css">
</head>
<body>
<header>
    <div>
        <h1>HazardScout</h1>
        <p>Server-side hazard records dashboard</p>
    </div>
    <a class="api-link" href="../api/health.php" target="_blank" rel="noopener">API Health</a>
</header>

<main>
    <section class="stats">
        <article><strong><?= $counts['All'] ?></strong><span>All reports</span></article>
        <article><strong><?= $counts['Road Hazards'] ?></strong><span>Road</span></article>
        <article><strong><?= $counts['Environmental Hazards'] ?></strong><span>Environmental</span></article>
        <article><strong><?= $counts['Building Hazards'] ?></strong><span>Building</span></article>
    </section>

    <section class="panel">
        <form method="get" class="filters">
            <label>
                Category
                <select name="category">
                    <option value="">All categories</option>
                    <?php foreach ($allowed as $option): ?>
                        <option value="<?= e($option) ?>" <?= $category === $option ? 'selected' : '' ?>><?= e($option) ?></option>
                    <?php endforeach; ?>
                </select>
            </label>
            <label>
                Search
                <input type="search" name="search" value="<?= e($search) ?>" placeholder="Name, location, or description">
            </label>
            <button type="submit">Apply Filter</button>
            <a class="reset" href="index.php">Reset</a>
        </form>
    </section>

    <section class="panel table-wrap">
        <div class="section-heading">
            <div>
                <h2>Hazard Reports</h2>
                <p><?= count($rows) ?> record(s) shown. Date/time is formatted in Malaysia time.</p>
            </div>
        </div>

        <table>
            <thead>
                <tr>
                    <th>ID</th><th>User and device</th><th>Date/time</th><th>Location / GPS</th><th>Category</th><th>Description</th>
                </tr>
            </thead>
            <tbody>
            <?php if (!$rows): ?>
                <tr><td colspan="6" class="empty">No records match the selected filter.</td></tr>
            <?php else: ?>
                <?php foreach ($rows as $row): ?>
                <tr>
                    <td>#<?= (int)$row['id'] ?></td>
                    <td>
                        <strong><?= e($row['user_name']) ?></strong>
                        <small><?= e($row['device_info']) ?></small>
                        <small>App <?= e($row['app_version']) ?></small>
                        <details><summary>User agent</summary><?= e($row['user_agent']) ?></details>
                    </td>
                    <td><?= e(date('d M Y, h:i A', strtotime($row['reported_at']))) ?></td>
                    <td>
                        <strong><?= e($row['location_text']) ?></strong>
                        <small><?= e((string)$row['latitude']) ?>, <?= e((string)$row['longitude']) ?></small>
                        <a href="https://www.openstreetmap.org/?mlat=<?= e((string)$row['latitude']) ?>&mlon=<?= e((string)$row['longitude']) ?>#map=18/<?= e((string)$row['latitude']) ?>/<?= e((string)$row['longitude']) ?>" target="_blank" rel="noopener">Open map</a>
                    </td>
                    <td><span class="<?= badge_class($row['hazard_category']) ?>"><?= e($row['hazard_category']) ?></span></td>
                    <td><?= nl2br(e($row['hazard_description'])) ?></td>
                </tr>
                <?php endforeach; ?>
            <?php endif; ?>
            </tbody>
        </table>
    </section>
</main>
<footer>HazardScout ICT602 Group Project · <?= date('Y') ?></footer>
</body>
</html>
