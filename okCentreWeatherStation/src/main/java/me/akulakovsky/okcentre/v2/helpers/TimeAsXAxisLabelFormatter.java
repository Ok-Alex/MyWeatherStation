package me.akulakovsky.okcentre.v2.helpers;

import android.content.Context;

import com.jjoe64.graphview.DefaultLabelFormatter;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by Ok-Alex on 7/19/17.
 */

public class TimeAsXAxisLabelFormatter extends DefaultLabelFormatter {
    /**
     * the date format that will convert
     * the unix timestamp to string
     */
    protected final DateFormat mDateFormat;

    /**
     * calendar to avoid creating new date objects
     */
    protected final Calendar mCalendar;

    /**
     * create the formatter with the Android default date format to convert
     * the x-values.
     *
     * @param context the application context
     */
    public TimeAsXAxisLabelFormatter(Context context) {
        mDateFormat = android.text.format.DateFormat.getTimeFormat(context);
        mCalendar = Calendar.getInstance();
    }

    /**
     * create the formatter with your own custom
     * date format to convert the x-values.
     *
     * @param context the application context
     * @param dateFormat custom date format
     */
    public TimeAsXAxisLabelFormatter(Context context, DateFormat dateFormat) {
        mDateFormat = dateFormat;
        mCalendar = Calendar.getInstance();
    }

    /**
     * formats the x-values as date string.
     *
     * @param value raw value
     * @param isValueX true if it's a x value, otherwise false
     * @return value converted to string
     */
    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            // format as date
            mCalendar.setTimeInMillis((long) value);
            return mDateFormat.format(mCalendar.getTimeInMillis());
        } else {
            return super.formatLabel(value, isValueX);
        }
    }
}
