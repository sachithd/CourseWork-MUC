package uk.ac.bbk.dcs.muc.logger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.widget.CheckBox;

public class AccelerationLogger extends SensorLogger {

    /*
    private float x;
    private float y;
    private float z;
    private float gravityX;
    private float gravityY;
    private float gravityZ;
    */

    public AccelerationLogger(Context context, CheckBox checkBox, SensorManager sensorManager) {
        super("acceleration", context, checkBox, sensorManager);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.initializeCheckBox();
    }

    /**
     * When the acceleration changes, update the sensor value to X,Y,Z acceleration
     * TODO How should this value be handled?
     *
     * @param event
     */
    public void onSensorChanged(SensorEvent event) {
        sensorValue = event.values[0] + "," + event.values[1] + "," + event.values[2];

        /*
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        final float alpha = 0.8;

        gravityX = alpha * gravityX + (1 - alpha) * event.values[0];
        gravityY = alpha * gravityY + (1 - alpha) * event.values[1];
        gravityZ = alpha * gravityZ + (1 - alpha) * event.values[2];

        x = event.values[0] - gravityX;
        y = event.values[1] - gravityY;
        z = event.values[2] - gravityZ;
        */
    }
}
