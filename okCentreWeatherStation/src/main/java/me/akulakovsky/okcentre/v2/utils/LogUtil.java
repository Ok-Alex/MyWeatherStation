package me.akulakovsky.okcentre.v2.utils;

import android.util.Log;

/**
 * User: Alexander Kulakovsky
 * Date: 17.07.13
 * Time: 13:12
 * E-Mail: akulakovskyy@smartzeit.com
 */
public class LogUtil {

    public static final boolean LOGGING_ENABLED = true;

    public static void logDebug(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.d(tag, message);
        }
    }
}
