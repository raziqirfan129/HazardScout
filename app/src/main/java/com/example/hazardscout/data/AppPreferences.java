package com.example.hazardscout.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class AppPreferences {
    private static final String PREFS = "hazardscout_preferences";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_REPORTER_NAME = "reporter_name";

    /**
     * Laragon server URL for a real Android phone.
     * The phone and computer must be connected to the same Wi-Fi network.
     */
    public static final String DEFAULT_SERVER_URL =
            "http://192.168.100.23/hazardscout/server/api/";

    private static final String LEGACY_EMULATOR_URL =
            "http://10.0.2.2/hazardscout/api/";

    private AppPreferences() { }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getServerUrl(Context context) {
        SharedPreferences preferences = prefs(context);
        String value = preferences.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL);

        if (value == null || value.trim().isEmpty()) {
            value = DEFAULT_SERVER_URL;
        }

        value = value.trim();

        // Automatically migrate installations that still contain the old emulator URL.
        if (LEGACY_EMULATOR_URL.equalsIgnoreCase(value)
                || value.contains("10.0.2.2")) {
            value = DEFAULT_SERVER_URL;
            preferences.edit().putString(KEY_SERVER_URL, value).apply();
        }

        return value.endsWith("/") ? value : value + "/";
    }

    public static void setServerUrl(Context context, String url) {
        String value = url == null ? DEFAULT_SERVER_URL : url.trim();
        if (value.isEmpty()) value = DEFAULT_SERVER_URL;
        if (!value.endsWith("/")) value += "/";
        prefs(context).edit().putString(KEY_SERVER_URL, value).apply();
    }

    public static int getThemeMode(Context context) {
        return prefs(context).getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setThemeMode(Context context, int mode) {
        prefs(context).edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public static void applySavedTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(getThemeMode(context));
    }

    public static String getReporterName(Context context) {
        String value = prefs(context).getString(KEY_REPORTER_NAME, "");
        return value == null ? "" : value;
    }

    public static void setReporterName(Context context, String name) {
        prefs(context).edit().putString(KEY_REPORTER_NAME, name == null ? "" : name.trim()).apply();
    }
}
