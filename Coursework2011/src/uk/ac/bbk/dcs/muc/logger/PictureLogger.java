package uk.ac.bbk.dcs.muc.logger;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.widget.CheckBox;
import uk.ac.bbk.dcs.muc.LifeLoggingActivity;

import java.io.File;
import java.io.FileOutputStream;

public class PictureLogger extends AbstractLogger {

    private Boolean available = false;
    private Camera camera = null;
    //private Size mSize;

    public PictureLogger(Context context, CheckBox checkBox) {
        super("picture", context, checkBox);
        // check whether the device has a camera or not
        // this would be better done by asking the PackageManager,
        // but this is not possible in 1.6
        Camera testCamera = Camera.open();
        if (testCamera != null) {
            available = true;
            testCamera.release();
            testCamera = null;
        }
        this.initializeCheckBox();
    }

    /**
     * @return True if a camera is available
     */
    protected Boolean isAvailable() {
        return available;
    }

    /**
     * Enable the camera
     *
     * @param interval
     */
    protected void enableHandler(int interval) {
    	try {
            camera = Camera.open();
            Camera.Parameters params = camera.getParameters();
            params.set("rotation", "portrait");
            //mSize = params.getPictureSize();
            params.setPictureSize(640, 480); 
            camera.setParameters(params);
            camera.startPreview();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    /**
     * Disable the camera
     */
    protected void disableHandler() {
    	if (camera != null){
    		camera.stopPreview();
    	    camera.release();
            camera = null;
    	}
    }

    /**
     * Take a picture on each interval
     */
    protected void updateHandler() {
    	try {
    		camera.takePicture(null, null, jpegPictureCallback);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    /**
     * Callback of JPG file
     * Writes the file to the SD card in the pictures folder
     */
    Camera.PictureCallback jpegPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				String filename = String.format("%d.jpg", System.currentTimeMillis());
				File root = Environment.getExternalStorageDirectory();
				File pictureDirectory = new File(root, LifeLoggingActivity.PICTURES_FOLDER);
				pictureDirectory.mkdirs();
				File pictureFile = new File(pictureDirectory, filename);
				FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
				fileOutputStream.write(data);
				fileOutputStream.close();
				camera.startPreview();
                loggedValues.add(filename);
			} catch (Exception e) {
				e.printStackTrace();
            }
		}
	};
}
