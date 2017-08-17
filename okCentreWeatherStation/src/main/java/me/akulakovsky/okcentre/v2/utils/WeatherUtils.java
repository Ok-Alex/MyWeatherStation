package me.akulakovsky.okcentre.v2.utils;

import me.akulakovsky.okcentre.v2.models.WeatherPacket;

public class WeatherUtils {

    private static final String TAG = "WeatherUtils";

    public static WeatherPacket processPacket(byte[] packet) {
        WeatherPacket weatherPacket = null;

        if (packet[0] == Constants.START_DELIMITER){
            int portNumber = packet[15];
            long windData = (long)((packet[16] << 8) + packet[17]);
            double windSpeed = Math.abs(windData) * Constants.WIND_SLOPE / 100f;

            LogUtil.logDebug(TAG, "WIND DATA = " + windData + " , SPEED = " + windSpeed);

            byte[] tempData = new byte[2];
            tempData[0] = packet[19];
            tempData[1] = packet[18];

            //short temp_check = ByteUtils.toShort(tempData);
            short temp_check = ByteUtils.bytesToShort(tempData);// TODO TEST!
            //temp_check = ByteUtils.swapBytes(temp_check); //TODO TEST!
            double temp = ((temp_check * Constants.TEMP_SLOPE) + Constants.TEMP_INTERCEPT);
            temp = ((temp - 32)*5)/9;

            weatherPacket = new WeatherPacket(portNumber, temp, windSpeed);
        }

        return weatherPacket;
    }

}
