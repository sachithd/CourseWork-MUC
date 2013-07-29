package uk.ac.bbk.dcs.muc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import uk.ac.bbk.dcs.muc.logger.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class LifeLoggingActivity extends Activity
{
    public static final String LOG_TAG = "MUC Life Logging";
    public static final String PICTURES_FOLDER = "muc/pictures";
    public static final String TOURS_FOLDER = "muc/tours";

    private Context context = null;
    private TextView stateBox = null;
    private EditText intervalTimeBox = null;
    private Button startButton = null;
    private Button stopButton = null;
    private Button clearButton = null;

    private AbstractLogger locationLogger = null;
    private ArrayList<AbstractLogger> loggers = new ArrayList<AbstractLogger>();
    private ArrayList<AbstractLogger> activeLoggers = new ArrayList<AbstractLogger>();
    private Boolean logging = false;
	private Handler intervalHandler = new Handler();
    private int intervalTime = 0;
    private int samples = 0;

    private String uploadURL = "http://www.sachith.0sites.net/upload.php"; 
    private String uploadedImagePath = "http://www.sachith.0sites.net/muc/";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context = this;

        // loggers
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // time
        AbstractLogger timeLogger = new TimeLogger(this);
        loggers.add(timeLogger);
        // location
        locationLogger = new LocationLogger(this);
        loggers.add(locationLogger);
        // noise
        AbstractLogger noiseLogger = new NoiseLogger(
                this,
                (CheckBox) findViewById(R.id.log_noise_checkbox));
        loggers.add(noiseLogger);
        // light
        AbstractLogger lightLogger = new LightLogger(
                this,
                (CheckBox) findViewById(R.id.log_light_checkbox),
                sensorManager);
        loggers.add(lightLogger);
        // acceleration
        AbstractLogger accelerationLogger = new AccelerationLogger(
                this,
                (CheckBox) findViewById(R.id.log_acceleration_checkbox),
                sensorManager);
        loggers.add(accelerationLogger);
        // temperature
        AbstractLogger temperatureLogger = new TemperatureLogger(
                this,
                (CheckBox) findViewById(R.id.log_temperature_checkbox),
                sensorManager);
        loggers.add(temperatureLogger);
        // picture
        AbstractLogger pictureLogger = new PictureLogger(
                this,
                (CheckBox) findViewById(R.id.log_picture_checkbox));
        loggers.add(pictureLogger);

        // UI
        stateBox = (TextView) findViewById(R.id.state_box);
        intervalTimeBox = (EditText) findViewById(R.id.interval_picker);
        startButton = (Button) findViewById(R.id.startbutton);
        startButton.setOnClickListener(startButtonListener);
        stopButton = (Button) findViewById(R.id.stopbutton);
        stopButton.setOnClickListener(stopButtonListener);
        stopButton.setEnabled(false);
        clearButton = (Button) findViewById(R.id.clearbutton);
        clearButton.setOnClickListener(clearButtonListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (logging) {
            stopLogging();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (logging) {
            startLogging();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (logging) {
            stopLogging();
        }
    }

    /**
     * Click handler for start button
     * Starts the logging of the selected loggers
     */
    private View.OnClickListener startButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            clearButton.setEnabled(false);
            stateBox.setText(R.string.searching_for_location);
            intervalTime = Integer.parseInt(intervalTimeBox.getText().toString()) * 1000;
            samples = 0;
            searchForFirstLocation();
        }
    };

    /**
     * Click handler for stop button
     * Ends the logging and writes the log files (uploads pictures etc.)
     */
    private View.OnClickListener stopButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            clearButton.setEnabled(true);
            stateBox.setText(R.string.logging_stopped);
            stopLogging();
            saveLog();
        }
    };

    /**
     * Click handler for clear button
     * Shows a confirmation dialog to ensure user really wants to clear the logs
     *
     * Code from:
     * http://stackoverflow.com/questions/2257963/android-how-to-show-dialog-to-confirm-user-wishes-to-exit-activity
     */
    private View.OnClickListener clearButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.clear)
                .setMessage(R.string.really_clear)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        File root = Environment.getExternalStorageDirectory();
                        ArrayList<File> directories = new ArrayList<File>();
                        directories.add(new File(root, TOURS_FOLDER));
                        directories.add(new File(root, PICTURES_FOLDER));
                        deleteDirectoryContents(directories);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
        }
    };

    /**
     * Deletes all files in a directory
     * Does not work for recursive directories, so only pass directories containing only files
     *
     * Code from:
     * http://stackoverflow.com/questions/4943629/android-how-to-delete-a-whole-folder-and-content
     *
     * @param directories
     */
    private void deleteDirectoryContents(ArrayList<File> directories) {
        for (File directory : directories) {
            if (directory.isDirectory()) {
                String[] children = directory.list();
                for (int i = 0; i < children.length; i++) {
                    new File(directory, children[i]).delete();
                }
            }
        }
    }

    /**
     * Called when a first location is recorded
     * This method is either called from this activity if a location is already present or it is called from the
     * location logger when the first location is recorded
     */
    public void firstLocationRecorded() {
        locationLogger.disable();
    	startLogging();
    }

    /**
     * Checks whether a location has been recorded already, and if not, searches for one
     */
    private void searchForFirstLocation() {
    	if (((LocationLogger) locationLogger).isFirstLocationRecorded()) {
    		firstLocationRecorded();
    	} else {
    		locationLogger.enable(1);
    	}
    }

    /**
     * Starts the logging
     * Enables the loggers and initiates the interval handler
     */
    private void startLogging() {
        logging = true;
        stateBox.setText(R.string.logging);
        enableLoggers();
        intervalHandler.removeCallbacks(intervalElapsed);
        intervalHandler.postDelayed(intervalElapsed, intervalTime);
    }

    /**
     * Stops the logging
     * Disables the loggers and removes the interval handler
     */
    private void stopLogging() {
        stateBox.setText(R.string.currently_not_logging);
        intervalHandler.removeCallbacks(intervalElapsed);
        disableLoggers();
        logging = false;
    }

    /**
     * Interval handler
     * Is called whenever intervalTime elapses
     * It calls the update methods of the active loggers
     */
    private Runnable intervalElapsed = new Runnable() {
		public void run() {
            samples++;
            for (AbstractLogger logger : activeLoggers) {
                logger.update();
            }
            intervalHandler.postDelayed(this, intervalTime);
        }
    };

    /**
     * Enables the loggers that the user selected
     */
    private void enableLoggers() {
        activeLoggers = new ArrayList<AbstractLogger>();
        for (AbstractLogger logger : loggers) {
            if (logger.enable(intervalTime)) {
                activeLoggers.add(logger);
            }
        }
    }

    /**
     * Disables the active loggers
     */
    private void disableLoggers() {
        for (AbstractLogger logger : activeLoggers) {
            logger.disable();
        }
    }

    /**
     * Saves the log
     * A KML tour file is created and saved in muc/tours
     * The pictures taken are uploaded to a server
     */
    @SuppressWarnings("unchecked")
	private void saveLog() {
    	// go through loggers and get data
        String placemarks = "";
        String tour = "";
        String path = "";
        ArrayList<String> picturesToUpload = new ArrayList<String>();

        for (int i = 0; i < samples; i++) {
        	String point = "";
        	String imgTag = "";

            placemarks += "<Placemark id=\"placemark" + i + "\">\n" +
                "<name>placemark" + i + "</name>\n" +
                "<description>\n" +
                    "<![CDATA[" +
                        "<table border=\"1\">";

            for (AbstractLogger logger : activeLoggers) {

                String value = logger.getValue(i);

                if (logger.getName() == "location") {

                    point = "<Point><coordinates>" + value + ",0</coordinates></Point>";
                	String[] locationArray = value.split(",");
                	tour += "<gx:FlyTo>\n"+
                        "<gx:duration>3</gx:duration>\n"+
                        "<gx:flyToMode>smooth</gx:flyToMode>\n"+
                        "<LookAt>\n"+
                            "<longitude>" + locationArray[0] + "</longitude>\n"+
                            "<latitude>" + locationArray[1] + "</latitude>\n"+
                            "<altitude>0</altitude>\n"+
                            "<heading>0</heading>\n"+
                            "<tilt>23.063392</tilt>\n"+
                            "<range>250</range>\n"+
                            "<altitudeMode>relativeToGround</altitudeMode>\n"+
                        "</LookAt>\n"+
                    "</gx:FlyTo>\n"+
                    "<gx:AnimatedUpdate>\n"+
                        "<Update>\n"+
                            "<targetHref/>\n"+
                            "<Change>\n"+
                                "<Placemark targetId=\"placemark" + i + "\">\n"+
                                    "<gx:balloonVisibility>1</gx:balloonVisibility>\n"+
                                "</Placemark>\n"+
                            "</Change>\n"+
                        "</Update>\n"+
                    "</gx:AnimatedUpdate>\n"+
                    "<gx:Wait>\n"+
                        "<gx:duration>4.0</gx:duration>\n"+
                    "</gx:Wait>\n";
                	path += locationArray[0] + "," + locationArray[1] + "0\n";
                	
                } else if (logger.getName() == "time" && value != null) {

                    placemarks += "<tr><td>Time</td><td colspan=\"3\">" + value + "</td></tr>";

                } else if (logger.getName() == "noise" && value != null) {

					placemarks += "<tr><td>Noise</td>" + ((NoiseLogger) logger).getNoiseDescription(value) + "</tr>";

                } else if (logger.getName() == "light" && value != null) {

					placemarks += "<tr><td>Light</td>" + ((LightLogger) logger).getLightDescription(value) + "</tr>";

                } else if (logger.getName() == "temperature" && value != null) {

					placemarks += "<tr><td>Temperature</td><td colspan=\"3\">" + value + " °C" + "</td></tr>";

                } else if (logger.getName() == "acceleration" && value != null) {

                    String[] AccelerationArray = value.split(",");
                    placemarks += "<tr>" +
                            "<td>Acceleration</td>" +
                            "<td> X Axis : " + AccelerationArray[0] + "</td>" +
                            "<td>Y Axis : " + AccelerationArray[1] + "</td>" +
                            "<td>Z Axis : " + AccelerationArray[2] + "</td>" +
                        "</tr>";

				} else if (logger.getName() == "picture" && value != null) {

                    picturesToUpload.add(value);
                    placemarks += "<tr><td colspan=\"4\"><img src=\"" + uploadedImagePath + value + "\" /></td></tr>";

                }
            }

            placemarks += "</table>" +
                    "]]>\n" +
                "</description>\n" +
                "<styleUrl>route</styleUrl>\n" +
                point + "\n" +
            "</Placemark>\n";
        }

        // create a new kml file on the SD card
        String filename = "mucLog_" + new SimpleDateFormat("yy-MM-dd-HH-mm-ss").format(new Date()) + ".kml";
        File root = Environment.getExternalStorageDirectory();
		File toursDirectory = new File(root, "muc/tours");
		toursDirectory.mkdirs();
		File xmlFile = new File(toursDirectory, filename);
        try {
            xmlFile.createNewFile();
            FileWriter xmlFileWriter = new FileWriter(xmlFile);
            BufferedWriter out = new BufferedWriter(xmlFileWriter);

            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +
                "<Document>\n" +
                    "<name>MUC Log</name>\n" +
                    "<open>1</open>\n" +
                    "<gx:Tour>\n" +
                        "<name>Play me</name>\n" +
                        "<Style id=\"route\">\n" +
                            "<IconStyle>\n" +
                                "<Icon>\n" +
                                    "<href>http://maps.google.com/mapfiles/ms/micons/man.png</href>\n" +
                                    "<y>128</y>\n" +
                                    "<w>32</w>\n" +
                                    "<h>32</h>\n" +
                                "</Icon>\n" +
                            "</IconStyle>\n" +
                            "<LabelStyle>\n" +
                                "<color>ffffffff</color>\n" +
                                "<scale>1</scale>\n" +
                            "</LabelStyle>\n" +
                        "</Style>\n" +
                        "<gx:Playlist>" +
                            tour +
                        "</gx:Playlist>\n" +
                    "</gx:Tour>" +
                    placemarks +
                    "<Placemark>\n" +
                        "<name>Path </name>\n" +
                        "<Style>\n" +
                            "<BalloonStyle>\n" +
     	    	                "<bgColor>ffffffbb</bgColor>\n" +
     	    	            "</BalloonStyle>\n" +
                            "<IconStyle>\n" +
                                "<Icon>\n" +
                                    "<href>root://icons/palette-2.png</href>\n" +
                                "</Icon>\n" +
                            "</IconStyle>\n" +
                            "<LineStyle>\n" +
                                "<color>ff0000ff</color>\n" +
                                "<width>5.0</width>\n" +
                            "</LineStyle>\n" +
                        "</Style>\n" +
                        "<MultiGeometry>\n" +
                            "<LineString>\n" +
                                "<altitudeMode>relativeToGround</altitudeMode>\n" +
                                "<coordinates>\n" +
                                    path +
                                "</coordinates>\n" +
                            "</LineString>\n" +
                        "</MultiGeometry>\n" +
                    "</Placemark>\n" +
                "</Document>\n" +
            "</kml>");
            out.close();
        } catch(Exception e){
            Log.d(LOG_TAG, e.getMessage());
        }

        // upload pictures
        if (picturesToUpload.size() > 0) {                   
            try {
            	new ImageUploadTask().execute(picturesToUpload);
               
            } catch(Exception e) {
                Log.d(LOG_TAG, e.getMessage());
                stateBox.setText(R.string.uploading_failed);
            }
        }
    }
    
    /*
     * 
     * Background process to upload image files to the server
     * 
     */
    
    class ImageUploadTask extends AsyncTask <ArrayList<String>, String, String>{
        
    	//private ProgressDialog Dialog = new ProgressDialog(LifeLoggingActivity.this);
        
        protected void onPreExecute() {
        	stateBox.setText(R.string.uploading_pictures);
            //Dialog.setMessage("Uploading Images..");
            //Dialog.show();
        }

		
    	@Override	
		protected String doInBackground(ArrayList<String>... passing) {
			File root = Environment.getExternalStorageDirectory();
			ArrayList<String> passed = passing[0];
			
			
			for (String pictureFilename : passed) {
				publishProgress(pictureFilename);
				String filename = PICTURES_FOLDER + "/" + pictureFilename;
				try{
				File uploadFile = new File(root, filename);     
		        FileInputStream fileInputStream = new FileInputStream(uploadFile);
		        String lineEnd = "\r\n";
		        String twoHyphens = "--";
		        String boundary = "*****";
		        URL url = new URL(uploadURL);
		        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		        httpURLConnection.setDoInput(true);
		        httpURLConnection.setDoOutput(true);
		        httpURLConnection.setUseCaches(false);
		        httpURLConnection.setRequestMethod("POST");
		        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
		        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		        DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
		        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
		        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + filename + "\"" + lineEnd);
		        dataOutputStream.writeBytes(lineEnd);
		        int bytesAvailable = fileInputStream.available();
		        int maxBufferSize = 1024;
		        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
		        byte[] buffer = new byte[bufferSize];
		        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		        while (bytesRead > 0) {
		            dataOutputStream.write(buffer, 0, bufferSize);
		            bytesAvailable = fileInputStream.available();
		            bufferSize = Math.min(bytesAvailable, maxBufferSize);
		            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		        }
		        dataOutputStream.writeBytes(lineEnd);
		        dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
		        fileInputStream.close();
		        dataOutputStream.flush();
		        InputStream inputStream = httpURLConnection.getInputStream();
		        int ch;
		        StringBuffer stringBuffer = new StringBuffer();
		        while ((ch = inputStream.read()) != -1) {
		            stringBuffer.append((char) ch);
		        }
		        String s = stringBuffer.toString();
		        Log.d(LOG_TAG + " HERE", s);
		        dataOutputStream.close();
		       
				
				}
				catch (Exception e)
				{
					return "false";
				}
			 
			}
			
			return "true";

		}//End of doInBackground
    	
    	
    	//Shows the upload progress
    	@Override
        protected void onProgressUpdate(String... filename) { 
    		//Dialog.setMessage("Uploading " + filename[0]);
    		stateBox.setText("Uploading " + filename[0]);
        }

        @Override
	     protected void onPostExecute(String result) {
        	//Dialog.dismiss(); 
        	if(result.equals("true"))
        	{	
        		stateBox.setText(R.string.uploading_done);
        	}
        	else
        	{
        		stateBox.setText(R.string.uploading_failed);
        	}
	     }


    }
}