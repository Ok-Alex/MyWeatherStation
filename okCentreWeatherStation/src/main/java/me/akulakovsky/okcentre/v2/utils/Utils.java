package me.akulakovsky.okcentre.v2.utils;

import android.content.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Ok-Alex on 7/19/17.
 */

public class Utils {

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
