package com.iyxan23.blocks.view;

import android.graphics.Color;

public class Utilities {

    /**
     * Multiples color by the factor (should be below 1.0f)
     *
     * @param color The color
     * @param factor The factor (should be below 1.0f)
     * @return The multiplied color
     */
    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }
}
