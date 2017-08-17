package me.akulakovsky.okcentre.v2.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import me.akulakovsky.okcentre.v2.HomeActivity;
import me.akulakovsky.okcentre.v2.R;
import me.akulakovsky.okcentre.v2.models.WeatherPacket;
import me.akulakovsky.okcentre.v2.utils.Constants;
import me.akulakovsky.okcentre.v2.utils.LogUtil;
import me.akulakovsky.okcentre.v2.utils.WeatherUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WeatherService extends Service {

    private final IBinder mBinder = new MyBinder();

    private FT_Device xBeeReceiver;

    private double[] currentTemp = new double[4];
    private double[] currentWind = new double[4];

    private boolean readingOn = false;
    private PowerManager.WakeLock wakeLock;

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.logDebug(TAG, "onCreate()");

        for (int i = 0; i < currentTemp.length; i++) {
            currentTemp[i] = 0;
            currentWind[i] = 0;
        }

        startWeatherService();
    }

    public void startWeatherService() {
        // Create D2xx class
        try {
            D2xxManager devManager = D2xxManager.getInstance(this);
            int devCount = devManager.createDeviceInfoList(this);
            if (devCount != 0){
                xBeeReceiver = devManager.openByIndex(this, 0);
                if (xBeeReceiver != null) {
                    xBeeReceiver.setBaudRate(57600);
                    LogUtil.logDebug(TAG, "Port opened!");

                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    wakeLock = pm.newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK, "WEATHER_STATION_LOCK");
                    wakeLock.acquire();


                    startAsForeground();
                    startReading();
                } else {
                    Toast.makeText(WeatherService.this, "Can't init device! Please try again!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(WeatherService.this, "Can't init device! Please try again!", Toast.LENGTH_SHORT).show();
            }
        } catch (D2xxManager.D2xxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.release();
        }
        stopReading();
        super.onDestroy();
    }

    private void startAsForeground() {
        Notification note = buildForegroundNotification();
        note.flags|= Notification.FLAG_NO_CLEAR;
        startForeground(1337, note);
    }

    private Notification buildForegroundNotification() {
        NotificationCompat.Builder b=new NotificationCompat.Builder(this);


        Intent i = new Intent(this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi= PendingIntent.getActivity(this, 0, i, 0);

        b.setOngoing(true)
                .setContentTitle("Weather Station")
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker("Receiving...")
                .setContentIntent(pi);

        return(b.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.logDebug(TAG, "onStartCommand()");
        return START_STICKY;
    }

    private void startReading(){
        if (xBeeReceiver != null) {
            xBeeReceiver.restartInTask();
            readingOn = true;
            new MyThread().start();
        }
    }

    private void stopReading(){
        if (xBeeReceiver != null) {
            try {
                xBeeReceiver.stopInTask();
            } catch (NullPointerException e) {
                Log.e(TAG, "Failed to stop device. Is it plugged in?");
            }
            readingOn = false;
        }
    }

    private class MyThread extends Thread {
        @Override
        public void run() {
            while (readingOn){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (xBeeReceiver){
                    int bytesAvailable = xBeeReceiver.getQueueStatus();

                    if (bytesAvailable > 0){
                        if (bytesAvailable == Constants.PACKET_LENGTH){
                            byte[] readData = new byte[Constants.PACKET_LENGTH];
                            xBeeReceiver.read(readData, bytesAvailable);
                            WeatherPacket weatherPacket = WeatherUtils.processPacket(readData);

                            if (weatherPacket != null){
                                LogUtil.logDebug(TAG, weatherPacket.getSensorNumber() + "#: TEMP = " + weatherPacket.getTemperature() + ", WIND = " + weatherPacket.getWindSpeed());
//                                switch (weatherPacket.getSensorNumber()){
//                                    case 0:
//                                        currentTemp[0] = weatherPacket.getTemperature();
//                                        currentWind[0] = weatherPacket.getWindSpeed();
//                                        break;
//
//                                    case 1:
//                                        currentTemp[1] = weatherPacket.getTemperature();
//                                        currentWind[1] = weatherPacket.getWindSpeed();
//                                        break;
//
//                                    case 2:
//                                        currentTemp[2] = weatherPacket.getTemperature();
//                                        currentWind[2] = weatherPacket.getWindSpeed();
//                                        break;
//
//                                    case 3:
//                                        currentTemp[3] = weatherPacket.getTemperature();
//                                        currentWind[3] = weatherPacket.getWindSpeed();
//                                        break;
//                                }
                                currentTemp[weatherPacket.getSensorNumber()] = weatherPacket.getTemperature();
                                currentWind[weatherPacket.getSensorNumber()] = weatherPacket.getWindSpeed();
                            }

                        } else {
                            byte[] tempBuffer = new byte[512];
                            xBeeReceiver.read(tempBuffer, bytesAvailable);
                        }
                    }
                }
            }
        }
    }

    public double[] getCurrentTemp() {
        return currentTemp;
    }

    public double[] getCurrentWind() {
        return currentWind;
    }

    public boolean isReading() {
        return readingOn;
    }

    public class MyBinder extends Binder {
        public WeatherService getService() {
            return WeatherService.this;
        }
    }

    private static final String TAG = WeatherService.class.getSimpleName();
}