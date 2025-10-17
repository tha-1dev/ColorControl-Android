package com.maassoft.colorcontrol.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import androidx.core.content.ContextCompat;

public class ResourceUtils {
    
    public static int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.getResources().getDisplayMetrics()
        );
    }
    
    public static int spToPx(Context context, float sp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.getResources().getDisplayMetrics()
        );
    }
    
    public static int getColor(Context context, int colorResId) {
        try {
            return ContextCompat.getColor(context, colorResId);
        } catch (Resources.NotFoundException e) {
            return 0xFF000000; // Return black as fallback
        }
    }
    
    public static Drawable getDrawable(Context context, int drawableResId) {
        try {
            return ContextCompat.getDrawable(context, drawableResId);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }
    
    public static String getString(Context context, int stringResId) {
        try {
            return context.getString(stringResId);
        } catch (Resources.NotFoundException e) {
            return "";
        }
    }
    
    public static String getString(Context context, int stringResId, Object... formatArgs) {
        try {
            return context.getString(stringResId, formatArgs);
        } catch (Resources.NotFoundException e) {
            return "";
        }
    }
    
    public static int getThemeColor(Context context, int attrResId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrResId, typedValue, true);
        return typedValue.data;
    }
    
    public static boolean isDarkTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorBackground, typedValue, true);
        int backgroundColor = typedValue.data;
        
        // Simple brightness calculation
        int r = (backgroundColor >> 16) & 0xFF;
        int g = (backgroundColor >> 8) & 0xFF;
        int b = backgroundColor & 0xFF;
        
        double brightness = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
        return brightness < 0.5;
    }
    
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }
    
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
    
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
}