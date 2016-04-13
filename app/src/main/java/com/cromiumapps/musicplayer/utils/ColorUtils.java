package com.cromiumapps.musicplayer.utils;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ColorUtils {

    public static int ensureLight(int color) {

        float hsl[] = new float[3];
        rgbToHsl(color, hsl);

        if (hsl[2] < 0.54f) {
            hsl[2] = 0.54f;
            color = hslToRgb(hsl);
        }
        return color;
    }

    private static void rgbToHsl(int color, float hsl[]) {

        float r = Color.red(color) / 255.f;
        float g = Color.green(color) / 255.f;
        float b = Color.blue(color) / 255.f;
        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float h;
        float s;
        float l;

        h = s = l = (max + min) / 2;

        if (max == min) {
            h = s = 0;
        } else {
            float delta = max - min;
            s = l > 0.5f ? delta / (2f - max - min) : delta / (max + min);
            if (max == r) {
                h = (g - b) / delta;
            } else if (max == g) {
                h = (b - r) / delta + 2f;
            } else if (max == b) {
                h = (r - g) / delta + 4f;
            }
            h *= 60f;
            if (h < 0f) {
                h += 360f;
            } else if (h > 360f) {
                h -= 360f;
            }
        }
        hsl[0] = h;
        hsl[1] = s;
        hsl[2] = l;
    }

    private static int hslToRgb(float hsl[]) {
        float h = hsl[0] / 360.f;
        float s = hsl[1];
        float l = hsl[2];

        int r;
        int g;
        int b;

        if (s == 0) {
            r = g = b = (int) (l * 255f);
        } else {
            float t1 = l < 0.5 ? l * (1 + s) : l + s - l * s;
            float t2 = 2 * l - t1;
            r = (int) (hueToMagnitude(t1, t2, h + 1 / 3f) * 255f);
            g = (int) (hueToMagnitude(t1, t2, h) * 255f);
            b = (int) (hueToMagnitude(t1, t2, h - 1 / 3f) * 255f);
        }
        return Color.rgb(r, g, b);
    }

    private static float hueToMagnitude(float t1, float t2, float h) {
        h += h > 1 ? -1 : h < 0 ? 1 : 0;
        float result = 0;
        if (h * 6 < 1) {
            result = t2 + (t1 - t2) * 6f * h;
        } else if (h * 2 < 1) {
            result = t1;
        } else if (h * 3 < 2) {
            result = t2 + (t1 - t2) * (2f / 3f - h) * 6f;
        } else {
            result = t2;
        }
        return result;
    }

    public static class UnsaturatedRgbInHslEvaluator implements android.animation.TypeEvaluator {

        private final static int H = 0;
        private final static int S = 1;
        private final static int L = 2;

        @Override
        public Object evaluate(float proportion, Object source, Object destination) {

            float[] src = new float[3];
            float[] dst = new float[3];
            float[] result = new float[3];

            rgbToHsl((Integer) source, src);
            rgbToHsl((Integer) destination, dst);

            //this evaluator assumes that the start color does not have saturation. and an instantaneous jump in hue will not be visible.
            result[H] = dst[H];
            result[S] = interpolate(src[S], dst[S], proportion);
            result[L] = interpolate(src[L], dst[L], proportion);

            return hslToRgb(result);
        }

        private float interpolate(float a, float b, float proportion) {
            return (a + ((b - a) * proportion));
        }
    }

    public static int getBackgroundColor(View view){
        int color = Color.TRANSPARENT;
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
        }
        return color;
    }

    public static boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }
}
