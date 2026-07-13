package com.example.hazardscout.api;

import android.content.Context;

import com.example.hazardscout.data.AppPreferences;

public final class ApiConfig {
    private ApiConfig() { }

    public static String getHazardsUrl(Context context) {
        return AppPreferences.getServerUrl(context) + "get_hazards.php";
    }

    public static String addHazardUrl(Context context) {
        return AppPreferences.getServerUrl(context) + "add_hazard.php";
    }

    public static String healthUrl(Context context) {
        return AppPreferences.getServerUrl(context) + "health.php";
    }
}
