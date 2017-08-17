package me.akulakovsky.okcentre.v2.utils;

public class Constants {

    // Anemometer - calibrated for old style cup anemometers with modified factory circuit board----------------------
    // May need to change for newer style cup anemometers with custom Hall effect sensor installation
    public static final float WIND_SLOPE = 0.042117f; // m/s per Hz

    // National Semiconductor LM71 digital temperature sensor --------------------------------------------------------
    public static final float LM71_MAX = -40;                       // Minimum valid temperature from LM71 sensor, in degrees F         //
    public static final float LM71_MIN = 302;                       // Maximum valid temperature from LM71 sensor, in degrees F         //
    public static final float TEMP_SLOPE = 0.01406250f;       // degrees F per count                                              //
    public static final float TEMP_INTERCEPT = 31.95781250f;  // degeees F                                                        //
    // ---------------------------------------------------------------------------------------------------------------

    public static final byte START_DELIMITER = 0x7E;
    public static final int PACKET_LENGTH = 21;

}