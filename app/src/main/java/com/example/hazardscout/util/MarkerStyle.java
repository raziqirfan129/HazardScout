package com.example.hazardscout.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.example.hazardscout.R;

public final class MarkerStyle {
    private MarkerStyle() { }

    public static Drawable iconFor(Context context, String category) {
        String normalized = category == null ? "" : category.toLowerCase();
        if (normalized.contains("environment")) {
            return ContextCompat.getDrawable(context, R.drawable.ic_environment_hazard);
        }
        if (normalized.contains("building")) {
            return ContextCompat.getDrawable(context, R.drawable.ic_building_hazard);
        }
        return ContextCompat.getDrawable(context, R.drawable.ic_road_hazard);
    }
}
