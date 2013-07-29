package uk.ac.bbk.dcs.muc.logger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.CheckBox;

public abstract class SensorLogger extends AbstractLogger implements SensorEventListener {

    protected SensorManager sensorManager = null;
    protected Sensor sensor = null;
    protected String sensorValue = null;

    public SensorLogger(String name, Context context, CheckBox checkBox, SensorManager sensorManager) {
        super(name, context, checkBox);
        this.sensorManager = sensorManager;
    }

    /**
     * If the sensor was not found, this logger is not available
     *
     * @return true if sensor is available
     */
    protected Boolean isAvailable() {
        return sensor != null;
    }

    /**
     * Adds the current sensorValue to loggedValues
     */
    protected void updateHandler() {
        loggedValues.add(sensorValue);
    }

    /**
     * Registers a listener on the sensor
     *
     * @param interval
     */
    protected void enableHandler(int interval) {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Removes the listener on the sensor
     */
    protected void disableHandler() {
        sensorManager.unregisterListener(this, sensor);
    }

    /**
     * Callback when the sensor value changed
     *
     * @param event
     */
    public abstract void onSensorChanged(SensorEvent event);

    /**
     * Callback when the accuracy of the sensor changed
     * Currently this case is not handled
     *
     * @param sensor
     * @param accuracy
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
