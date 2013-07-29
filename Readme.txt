=====================================================
                 MUC Lifelogging
=====================================================


# Contributors

	Michael Sauter

	M G S Dassanayake


# INTRODUCTION

MUC Lifelogging enables your android phone to capture human experiences by using its sensors such as Acceleration, Light, Noise, Temperature,  & Camera
It uses the phone GPS receiver to record the location and create a kml file with the above sensors data to play it on Google Earth.   


# Requirements

MUC Lifelogging requires an Android phone with the following

Android 1.6 or higher
	MUC Lifelogging is built on Standard Android Platform 1.6 but it will run on the higher platforms

A GPS Receiver
	This application requires a phone with a GPS receiver to record the user locations.

SD Card
	SD Card is required to create the KML files and the image files


Below sensors are optional but enhance the human experience of a given moment

*Audio Recorder - To record the noise level

*Acceleration Sensor - To record the acceleration of the phone along the x,y & z axes 

*Ambient Light Sensor - To record the light level

*Temperature Sensor - To record the temperature

*Camera - Take pictures

*WiFi or Mobile data connection - To upload pictures to the server



# How to use MUC Lifeloggin

The application on the andorid device disables all the unsupported sensors and enables the users to select the sensors they want to record.

Users have the option to change the time interval to log the sensor readings.

Press start button to start logging.

The application will not start logging until it receives the first GPS location. As soon as it receives the GPS location it will start recording and if the GPS signal is lost, it will use the last known location until user stops logging.

A KML file will be created on the SD card under MUC/Tours with the current date and time stamp  (Eg. mucLog_11-04-12-17-59-53)  

Pictures will be uploaded to a server if there is an internet connection available on the phone.
A Clear button is provided to remove the KML Logs and the image files on the SD Card

Download the KML tour file and view it on Google earth
 

