package uk.ac.bbk.dcs.muc.logger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.widget.CheckBox;

public class LightLogger extends SensorLogger {

    public LightLogger(Context context, CheckBox checkBox, SensorManager sensorManager) {
        super("light", context, checkBox, sensorManager);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        this.initializeCheckBox();
    }

    /**
     * @param event
     */
    public void onSensorChanged(SensorEvent event) {
        sensorValue = String.valueOf(event.values[0]);
    }

    /**
     * Light Level
     *
     * References:
     * http://www.use-ip.co.uk/datasheets/lux_light_level_chart.pdf
     */
	public String getLightDescription(String value) {
        float light = Float.valueOf(value);

		String lightLevel = "";
		String lighthtml = "";
		String lightText = "";

		if (light >= 0.0 && light < 0.0001) {
			lightText = "Poor starlight";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/14.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 0.0001 && light < 0.001) {
			lightText = "Typical starlight";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/13.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 0.001 && light < 0.1) {
			lightText = "Moon light / Cloudy sky";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/12.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 0.1 && light < 0.3) {
			lightText = "Clear full moon";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/11.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 0.3 && light < 1) {
			lightText = "Twilight";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/10.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 1 && light < 2) {
			lightText = "Minimum Security risk lighting";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/9.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 2 && light < 5) {
			lightText = "Typical side road lighting";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/8.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 5 && light < 10) {
			lightText = "Sunset";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/7.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 10 && light < 15) {
			lightText = "Good main road lighting";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/6.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 15 && light < 50) {
			lightText = "Passage way / Outside working area";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/5.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 50 && light < 300) {
			lightText = "Minimum for easy reading";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/4.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 300 && light < 500) {
			lightText = "Well-lit Office";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/3.jpg\" width=\"50\" height=\"50\" /";
		}
		else if (light >= 500 && light < 5000) {
			lightText = "Overcast Sky";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/2.jpg\" width=\"50\" height=\"50\" />";
		}
		else if (light >= 5000) {
			lightText = "British summer sunshine";
			lighthtml = "<img src=\"http://www.sachith.0sites.net/muc/light/1.jpg\" width=\"50\" height=\"50\" />";
		}
		else {
			lightText = "Invalid Light Value";
		}

		return "<td>" + lighthtml  + "</td>" + "<td colspan=\"2\" >" + lightText + "</td>";
    }
}
