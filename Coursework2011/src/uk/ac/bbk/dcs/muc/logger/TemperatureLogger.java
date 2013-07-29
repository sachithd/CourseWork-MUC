package uk.ac.bbk.dcs.muc.logger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.widget.CheckBox;

public class TemperatureLogger extends SensorLogger {

    public TemperatureLogger(Context context, CheckBox checkBox, SensorManager sensorManager) {
        super("temperature", context, checkBox, sensorManager);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        this.initializeCheckBox();
    }

    /**
     * The sensor value corresponds to a celsius temperature
     *
     * @param event
     */
    public void onSensorChanged(SensorEvent event) {
        sensorValue = String.valueOf(event.values[0]) + "°C";
    }
}
