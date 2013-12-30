package com.xortech.sender;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.xortech.database.MsgDatabaseHandler;
import com.xortech.database.MessageData;
import com.xortech.database.MyTags;
import com.xortech.database.TagDatabaseHandler;
import com.xortech.map.GPSTracker;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {	
	
	private static final String SMS_EXTRA_NAME = "pdus";   
    private static final String SECRET_LOCATION_A = "*1*";
    private static final String SECRET_LOCATION_B = "*2*";
    private static final String SECRET_LOCATION_C = "*3*";  
    private static final String SECRET_LOCATION_D = "*4*";
    private static final String GOOGLE_STRING = "http://maps.google.com/maps?q=";
    private static final String DEFAULT_NUMBER = "+18775550000";
    private static final String DEFAULT_CODE = "1234";
    private static final String DEFAULT_TAGID = "Sender-01";
    private static final String PANIC_TXT = "Panic Detected!";
    private static final String UNDER_DURRESS = " is under durress!";
    private static final String BEGIN_PATH = "android.resource://";
    private static final String FILE_PATH = "/raw/panic";
    private static final Double FIVE_DIGIT = 100000.0D;
    private static final long FIVE_SECONDS = 5000L;
    private static final String NO_COORDS = "Unable to retrieve coordinates from: ";
	
    SenderReceive deviceMap;
    SharedPreferences preferences;
	NotificationManager notificationManager;
	Timer mytimer;	
	Uri soundUri;
	TimerTask mytask;
	Context context;
		
    String returnNumber = null;
    String secretCode = null;
    String tagID = null;
    String googleString = null;
    String tag_ID = null;
    String tag_lat = null;
    String tag_long = null;
    String latitude = null;
    String longitude = null;
    String location = null;
	String emergencyTag = null;
	        
	public void onReceive( final Context ctx, Intent intent ) 
	{   
		// Get SMS map from Intent
	    Bundle extras = intent.getExtras();
	    context = ctx;
	    
	    // GPS Instance
        GPSTracker gps = new GPSTracker(context);
	    
        if ( extras != null )
        {
        	// Load preferences into variables
        	preferences = PreferenceManager.getDefaultSharedPreferences(context);
        	returnNumber = preferences.getString("returnNumber", DEFAULT_NUMBER);
        	secretCode = preferences.getString("secretCode", DEFAULT_CODE);
        	tagID = preferences.getString("tagID", DEFAULT_TAGID);
            
        	// Get received SMS array
            Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);
            
            for ( int i = 0; i < smsExtra.length; ++i )
            {
            	// Get message
            	SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
            	
            	// Parse message data
            	String body = sms.getMessageBody().toString();
            	String address = sms.getOriginatingAddress();
            	long time = System.currentTimeMillis();
            	
        	    // Get coordinates and create message           	           	       	
    			latitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLatitude()) / FIVE_DIGIT);
    			longitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLongitude()) / FIVE_DIGIT);
    			location = "Tag_ID:" + tagID + ":Location:" + latitude + "," + longitude;
    			googleString = GOOGLE_STRING + latitude + "," + longitude + "(" + tagID + ")"; 			
    			
    			if (body.equals(SECRET_LOCATION_A + secretCode)) { 
    				if (latitude.equals("0.0")) {
    					SmsManager.getDefault().sendTextMessage(address, null, NO_COORDS + tagID, null, null);
    				}
    				else {
    					SmsManager.getDefault().sendTextMessage(address, null, googleString, null, null);
    				}
                    this.abortBroadcast(); 
    			}
    			else if (body.equals(SECRET_LOCATION_B + secretCode)) {
    				if (latitude.equals("0.0")) {
    					SmsManager.getDefault().sendTextMessage(address, null, NO_COORDS + tagID, null, null);
    				}
    				else {
    					SmsManager.getDefault().sendTextMessage(address, null, googleString, null, null);
    				}
                    this.abortBroadcast(); 
            	}
    			else if (body.equals(SECRET_LOCATION_C + secretCode)) {
    				if (latitude.equals("0.0")) {
    					SmsManager.getDefault().sendTextMessage(address, null, NO_COORDS + tagID, null, null);
    				}
    				else {
    					SmsManager.getDefault().sendTextMessage(address, null, location, null, null);
    				}
    				this.abortBroadcast();
    			}
    			else if (body.equals(SECRET_LOCATION_D + secretCode)) {
    				gps.ResetGPS();
    				gps.getLocation();
    				latitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLatitude()) / FIVE_DIGIT);
        			longitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLongitude()) / FIVE_DIGIT);
        			
    				if (latitude.equals("0.0") | longitude.equals("0.0")) {
    					SmsManager.getDefault().sendTextMessage(address, null, NO_COORDS + tagID, null, null);
    				}
    				else {
    					SmsManager.getDefault().sendTextMessage(address, null, location, null, null);
    				}
    				this.abortBroadcast();
    			}
    			else if (body.contains("Tag_ID:")) {
    				// Add to DB
    				MsgDatabaseHandler dbHandler = new MsgDatabaseHandler(context);
    				
    				// Verify if the tag exist in the array
    				String addressExists = VerifyTagExist(address);
    				
    				String[] splitBody = body.split(":");
    				String tag = splitBody[1];
    				tag.trim();
    				String coords = splitBody[3];
    				String[] splitCoords = coords.split(",");
    				String lat = splitCoords[0];
    				lat.trim();
    				String lon = splitCoords[1];
    				lon.trim();
    				String _time = String.valueOf(time);    			
    				String toastMsg = null;
    				
    				// Check if address exist for naming purposes
    				if (addressExists == null) {    					   					
    					dbHandler.Add_Message(new MessageData(tag, address, lat, lon, _time));
    					toastMsg = "Response Received: " + tag;
    				}
    				else {
    					dbHandler.Add_Message(new MessageData(addressExists, address, lat, lon, _time));
    					toastMsg = "Response Received: " + addressExists;
    				}
    				
    				Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
    				this.abortBroadcast();
    			}
    			else if (body.contains("Panic!")) {
    				
    				// Override silent 
    				AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    				int max = audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
    				audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    				audio.setStreamVolume(AudioManager.STREAM_RING, max, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    				
    				//Define Notification Manager
    				notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    				
    				// Start timer
        			mytimer = new Timer(true);
        			
        			// Sound location
    				soundUri = Uri.parse(BEGIN_PATH + context.getPackageName() + FILE_PATH);
    				
    				// Display tagID for emergency
    				String[] splitBody = body.split("\n");
    				String fieldTag = splitBody[1];
    				String[] splitTag = fieldTag.split(":");
    				
    				emergencyTag = splitTag[1].trim();
    				
    				// Timer for notifications
                    mytask = new TimerTask() {
                        public void run() {
            				// Run notification on timer
            				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
            				        .setSmallIcon(R.drawable.emergency)
            				        .setContentTitle(PANIC_TXT)
            				        .setContentText(emergencyTag + UNDER_DURRESS)
            				        .setSound(soundUri); //This sets the sound to play

            				//Display notification
            				notificationManager.notify(0, mBuilder.build());
                        }
                    };                    
                    // Start timer after 5 seconds and send new SMS every 60 seconds
                    mytimer.schedule(mytask, FIVE_SECONDS);
    			}
            }            
        } 
	    // Clear cache on receive
        try {
            SenderMain.trimCache(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * Function to check if the tag exist in the array for toast display
	 * */
	public String VerifyTagExist(String address) {
		String found = null;
		
		// Get data from DB and check if address exists
    	TagDatabaseHandler mytdb = new TagDatabaseHandler(context);
    	ArrayList<MyTags> mytag_array_from_db = mytdb.Get_Tags();

		for (int ix = 0; ix < mytag_array_from_db.size(); ix++) {		
			String mobile = null;
			String tag = null;
			
			mobile = mytag_array_from_db.get(ix).getMyTagPhoneNumber();
			tag = mytag_array_from_db.get(ix).getMyTag();
			
			// If exists, use the name from our tag DB, not the sender name
			if (address.equals(mobile)) {
				found = tag;
			}
			else if (address.equals("+1" + mobile)) {
				found = tag;
			}
		}
		mytdb.close();
		return found;
	}	
}