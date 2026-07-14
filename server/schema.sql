CREATE DATABASE IF NOT EXISTS hazardscout
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE hazardscout;

CREATE TABLE IF NOT EXISTS hazard_reports (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    reported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent VARCHAR(500) NOT NULL,
    device_info VARCHAR(255) NOT NULL,
    app_version VARCHAR(30) NOT NULL DEFAULT '',
    location_text VARCHAR(180) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    hazard_category ENUM('Road Hazards','Environmental Hazards','Building Hazards') NOT NULL,
    hazard_description VARCHAR(1000) NOT NULL,
    INDEX idx_category (hazard_category),
    INDEX idx_reported_at (reported_at),
    INDEX idx_coordinates (latitude, longitude)
) ENGINE=InnoDB;

-- Dynamic demo data stored in MySQL, not hard-coded in the Android application.
INSERT INTO hazard_reports
(user_name, reported_at, user_agent, device_info, app_version, location_text, latitude, longitude, hazard_category, hazard_description)
SELECT 'Demo Reporter', NOW() - INTERVAL 3 HOUR, 'HazardScout seed', 'Android demo device', '2.0',
       'Jalan Ampang', 3.1598000, 101.7123000, 'Road Hazards', 'Large pothole in the left lane; vehicles should slow down.'
WHERE NOT EXISTS (SELECT 1 FROM hazard_reports WHERE location_text='Jalan Ampang' AND hazard_category='Road Hazards');

INSERT INTO hazard_reports
(user_name, reported_at, user_agent, device_info, app_version, location_text, latitude, longitude, hazard_category, hazard_description)
SELECT 'Demo Reporter', NOW() - INTERVAL 2 HOUR, 'HazardScout seed', 'Android demo device', '2.0',
       'Taman Tasik Titiwangsa', 3.1764000, 101.6950000, 'Environmental Hazards', 'Fallen tree partly blocks the pedestrian path after heavy rain.'
WHERE NOT EXISTS (SELECT 1 FROM hazard_reports WHERE location_text='Taman Tasik Titiwangsa' AND hazard_category='Environmental Hazards');

INSERT INTO hazard_reports
(user_name, reported_at, user_agent, device_info, app_version, location_text, latitude, longitude, hazard_category, hazard_description)
SELECT 'Demo Reporter', NOW() - INTERVAL 1 HOUR, 'HazardScout seed', 'Android demo device', '2.0',
       'Bukit Bintang Walkway', 3.1468000, 101.7113000, 'Building Hazards', 'Loose ceiling panel near the building entrance; keep clear of the area.'
WHERE NOT EXISTS (SELECT 1 FROM hazard_reports WHERE location_text='Bukit Bintang Walkway' AND hazard_category='Building Hazards');
