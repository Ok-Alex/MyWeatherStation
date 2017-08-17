package me.akulakovsky.okcentre.v2.helpers;

import me.akulakovsky.okcentre.v2.utils.LogUtil;

/**
 * Created by Ok-Alex on 7/19/17.
 */

public class ExponentialMovingAverage {
    private double alpha;
    private Double oldValue;
    public ExponentialMovingAverage(double alpha) {
        this.alpha = alpha;
    }

    public double average(double value) {
        if (oldValue == null) {
            oldValue = value;
            return value;
        }
        double newValue = oldValue + alpha * (value - oldValue);
        LogUtil.logDebug("SMA", "NEW " + value + ", OLD = " + oldValue + ", AVG = " + newValue);
        oldValue = newValue;
        return newValue;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }
}