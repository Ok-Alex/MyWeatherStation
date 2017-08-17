package me.akulakovsky.okcentre.v2.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.text.DecimalFormat;

import me.akulakovsky.okcentre.v2.HomeActivity;
import me.akulakovsky.okcentre.v2.R;
import me.akulakovsky.okcentre.v2.adapters.MySpinnerAdapter;
import me.akulakovsky.okcentre.v2.utils.SettingsUtils;

public class PrefsFragment extends DialogFragment implements DialogInterface.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private SeekBar emaTempAlphaBar;
    private SeekBar emaWindAlphaBar;
    private SeekBar graphLengthBar;
    private TextView emaTempValue;
    private TextView emaWindValue;
    private TextView graphLengthValue;
    private CheckBox chkBlack;
    private CheckBox chkRed;
    private CheckBox chkGreen;
    private CheckBox chkBlue;

    private CheckBox chkSmoothBlack;
    private CheckBox chkSmoothRed;
    private CheckBox chkSmoothGreen;
    private CheckBox chkSmoothBlue;

    private Spinner nearSensorSpinner;
    private SettingsUtils settingsUtils;
    private RadioButton blackThemeRadio;
    private RadioButton whiteThemeRadio;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        settingsUtils = SettingsUtils.getInstance(getActivity());

        View v =  getActivity().getLayoutInflater().inflate(R.layout.prefs_fragment, null);
        initViews(v);
        initSeekBars();
        initCheckBoxes();

        return new AlertDialog.Builder(getActivity())
                .setTitle("Settings")
                .setView(v)
                .setPositiveButton("OK", this)
                .setNegativeButton("Cancel", null)
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ((HomeActivity) getActivity()).currentTick = -1;
        if (((HomeActivity) getActivity()).weatherService != null) {
            ((HomeActivity) getActivity()).startHandler(0);
        }
        super.onDismiss(dialog);
    }

    private void initCheckBoxes() {
        SettingsUtils settingsUtils = SettingsUtils.getInstance(getActivity());
        chkBlack.setChecked(settingsUtils.getValue(SettingsUtils.PREF_BLACK_SENSOR, false));
        chkRed.setChecked(settingsUtils.getValue(SettingsUtils.PREF_RED_SENSOR, false));
        chkBlue.setChecked(settingsUtils.getValue(SettingsUtils.PREF_BLUE_SENSOR, false));
        chkGreen.setChecked(settingsUtils.getValue(SettingsUtils.PREF_GREEN_SENSOR, false));

        chkSmoothBlack.setChecked(settingsUtils.getValue(SettingsUtils.PREF_EMA_BLACK_SENSOR, true));
        chkSmoothRed.setChecked(settingsUtils.getValue(SettingsUtils.PREF_EMA_RED_SENSOR, true));
        chkSmoothGreen.setChecked(settingsUtils.getValue(SettingsUtils.PREF_EMA_GREEN_SENSOR, true));
        chkSmoothBlue.setChecked(settingsUtils.getValue(SettingsUtils.PREF_EMA_BLUE_SENSOR, true));
    }

    private void initViews(View v) {
        emaTempValue = (TextView) v.findViewById(R.id.ema_value);
        emaWindValue = (TextView) v.findViewById(R.id.ema_wind_value);
        graphLengthValue = (TextView) v.findViewById(R.id.graph_length_value);

        emaTempAlphaBar = (SeekBar) v.findViewById(R.id.ema_temp);
        emaWindAlphaBar = (SeekBar) v.findViewById(R.id.ema_wind);
        graphLengthBar = (SeekBar) v.findViewById(R.id.graph_length);

        chkBlack = (CheckBox) v.findViewById(R.id.black_sensor);
        chkRed = (CheckBox) v.findViewById(R.id.red_sensor);
        chkGreen = (CheckBox) v.findViewById(R.id.green_sensor);
        chkBlue = (CheckBox) v.findViewById(R.id.blue_sensor);

        chkSmoothBlack = v.findViewById(R.id.smooth_black);
        chkSmoothRed = v.findViewById(R.id.smooth_red);
        chkSmoothGreen = v.findViewById(R.id.smooth_green);
        chkSmoothBlue = v.findViewById(R.id.smooth_blue);

        nearSensorSpinner = (Spinner) v.findViewById(R.id.spinner);
        MySpinnerAdapter adapter = new MySpinnerAdapter(getActivity(), android.R.layout.simple_list_item_1);
        String[] sensors = new String[]{"Black", "Red", "Blue", "Green"};
        adapter.addAll(sensors);
        adapter.add("Select sensor near you");
        nearSensorSpinner.setAdapter(adapter);

        int nearSensor = settingsUtils.getValue(SettingsUtils.PREF_NEAR_SENSOR, -1);
        if (nearSensor == -1) {
            nearSensorSpinner.setSelection(adapter.getCount());
        } else {
            nearSensorSpinner.setSelection(nearSensor);
        }

        blackThemeRadio = (RadioButton) v.findViewById(R.id.black_theme);
        whiteThemeRadio = (RadioButton) v.findViewById(R.id.white_theme);

        if (settingsUtils.getValue(SettingsUtils.PREF_THEME, 0) == 1) {
            whiteThemeRadio.setChecked(true);
        }

    }

    private void initSeekBars() {
        emaTempAlphaBar.setMax(10);
        double alphaTemp = SettingsUtils.getInstance(getActivity()).getValue(SettingsUtils.PREF_EMA_TEMP_ALPHA, 0.1f);
        emaTempAlphaBar.setProgress((int) (alphaTemp * 10));
        emaTempValue.setText(decimalFormat.format(alphaTemp) + " alpha");
        emaTempAlphaBar.setOnSeekBarChangeListener(this);

        double alphaWind = SettingsUtils.getInstance(getActivity()).getValue(SettingsUtils.PREF_EMA_WIND_ALPHA, 0.01f);
        emaWindAlphaBar.setMax(100);
        emaWindAlphaBar.setProgress((int) (alphaWind * 100));
        emaWindAlphaBar.setOnSeekBarChangeListener(this);
        emaWindValue.setText(decimalFormat.format(alphaWind) + " alpha");

        graphLengthBar.setMax(20);
        graphLengthBar.setProgress(SettingsUtils.getInstance(getActivity()).getValue(SettingsUtils.PREF_GRAPH_LENGTH, 12));
        graphLengthValue.setText(graphLengthBar.getProgress() + " minutes");
        graphLengthBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                save();
                break;
        }
    }

    private void save() {
        settingsUtils.putValue(SettingsUtils.PREF_GRAPH_LENGTH, graphLengthBar.getProgress());
        float alphaTemp = ((float) emaTempAlphaBar.getProgress()) / 10;
        settingsUtils.putValue(SettingsUtils.PREF_EMA_TEMP_ALPHA, alphaTemp);
        float alphaWind = ((float) emaWindAlphaBar.getProgress()) / 100;
        settingsUtils.putValue(SettingsUtils.PREF_EMA_WIND_ALPHA, alphaWind);
        settingsUtils.putValue(SettingsUtils.PREF_BLACK_SENSOR, chkBlack.isChecked());
        settingsUtils.putValue(SettingsUtils.PREF_BLUE_SENSOR, chkBlue.isChecked());
        settingsUtils.putValue(SettingsUtils.PREF_GREEN_SENSOR, chkGreen.isChecked());
        settingsUtils.putValue(SettingsUtils.PREF_RED_SENSOR, chkRed.isChecked());

        settingsUtils.putValue(SettingsUtils.PREF_EMA_BLACK_SENSOR, chkSmoothBlack.isChecked());
        settingsUtils.putValue(SettingsUtils.PREF_EMA_RED_SENSOR, chkSmoothRed.isChecked());
        settingsUtils.putValue(SettingsUtils.PREF_EMA_GREEN_SENSOR, chkSmoothGreen.isChecked());
        settingsUtils.putValue(SettingsUtils.PREF_EMA_BLUE_SENSOR, chkSmoothBlue.isChecked());

        settingsUtils.putValue(SettingsUtils.PREF_NEAR_SENSOR, nearSensorSpinner.getSelectedItemPosition());
        settingsUtils.putValue(SettingsUtils.PREF_THEME, blackThemeRadio.isChecked() ? 0 : 1);
        ((HomeActivity) getActivity()).updateSettings();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress == 0) {
            progress = 1;
            seekBar.setProgress(progress);
        }

        switch (seekBar.getId()) {
            case R.id.graph_length:
                graphLengthValue.setText(progress + " minutes");
                break;
            case R.id.ema_temp:
                float valueTemp = ((float) progress) / 10;
                emaTempValue.setText(decimalFormat.format(valueTemp) + " alpha");
                break;
            case R.id.ema_wind:
                float valueWind = ((float) progress) / 100;
                emaWindValue.setText(decimalFormat.format(valueWind) + " alpha");
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
