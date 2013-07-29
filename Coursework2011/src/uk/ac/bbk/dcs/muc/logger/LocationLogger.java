package uk.ac.bbk.dcs.muc.logger;

import uk.ac.bbk.dcs.muc.LifeLoggingActivity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationLogger extends AbstractLogger implements LocationListener {

    private LocationManager locationManager = null;
    private String locationValue = null;
    private Boolean firstLocationRecorded = false;

    public LocationLogger(Context context) {
        super("location", context, null);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * @return True if a location has been recorded already
     */
    public Boolean isFirstLocationRecorded() {
    	return firstLocationRecorded;
    }

    /**
     * @return True if location is available
     */
    protected Boolean isAvailable() {
        return locationManager != null;
    }

    /**
     * @return Always true, because the location nees to be recorded all the time
     */
    protected Boolean isChecked() {
        return true; // always log the location
    }

    /**
     * Requests location updates from the GPS provider
     *
     * @param interval
     */
    protected void enableHandler(int interval) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval / 2, 5, this);
    }

    /**
     * Stops location updates
     */
    protected void disableHandler() {
        locationManager.removeUpdates(this);
    }

    /**
     * Adds the current location to the logged values
     */
    protected void updateHandler() {
        loggedValues.add(locationValue);
    }

    /**
     * When the location has changed, the new value is recorded
     * If that value is the first location recorded, we need to notify LifeLoggingActivity
     *
     * @param location
     */
    public void onLocationChanged(Location location) {
        locationValue = location.getLongitude() + "," + location.getLatitude();
        if (!isFirstLocationRecorded()) {
        	firstLocationRecorded = true;
    		((LifeLoggingActivity) context).firstLocationRecorded();
    	}
	}

    /**
     * Callbacks for location that are not taken into account
     */
    public void onProviderDisabled(String provider) {}
    public void onProviderEnabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}