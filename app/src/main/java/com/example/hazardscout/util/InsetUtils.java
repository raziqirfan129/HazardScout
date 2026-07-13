package com.example.hazardscout.util;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class InsetUtils {

    private InsetUtils() {
        // Prevent object creation
    }

    public static void applyToolbarInset(View toolbar) {

        final int originalHeight = toolbar.getLayoutParams().height;

        final int originalLeft = toolbar.getPaddingLeft();
        final int originalTop = toolbar.getPaddingTop();
        final int originalRight = toolbar.getPaddingRight();
        final int originalBottom = toolbar.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, windowInsets) -> {

            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );

            view.setPadding(
                    originalLeft + insets.left,
                    originalTop + insets.top,
                    originalRight + insets.right,
                    originalBottom
            );

            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = originalHeight + insets.top;
            view.setLayoutParams(params);

            return windowInsets;
        });

        ViewCompat.requestApplyInsets(toolbar);
    }
}