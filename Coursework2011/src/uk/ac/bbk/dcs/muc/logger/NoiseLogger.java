package uk.ac.bbk.dcs.muc.logger;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.CheckBox;
import uk.ac.bbk.dcs.muc.LifeLoggingActivity;

import java.io.File;

public class NoiseLogger extends AbstractLogger {

    private Boolean available = false;
    private MediaRecorder noiseRecorder = null;
    private String fileName;
    final private int MAX_AMPLITUDE = 32768;
    
    public NoiseLogger(Context context, CheckBox checkBox) {
        super("noise", context, checkBox);

        fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecord.3gp";

        // check whether the device has a microphone or not
        // this would be better done by asking the PackageManager,
        // but this is not possible in 1.6
        try {
            noiseRecorder = new MediaRecorder();
            noiseRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            noiseRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            noiseRecorder.setOutputFile(fileName);
            noiseRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            noiseRecorder.prepare();
            noiseRecorder.start();
            noiseRecorder.stop();
            noiseRecorder.release();
            noiseRecorder = null;
            available = true;
        } catch (Exception e) {
        	available = false;
        }

        initializeCheckBox();
    }

    public Boolean isAvailable() {
        return available;
    }

    /**
     * Enable media recorder
     *
     * @param interval
     */
    protected void enableHandler(int interval) {

    	noiseRecorder = new MediaRecorder();
        noiseRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        noiseRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        noiseRecorder.setOutputFile(fileName);
        noiseRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    	
        try {
            noiseRecorder.prepare();
            noiseRecorder.start();
        } catch (Exception e) {
            Log.e(LifeLoggingActivity.LOG_TAG, "prepare() failed");
        }
        
    }

    /**
     * Disable media recorder
     */
    protected void disableHandler() {
        noiseRecorder.stop();
        noiseRecorder.reset();
        noiseRecorder.release();
        
        // Delete the temporary audio file
        File file = new File(fileName);
        boolean deleted = file.delete();

    }

    /**
     * Get the maximum amplitude of the recorder
     */
    protected void updateHandler() {
        this.loggedValues.add(String.valueOf(noiseRecorder.getMaxAmplitude()));
    }

    /**
     * Noise Level
     */
    public String getNoiseDescription(String value)
    {
        int noise = Integer.parseInt(value);
		String soundLevel = "";
		String soundText = "";

		if (noise >= 0 && noise <= (MAX_AMPLITUDE / 4)) {
			soundText = "Quiet";
			soundLevel = "<img src=\"http://www.sachith.0sites.net/muc/noise/Green.png\" width=\"50\" height=\"50\" />";
		}
		else if (noise > (MAX_AMPLITUDE / 4) && noise <= (MAX_AMPLITUDE / 2)) {
			soundText = "Normal";
			soundLevel = "<img src=\"http://www.sachith.0sites.net/muc/noise/Blue.png\" width=\"50\" height=\"50\" />";
		}
		else if (noise > (MAX_AMPLITUDE / 2) && noise <= ((MAX_AMPLITUDE / 4) * 3)) {
			soundText = "Loud";
			soundLevel = "<img src=\"http://www.sachith.0sites.net/muc/noise/Orange.png\" width=\"50\" height=\"50\" />";
		}
		else if (noise > ((MAX_AMPLITUDE / 4) * 3) && noise <= MAX_AMPLITUDE) {
			soundText = "Very Loud";
			soundLevel = "<img src=\"http://www.sachith.0sites.net/muc/noise/Red.png\" width=\"50\" height=\"50\" />";
		}
		else {
			soundText = "Unknown";
			soundLevel = "<img src=\"http://www.sachith.0sites.net/muc/noise/Black.png\" width=\"50\" height=\"50\" />";
		}

    	return "<td>" + soundLevel + "</td>" + "<td colspan=\"2\" >" + soundText + "</td>";
    }
}