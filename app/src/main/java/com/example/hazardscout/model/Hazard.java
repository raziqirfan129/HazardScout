package com.example.hazardscout.model;

import org.json.JSONObject;

public class Hazard {
    private final int id;
    private final String userName;
    private final String reportedAt;
    private final String userAgent;
    private final String deviceInfo;
    private final String locationText;
    private final double latitude;
    private final double longitude;
    private final String category;
    private final String description;

    private Hazard(int id, String userName, String reportedAt, String userAgent, String deviceInfo,
                   String locationText, double latitude, double longitude, String category, String description) {
        this.id = id;
        this.userName = userName;
        this.reportedAt = reportedAt;
        this.userAgent = userAgent;
        this.deviceInfo = deviceInfo;
        this.locationText = locationText;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.description = description;
    }

    public static Hazard fromJson(JSONObject json) {
        return new Hazard(
                json.optInt("id"),
                json.optString("user_name", "Unknown"),
                json.optString("reported_at", "Unknown time"),
                json.optString("user_agent", ""),
                json.optString("device_info", ""),
                json.optString("location_text", "Unknown location"),
                json.optDouble("latitude", Double.NaN),
                json.optDouble("longitude", Double.NaN),
                json.optString("hazard_category", "Road Hazards"),
                json.optString("hazard_description", "No description")
        );
    }

    public boolean hasValidCoordinates() {
        return !Double.isNaN(latitude) && !Double.isNaN(longitude)
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }

    public int getId() { return id; }
    public String getUserName() { return userName; }
    public String getReportedAt() { return reportedAt; }
    public String getUserAgent() { return userAgent; }
    public String getDeviceInfo() { return deviceInfo; }
    public String getLocationText() { return locationText; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
}
