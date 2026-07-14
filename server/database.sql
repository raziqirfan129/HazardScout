-- HazardScout database for ICT602 Mobile Technology Group Project
-- Import this file in phpMyAdmin or run it in MySQL.

CREATE DATABASE IF NOT EXISTS hazardscout
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hazardscout;

CREATE TABLE IF NOT EXISTS hazards (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    reported_at DATETIME NOT NULL,
    user_agent VARCHAR(255) NOT NULL,
    device_info VARCHAR(255) NOT NULL,
    location_text VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    hazard_category ENUM('Road Hazards','Environmental Hazards','Building Hazards') NOT NULL,
    hazard_description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO hazards
(user_name, reported_at, user_agent, device_info, location_text, latitude, longitude, hazard_category, hazard_description)
VALUES
('Demo User', NOW(), 'Demo Browser', 'Demo Device', 'Jalan Air Jernih, Kuala Terengganu', 5.3306000, 103.1408000, 'Road Hazards', 'Pothole near the left lane. Motorcyclists should slow down.'),
('Demo User', NOW(), 'Demo Browser', 'Demo Device', 'Near public park entrance', 5.3313000, 103.1379000, 'Environmental Hazards', 'Fallen tree partially blocking pedestrian path.'),
('Demo User', NOW(), 'Demo Browser', 'Demo Device', 'Old shop lot staircase', 5.3296000, 103.1393000, 'Building Hazards', 'Broken step and loose handrail at the entrance.');
