package me.akulakovsky.okcentre.v2.models;

import java.math.BigDecimal;
import java.math.RoundingMode;

import me.akulakovsky.okcentre.v2.utils.Utils;

public class WeatherPacket {

    private int sensorNumber;
    private double temperature;
    private double windSpeed;

    public WeatherPacket(int sensorNumber, double temperature, double windSpeed) {
        this.sensorNumber = sensorNumber;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
    }

    public int getSensorNumber() {
        return sensorNumber;
    }

    public double getTemperature() {
        return Utils.round(temperature, 2);
    }

    public double getWindSpeed() {
        return Utils.round(windSpeed, 1);
    }
}