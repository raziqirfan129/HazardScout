package com.example.hazardscout;

import android.app.Application;

import com.example.hazardscout.data.AppPreferences;

public class HazardScoutApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppPreferences.applySavedTheme(this);
    }
}
