package uk.ac.bbk.dcs.muc.logger;

import android.content.Context;
import android.widget.CheckBox;
import java.util.ArrayList;

public abstract class AbstractLogger {

    protected String name = "";
    protected Context context = null;
    protected Boolean enabled = false;
    protected CheckBox checkBox = null;
    protected Boolean isChecked = false;
    protected ArrayList<String> loggedValues = new ArrayList<String>();

    public AbstractLogger(String name, Context context, CheckBox checkBox) {
        this.name = name;
        this.context = context;
        this.checkBox = checkBox;
    }

    /**
     * Returns the name of the logger
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Enables the logger by calling its enableHandler
     * A logger is only enabled if it is available on the phone and selected by the user
     *
     * @param interval
     * @return True if the logger was enabled
     */
    public Boolean enable(int interval) {
        if (!enabled) {
            loggedValues = new ArrayList<String>();
            if (isAvailable() && isChecked()) {
                enableHandler(interval);
                enabled = true;
            }
        }
        return enabled;
    }

    /**
     * The enableHandler, needs to be overridden by the subclass
     *
     * @param interval
     */
    protected abstract void enableHandler(int interval);

    /**
     * Enables the logger by calling its enableHandler
     */
    public void disable() {
        if (enabled) {
            disableHandler();
            enabled = false;
        }
    }

    /**
     * The disableHandler, needs to be overridden by the subclass
     */
    protected abstract void disableHandler();

    /**
     * Updates the logger if enabled, by calling its updateHandler
     */
    public void update() {
        if (enabled) {
            updateHandler();
        }
    }

    /**
     * The updateHandler, needs to be overridden by the subclass
     */
    protected abstract void updateHandler();

    /**
     * Returns if the logger is available on the device, needs to be overridden by the subclass
     */
    protected abstract Boolean isAvailable();

    /**
     * Sets the checkbox state according to the availability of the logger
     */
    protected void initializeCheckBox() {
        checkBox.setEnabled(isAvailable());
    }

    /**
     * @return True if the checkbox is checked
     */
    protected Boolean isChecked() {
        return checkBox.isChecked();
    }

    /**
     * Gets the value of the logger at the given index
     *
     * @param index
     * @return Value
     */
    public String getValue(int index) {
        if (index < loggedValues.size()) {
            return loggedValues.get(index);
        } else {
            return null;
        }
    }
}