package com.example.chooselibrary.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * 尺寸工具类
 */
public class SizeUtils {

    /**
     * 屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 屏幕高度
     *
     * @return
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * dp转px
     * @param context
     * @param value
     * @return
     */
    public static int dp2px(Context context, int value) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density = metrics.density;
        return (int) (density * value + 0.5f);
    }

    /**
     * px转dp
     * @param context
     * @param value
     * @return
     */
    public static int px2dp(Context context, int value) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density = metrics.density;
        return (int) (density / value + 0.5f);
    }

}
