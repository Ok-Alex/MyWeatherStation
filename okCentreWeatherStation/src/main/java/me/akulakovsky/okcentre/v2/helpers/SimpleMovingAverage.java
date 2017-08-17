package me.akulakovsky.okcentre.v2.helpers;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Ok-Alex on 7/19/17.
 */

public class SimpleMovingAverage {

    private final Queue<Double> window = new LinkedList<>();
    private final int period;
    private double sum;

    public SimpleMovingAverage(int period) {
        assert period > 0 : "Period must be a positive integer";
        this.period = period;
    }

    private void newNum(double num) {
        sum += num;
        window.add(num);
        if (window.size() > period) {
            sum -= window.remove();
        }
    }

    public double getAvg(double num) {
        newNum(num);
        if (window.isEmpty()) return 0; // technically the average is undefined
        return sum / window.size();
    }
}
