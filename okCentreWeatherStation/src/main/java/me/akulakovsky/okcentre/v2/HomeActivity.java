package me.akulakovsky.okcentre.v2;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.*;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.ftdi.j2xx.D2xxManager;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import me.akulakovsky.okcentre.v2.fragments.PrefsFragment;
import me.akulakovsky.okcentre.v2.helpers.ExponentialMovingAverage;
import me.akulakovsky.okcentre.v2.helpers.GraphHelper;
import me.akulakovsky.okcentre.v2.helpers.TimeAsXAxisLabelFormatter;
import me.akulakovsky.okcentre.v2.services.WeatherService;
import me.akulakovsky.okcentre.v2.utils.LogUtil;
import me.akulakovsky.okcentre.v2.utils.SettingsUtils;
import me.akulakovsky.okcentre.v2.utils.Utils;

public class HomeActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

    private static final double DEFAULT_TEMP_COMPRESSION = 0.1;

    private DialogFragment prefsFragment;

    private GraphView tempGraph;
    private GraphView windGraph;

    private int graphLength = 1200;
    private int updateInterval = 2;

    public int currentTick = -1;

    private double[][] tempValuesHolder = new double[4][updateInterval];
    private double[][] windValuesHolder = new double[4][updateInterval];

    private List<LineGraphSeries<DataPoint>> tempSeries = new ArrayList<>();
    private List<LineGraphSeries<DataPoint>> windSeries = new ArrayList<>();

    private double[] tempSyncOffset = new double[4];
    private double[] windSyncOffset = new double[4];

    public WeatherService weatherService;
    private MyServiceConnection myServiceConnection = new MyServiceConnection();

    private Handler tick_Handler = new Handler();
    private MyRunnable tick_thread = new MyRunnable();

    //value views
    private TextView tvCurrentTemp;
    private TextView tvCurrentWind;

    private TextView tvMaxTemp;
    private TextView tvMinTemp;
    private TextView tvAvgTemp;
    private TextView tvDiffTemp;
    private TextView tvMaxWind;
    private TextView tvMinWind;
    private TextView tvAvgWind;
    private TextView tvDiffWind;

    private int nearSensor;
    private boolean isPaused = true;
    private SettingsUtils settingsUtils;

    private int theme;

    private int maxGraphLength;

    private PowerManager.WakeLock wakeLock;

    private Button tempPlus;
    private Button tempMinus;
    private Button windPlus;
    private Button windMinus;


    private boolean[] sensorsSmooth = new boolean[] {true, true, true, true};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        settingsUtils = SettingsUtils.getInstance(this);
        theme = settingsUtils.getValue(SettingsUtils.PREF_THEME, 0);
        if (theme == 1){
            setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        LogUtil.logDebug(TAG, "onCreate()");
        setContentView(R.layout.main);
        getActionBar().setDisplayShowTitleEnabled(false);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        addSeries();
        updateSettings();
        initDevice();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "WEATHER_STATION_LOCK_ACTIVITY");
        wakeLock.acquire();
    }

    private void initDevice() {
        LogUtil.logDebug(TAG, "initDevice()");
        try {
            D2xxManager devManager = D2xxManager.getInstance(this);
            devManager.createDeviceInfoList(this);
        } catch (D2xxManager.D2xxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        LogUtil.logDebug(TAG, "onDestroy()");
        unBindService();
        wakeLock.release();
        super.onDestroy();
    }

    private void startService() {
        LogUtil.logDebug(TAG, "startService()");
        Intent weatherServiceIntent = new Intent(this, WeatherService.class);
        bindService(weatherServiceIntent, myServiceConnection, BIND_AUTO_CREATE);
    }

    private void unBindService() {
        LogUtil.logDebug(TAG, "unBindService()");
        if (weatherService != null) {
            unbindService(myServiceConnection);
        }
    }

    private void initViews() {
        LogUtil.logDebug(TAG, "initViews()");
        RelativeLayout tempGraphHolder = (RelativeLayout) findViewById(R.id.temp_graph);
        RelativeLayout windGraphHolder = (RelativeLayout) findViewById(R.id.wind_graph);

        tempGraph = new GraphView(this);
        windGraph = new GraphView(this);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins((int) Utils.pxFromDp(this, 20), 0 ,0 ,0);
        tempGraph.setLayoutParams(layoutParams);
        windGraph.setLayoutParams(layoutParams);

        tempGraph.getViewport().setScrollable(true);
        windGraph.getViewport().setScrollable(true);
        tempGraph.getViewport().setScalable(false);
        windGraph.getViewport().setScalable(false);

        tempGraphHolder.addView(tempGraph);
        windGraphHolder.addView(windGraph);

        tempGraphHolder.bringChildToFront(findViewById(R.id.temp_values));
        windGraphHolder.bringChildToFront(findViewById(R.id.wind_values));


        tvCurrentTemp = (TextView) findViewById(R.id.currentTemp);
        tvCurrentWind = (TextView) findViewById(R.id.currentWind);

        tvMaxTemp = findViewById(R.id.temp_max);
        tvMinTemp = findViewById(R.id.temp_min);
        tvAvgTemp = findViewById(R.id.temp_avg);
        tvDiffTemp = findViewById(R.id.tempDiff);

        tvMaxWind = findViewById(R.id.wind_max);
        tvMinWind = findViewById(R.id.wind_min);
        tvAvgWind = findViewById(R.id.wind_avg);
        tvDiffWind = findViewById(R.id.windDiff);

        tempPlus = findViewById(R.id.temp_plus);
        tempMinus = findViewById(R.id.temp_minus);
        tempPlus.setOnClickListener(this);
        tempMinus.setOnClickListener(this);
        tempPlus.setOnLongClickListener(this);
        tempMinus.setOnLongClickListener(this);

        windPlus = findViewById(R.id.wind_plus);
        windMinus = findViewById(R.id.wind_minus);
        windPlus.setOnClickListener(this);
        windMinus.setOnClickListener(this);
        windPlus.setOnLongClickListener(this);
        windMinus.setOnLongClickListener(this);
    }

    private void addSeries() {
        LogUtil.logDebug(TAG, "addSeries()");

        for (int i = 0; i < 4; i++) {
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            GraphHelper.setGraphViewSeriesStyle(series, i, theme);
            tempSeries.add(series);

            series = new LineGraphSeries<>();
            GraphHelper.setGraphViewSeriesStyle(series, i, theme);
            windSeries.add(series);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);

        MenuItem playPauseItem = menu.findItem(R.id.pause);
        if (isPaused) {
            playPauseItem.setIcon(getResources().getDrawable(R.drawable.ic_play));
            playPauseItem.setTitle("Play");
        } else {
            playPauseItem.setIcon(getResources().getDrawable(R.drawable.ic_pause));
            playPauseItem.setTitle("Pause");
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                showPrefsDialog();
                break;

            case R.id.sync:
                Toast.makeText(this, "Sync sensors!", Toast.LENGTH_SHORT).show();
                syncSensors();
                break;

            case R.id.pause:
                LogUtil.logDebug(TAG, "Pause clicked!");
                if (isPaused) {
                    startHandler(0);
                } else {
                    stopHandler();
                }
                break;
            case R.id.graph_length_5:
                setGraphLengthSize(5);
                break;

            case R.id.graph_length_10:
                setGraphLengthSize(10);
                break;

            case R.id.graph_length_15:
                setGraphLengthSize(15);
                break;

            case R.id.graph_length_20:
                setGraphLengthSize(20);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setGraphLengthSize(int minutes) {
        settingsUtils.putValue(SettingsUtils.PREF_GRAPH_LENGTH, minutes);
        updateSettings();
    }

    private void syncSensors() {
        LogUtil.logDebug(TAG, "syncSensors()");
        if (weatherService != null) {
            stopHandler();
            double[] currentTemp = weatherService.getCurrentTemp();
            double[] currentWind = weatherService.getCurrentWind();

            double zeroTemp = findMax(currentTemp);
            double zeroWind = findMax(currentWind);

            for (int i = 0; i < tempSyncOffset.length; i++) {
                if (currentTemp[i] > zeroTemp && currentTemp[i] != 0.0) {
                    tempSyncOffset[i] = currentTemp[i] - zeroTemp;
                } else if (currentTemp[i] < zeroTemp && currentTemp[i] != 0.0) {
                    tempSyncOffset[i] = zeroTemp - currentTemp[i];
                }
            }

            for (int i = 0; i < windSyncOffset.length; i++) {
                if (currentWind[i] > zeroWind && currentWind[i] != 0.0) {
                    windSyncOffset[i] = currentWind[i] - zeroWind;
                } else if (currentWind[i] < zeroWind && currentWind[i] != 0.0) {
                    windSyncOffset[i] = zeroWind - currentTemp[i];
                }
            }
            LogUtil.logDebug(TAG, "Sync done!");
            LogUtil.logDebug(TAG, "TEMP OFFSET: " + tempSyncOffset[0] + ", " + tempSyncOffset[1] + ", " + tempSyncOffset[2] + ", " + tempSyncOffset[3]);
            LogUtil.logDebug(TAG, "WIND OFFSET: " + windSyncOffset[0] + ", " + windSyncOffset[1] + ", " + windSyncOffset[2] + ", " + windSyncOffset[3]);
            Toast.makeText(this, "TEMP OFFSET: " + tempSyncOffset[0] + ", " + tempSyncOffset[1] + ", " + tempSyncOffset[2] + ", " + tempSyncOffset[3], Toast.LENGTH_SHORT).show();

            startHandler(1000);
        }
    }

    private void showPrefsDialog() {
        LogUtil.logDebug(TAG, "showPrefsDialog()");
        stopHandler();
        getPrefsFragment().show(getFragmentManager(), "PREFS");
    }

    public void updateSettings() {
        LogUtil.logDebug(TAG, "updateSettings()");
        int graphSizeMinutes = settingsUtils.getValue(SettingsUtils.PREF_GRAPH_LENGTH, 10);
        graphLength = graphSizeMinutes * 60 * 1000;

        tempGraph.getGridLabelRenderer().setNumHorizontalLabels(graphSizeMinutes + 1);
        windGraph.getGridLabelRenderer().setNumHorizontalLabels(graphSizeMinutes + 1);
        tempGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        windGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        //tempGraph.getGridLabelRenderer().setNumVerticalLabels(3);
        //windGraph.getGridLabelRenderer().setNumVerticalLabels(3);
        tempGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.VERTICAL);
        windGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.VERTICAL);
        tempGraph.setTitleTextSize(Utils.pxFromDp(this, 8));
        windGraph.setTitleTextSize(Utils.pxFromDp(this, 8));
        tempGraph.getGridLabelRenderer().setTextSize(Utils.pxFromDp(this, 8));
        windGraph.getGridLabelRenderer().setTextSize(Utils.pxFromDp(this, 8));

        tempGraph.getGridLabelRenderer().setLabelFormatter(new TimeAsXAxisLabelFormatter(this));
        windGraph.getGridLabelRenderer().setLabelFormatter(new TimeAsXAxisLabelFormatter(this));

        tempGraph.getGridLabelRenderer().setHumanRounding(false);
        windGraph.getGridLabelRenderer().setHumanRounding(false);

        tempGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        tempGraph.setTitle("TEMPERATURE (" + graphSizeMinutes + " minutes)");
        windGraph.setTitle("WIND SPEED (" + graphSizeMinutes + " minutes)");
        tempGraph.getGridLabelRenderer().setLabelVerticalWidth((int) Utils.pxFromDp(this, 40));
        tempGraph.getGridLabelRenderer().setLabelHorizontalHeight((int) Utils.pxFromDp(this, 1));
        windGraph.getGridLabelRenderer().setLabelVerticalWidth((int) Utils.pxFromDp(this, 40));
        windGraph.getGridLabelRenderer().setLabelHorizontalHeight((int) Utils.pxFromDp(this, 15));

        updateInterval = settingsUtils.getValue(SettingsUtils.PREF_UPD_INTERVAL, 1);
        tempGraph.getViewport().setXAxisBoundsManual(true);
        tempGraph.getViewport().setMinX(0);
        tempGraph.getViewport().setMaxX(graphLength);
        windGraph.getViewport().setXAxisBoundsManual(true);
        windGraph.getViewport().setMinX(0);
        windGraph.getViewport().setMaxX(graphLength);

        tempGraph.getViewport().setOnXAxisBoundsChangedListener(new Viewport.OnXAxisBoundsChangedListener() {
            @Override
            public void onXAxisBoundsChanged(double minX, double maxX, Reason reason) {
                if (reason == Reason.SCROLL) {
                    windGraph.getViewport().setMinX(minX);
                    windGraph.getViewport().setMaxX(maxX);
                }
            }
        });

        boolean s0 = settingsUtils.getValue(SettingsUtils.PREF_BLACK_SENSOR, false);
        boolean s1 = settingsUtils.getValue(SettingsUtils.PREF_RED_SENSOR, false);
        boolean s2 = settingsUtils.getValue(SettingsUtils.PREF_BLUE_SENSOR, false);
        boolean s3 = settingsUtils.getValue(SettingsUtils.PREF_GREEN_SENSOR, false);

        sensorsSmooth[0] = settingsUtils.getValue(SettingsUtils.PREF_EMA_BLACK_SENSOR, true);
        sensorsSmooth[1] = settingsUtils.getValue(SettingsUtils.PREF_EMA_RED_SENSOR, true);
        sensorsSmooth[2] = settingsUtils.getValue(SettingsUtils.PREF_EMA_BLUE_SENSOR, true);
        sensorsSmooth[3] = settingsUtils.getValue(SettingsUtils.PREF_EMA_GREEN_SENSOR, true);

        tempGraph.removeAllSeries();
        windGraph.removeAllSeries();

        enableSeries(0, s0);
        enableSeries(1, s1);
        enableSeries(2, s2);
        enableSeries(3, s3);

        tempValuesHolder = new double[4][updateInterval];
        windValuesHolder = new double[4][updateInterval];

        nearSensor = settingsUtils.getValue(SettingsUtils.PREF_NEAR_SENSOR, -1);

        maxGraphLength = settingsUtils.getValue(SettingsUtils.PREF_MAX_LENGTH, graphLength * 2);

        double alphaTemp = settingsUtils.getValue(SettingsUtils.PREF_EMA_TEMP_ALPHA, 0.1f);
        double alphaWind = settingsUtils.getValue(SettingsUtils.PREF_EMA_WIND_ALPHA, 0.01f);


        emaTemp.clear();
        emaWind.clear();
        for (int i = 0; i < 4; i++) {
            emaTemp.add(new ExponentialMovingAverage(alphaTemp));
            emaWind.add(new ExponentialMovingAverage(alphaWind));
        }
    }

    private void enableSeries(int sensorNumber, boolean isEnabled) {
        LogUtil.logDebug(TAG, "enableSeries()");
        if (isEnabled) {
            tempGraph.addSeries(tempSeries.get(sensorNumber));
            windGraph.addSeries(windSeries.get(sensorNumber));
        } else {
            tempGraph.removeSeries(tempSeries.get(sensorNumber));
            windGraph.removeSeries(windSeries.get(sensorNumber));
        }
    }

    private DialogFragment getPrefsFragment() {
        LogUtil.logDebug(TAG, "getPrefsFragment()");
        if (prefsFragment == null) {
            prefsFragment = new PrefsFragment();
        }
        return prefsFragment;
    }

    private void setCustomYTempBounds(GraphView graphView, boolean increase) {
        double tempMax = graphView.getViewport().getMaxY(false);
        double tempMin = graphView.getViewport().getMinY(false);
        graphView.getViewport().setYAxisBoundsManual(true);

        graphView.getViewport().setMaxY(increase? tempMax + DEFAULT_TEMP_COMPRESSION : tempMax - DEFAULT_TEMP_COMPRESSION);
        graphView.getViewport().setMinY(increase ? tempMin - DEFAULT_TEMP_COMPRESSION : tempMin + DEFAULT_TEMP_COMPRESSION);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.temp_plus:
                setCustomYTempBounds(tempGraph, true);
                break;
            case R.id.temp_minus:
                setCustomYTempBounds(tempGraph, false);
                break;
            case R.id.wind_plus:
                setCustomYTempBounds(windGraph, true);
                break;
            case R.id.wind_minus:
                setCustomYTempBounds(windGraph, false);
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.temp_plus:
            case R.id.temp_minus:
                tempGraph.getViewport().setYAxisBoundsManual(false);
                tempGraph.getViewport().setYAxisBoundsManual(false);
                break;
            case R.id.wind_plus:
            case R.id.wind_minus:
                windGraph.getViewport().setYAxisBoundsManual(false);
                windGraph.getViewport().setYAxisBoundsManual(false);
                break;
        }
        return true;
    }

    private DecimalFormat decimalFormat = new DecimalFormat("#.#");
    private List<ExponentialMovingAverage> emaTemp = new ArrayList<>();
    private List<ExponentialMovingAverage> emaWind = new ArrayList<>();


    private class MyRunnable implements Runnable {

        public void run() {
            LogUtil.logDebug(TAG, "run()");
            currentTick++;

            double[] currentTemp = weatherService.getCurrentTemp();
            double[] currentWind = weatherService.getCurrentWind();

            //LogUtil.logDebug(TAG, "VIEWPORT SIZE = " + tempGraph.getViewPort()[1]);

            if (nearSensor == -1) {
                tvCurrentTemp.setText(decimalFormat.format(findMax(currentTemp)) + " \u00B0C");
                tvCurrentWind.setText(decimalFormat.format(findMax(currentWind)) + " m/s");
            } else {
                tvCurrentTemp.setText(decimalFormat.format(currentTemp[nearSensor]) + " \u00B0C");
                tvCurrentWind.setText(decimalFormat.format(currentWind[nearSensor]) + " m/s");
            }

            for (int i = 0; i < 4; i++) {
                double avgTemp = findAvg(currentTemp);
                double avgWind = findAvg(currentWind);

                if (currentTemp[i] == 0) {
                    currentTemp[i] = avgTemp;
                }

//                if (currentWind[i] == 0) {
//                    currentWind[i] = avgWind;
//                }

                Date now = new Date();

                tempSeries.get(i).appendData(new DataPoint(now, sensorsSmooth[i] ? (emaTemp.get(i).average(currentTemp[i] + tempSyncOffset[i])) : (currentTemp[i] + tempSyncOffset[i])), true, graphLength*2);
                windSeries.get(i).appendData(new DataPoint(now, sensorsSmooth[i] ? (emaWind.get(i).average(currentWind[i] + windSyncOffset[i])) : (currentWind[i] + windSyncOffset[i])), true, graphLength*2);
            }

            double tempMax = tempGraph.getViewport().getMaxY(false);
            double tempMin = tempGraph.getViewport().getMinY(false);

            double windMax = windGraph.getViewport().getMaxY(false);
            double windMin = windGraph.getViewport().getMinY(false);

            if (nearSensor != -1) {
                List<DataPoint> tempDataPoints = getViewPortPoints(tempSeries.get(nearSensor));
                List<DataPoint> windDataPoints = getViewPortPoints(windSeries.get(nearSensor));
                double tempDiff = findDiff(tempDataPoints);
                double windDiff = findDiff(windDataPoints);
                tvDiffTemp.setText(decimalFormat.format(tempDiff) + " \u00B0C");
                tvDiffWind.setText(decimalFormat.format(windDiff) + " m/s");

                if (Utils.round(tempDiff, 1) > Utils.round(lastTempDiff, 1)) {
                    //temp diff is increased more or equal!!!
                    if (tempDataPoints.size() >= graphLength / 1000) {
                        //if it increased after view port is filled then beeep!!!
                        beep();
                    }
                }
                lastTempDiff = Utils.round(tempDiff, 1);
            }

            tvMaxTemp.setText(decimalFormat.format(tempMax));
            tvMinTemp.setText(decimalFormat.format(tempMin));
            tvAvgTemp.setText(decimalFormat.format((tempMax + tempMin)/2));

            tvMaxWind.setText(decimalFormat.format(windMax));
            tvMinWind.setText(decimalFormat.format(windMin));
            tvAvgWind.setText(decimalFormat.format((windMax + windMin)/2));

            tick_Handler.postDelayed(tick_thread, 1000);
        }
    }

    private void beep() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
    }

    private double lastTempDiff;

    private List<DataPoint> getViewPortPoints(LineGraphSeries<DataPoint> series) {
        Iterator<DataPoint> pointIterator = series.getValues(Double.MIN_VALUE, Double.MAX_VALUE);
        List<DataPoint> dataPoints = new ArrayList<>();
        while (pointIterator.hasNext()) {
            dataPoints.add(pointIterator.next());
        }

        //reverse data to iterate from the end
        Collections.reverse(dataPoints);

        int length = graphLength / 1000;

        //trim list to current viewport if larger
        if (dataPoints.size() > length) {
            dataPoints = dataPoints.subList(0, length);
        }
        return dataPoints;
    }

    private double findDiff(List<DataPoint> dataPoints) {
        double highest = findMax(dataPoints);
        double lowest = findMin(dataPoints);

        //lowest value not found
        if (lowest == Double.MAX_VALUE) {
            lowest = 0;
        }

        return highest - lowest;
    }

    private double findMax(List<DataPoint> points) {
        double max = 0;
        for (DataPoint dataPoint: points) {
            if (dataPoint.getY() != 0 && dataPoint.getY() > max) {
                max = dataPoint.getY();
            }
        }
        return max;
    }

    private double findMin(List<DataPoint> points) {
        double min = Double.MAX_VALUE;
        for (DataPoint dataPoint: points) {
            if (dataPoint.getY() != 0 && dataPoint.getY() < min) {
                min = dataPoint.getY();
            }
        }
        return min;
    }

    private double findMax(double[] values) {
        double max = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != 0 && values[i] > max) {
                max = values[i];
            }
        }
        //LogUtil.logDebug(TAG, "MAX = " + max);
        return max;
    }

    private double findAvg(double[] array) {
        double sum = 0;
        int count = 0;
        String logMessage = "";
        for (int i = 0; i < array.length; i++) {
            logMessage += i + " = " + array[i] + ", ";
            if (array[i] != 0) {
                sum += array[i];
                count++;
            }
        }
        //LogUtil.logDebug(TAG, logMessage);
        double result = sum/count;
        return !Double.isNaN(result) ? result : 0;
    }

    public void startHandler(int delay) {
        LogUtil.logDebug(TAG, "startHandler()  " + delay);
        if (weatherService != null) {
            isPaused = false;
            invalidateOptionsMenu();
            tick_Handler.postDelayed(tick_thread, delay);
        } else {
            startService();
            invalidateOptionsMenu();
        }
    }

    public void stopHandler() {
        LogUtil.logDebug(TAG, "stopHandler()");
        isPaused = true;
        invalidateOptionsMenu();
        tick_Handler.removeCallbacks(tick_thread);
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.logDebug(TAG, "onServiceConnected()");
            weatherService = ((WeatherService.MyBinder) service).getService();

            //give the service 5 seconds to update currentData
            if (weatherService.isReading()) {
                startHandler(5000);
                Toast.makeText(HomeActivity.this, "Initiating sensors...", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.logDebug(TAG, "onServiceDisconnected()");
            weatherService = null;
        }
    }

    public static final String TAG = HomeActivity.class.getSimpleName();
}