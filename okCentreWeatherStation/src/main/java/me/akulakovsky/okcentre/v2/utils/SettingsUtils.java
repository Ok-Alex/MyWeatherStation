package me.akulakovsky.okcentre.v2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import me.akulakovsky.okcentre.v2.R;

public class SettingsUtils {

    public static final String PREF_UPD_INTERVAL = "update_interval";
    public static final String PREF_GRAPH_LENGTH = "graph_length";

    public static final String PREF_BLUE_SENSOR = "blue_sensor";
    public static final String PREF_RED_SENSOR = "red_sensor";
    public static final String PREF_BLACK_SENSOR = "black_sensor";
    public static final String PREF_GREEN_SENSOR = "green_sensor";

    public static final String PREF_EMA_BLUE_SENSOR = "PREF_EMA_BLUE_SENSOR";
    public static final String PREF_EMA_RED_SENSOR = "PREF_EMA_RED_SENSOR";
    public static final String PREF_EMA_BLACK_SENSOR = "PREF_EMA_BLACK_SENSOR";
    public static final String PREF_EMA_GREEN_SENSOR = "PREF_EMA_GREEN_SENSOR";

    public static final String PREF_NEAR_SENSOR = "near_sensor";
    public static final String PREF_THEME = "theme";
    public static final String PREF_MAX_LENGTH = "graph_length";
    public static final String PREF_EMA_TEMP_ALPHA = "PREF_EMA_TEMP_ALPHA";
    public static final String PREF_EMA_WIND_ALPHA = "PREF_EMA_WIND_ALPHA";

    private static SettingsUtils instance;

    private SharedPreferences prefs;

    public SettingsUtils(Context context) {
        this.prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    public static SettingsUtils getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsUtils(context);
        }
        return instance;
    }

    public void putValue(String key, String value) {
        prefs.edit().putString(key, value).commit();
    }

    public void putValue(String key, int value) {
        prefs.edit().putInt(key, value).commit();
    }

    public void putValue(String key, boolean value) {
        prefs.edit().putBoolean(key, value).commit();
    }

    public void putValue(String key, long value) {
        prefs.edit().putLong(key, value).commit();
    }

    public void putValue(String key, float value) {
        prefs.edit().putFloat(key, value).commit();
    }

    public String getValue(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public int getValue(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public boolean getValue(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public long getValue(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }

    public float getValue(String key, float defaultValue) {
        return prefs.getFloat(key, defaultValue);
    }
}
