package uk.ac.bbk.dcs.muc.logger;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeLogger extends AbstractLogger {

	public TimeLogger(Context context) {
        super("time", context, null);
    }

    /**
     * @return Always true because time is available on all devices
     */
	protected Boolean isAvailable() {
        return true;
    }

    /**
     * @return Always true because time should be logged all the time
     */
	protected Boolean isChecked() {
        return true;
    }

    /**
     * Time does not need to enabled or disabled
     */
	protected void enableHandler(int interval) {}
	protected void disableHandler() {}

    /**
     * Get the current time
     * E.g. "4 April 2011 12:41:00, GMT"
     */
	protected void updateHandler() {
		String timeValue = new SimpleDateFormat("d MMM yyyy HH:mm:ss, z").format(new Date());
		loggedValues.add(timeValue);
	}
}