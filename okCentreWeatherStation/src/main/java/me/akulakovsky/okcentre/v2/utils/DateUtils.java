package me.akulakovsky.okcentre.v2.utils;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    public static final String FORMAT_FULL = "yyyy-MM-dd hh:mm:ss";
    public static final String FORMAT_TIME = "hh:mm";
    public static final String FORMAT_DATE = "yyyy-MM-dd";

    //returns the date string (without time) in system local format
    public static String getCurrentDate(Context context){
        Date date = new Date();
        long when = date.getTime();
        int flags = 0;
        //flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
        flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
        flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
        flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;

        return android.text.format.DateUtils.formatDateTime(context,
                when + TimeZone.getDefault().getOffset(when), flags);
    }

    public static String getFullCurrentTime(String format) {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        return df.format(format, new java.util.Date()).toString();
    }

    public static String getFullCurrentTimeWithOffset(String format, long millis) {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        return df.format(format, new java.util.Date(millis)).toString();
    }

    public static String dateToString(Date date, String format) {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        return df.format(format, date).toString();
    }

    public static Date convertToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

}