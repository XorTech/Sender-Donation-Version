/*
 * Copyright 2014 XOR TECH LTD 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.xortech.sender;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.xortech.database.MsgDatabaseHandler;
import com.xortech.database.MessageData;
import com.xortech.database.MyTags;
import com.xortech.database.TagDatabaseHandler;
import com.xortech.map.GPSTracker;
import com.xortech.sender.R;

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
    private static final String SECRET_LOCATION_B = "*3*";  
    private static final String GOOGLE_STRING = "http://maps.google.com/maps?q=";
    private static final String DEFAULT_CODE = "1234";
    private static final String DEFAULT_TAGID = "Sender-01";
    private static final String PANIC_TXT = "Panic Detected!";
    private static final String UNDER_DURRESS = " is under durress!";
    private static final String BEGIN_PATH = "android.resource://";
    private static final String FILE_PATH = "/raw/panic";
    private static final Double FIVE_DIGIT = 100000.0D;
    private static final long FIVE_SECONDS = 5000L;
    private static final String NO_COORDS = "Unable to retrieve coordinates from: ";
	
    private SharedPreferences preferences;
    private NotificationManager notificationManager;
    private Timer mytimer;	
    private Uri soundUri;
    private TimerTask mytask;
    private Context context;
    private GPSTracker gps;
		
    private String secretCode = null;
    private String tagID = null;
    private String googleString = null;
    private String latitude = null;
    private String longitude = null;
    private String location = null;
    private String emergencyTag = null;
    private boolean senderEnabled = true;
	        
	public void onReceive(final Context ctx, Intent intent) {   
		// GET SMS MAP FROM INTENT
	    Bundle extras = intent.getExtras();
	    context = ctx;
	    
	    // GPS INSTANCE
        gps = new GPSTracker(context);
        
    	// LOAD PREFERENCES
    	preferences = PreferenceManager.getDefaultSharedPreferences(context);
    	secretCode = preferences.getString("secretCode", DEFAULT_CODE);
    	tagID = preferences.getString("tagID", DEFAULT_TAGID);
    	senderEnabled = preferences.getBoolean("senderEnabled", true);
	    
        if (extras != null) {
        	     	           
        	// GET THE RECEIVED SMS ARRAY
            Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);
            
            for (int i = 0; i < smsExtra.length; ++i) {
            	
            	// GET THE MESSAGE
            	SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
            	
            	// PARSE THE MESSAGE BODY
            	String body = sms.getMessageBody().toString();
            	String address = sms.getOriginatingAddress();
            	long time = System.currentTimeMillis();
            	
        	    // GET COORDINATES AND SEND A MESSAGE
            	gps.getLocation();
            	
    			latitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLatitude()) / FIVE_DIGIT);
    			longitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLongitude()) / FIVE_DIGIT);
    			location = "Tag_ID:" + tagID + ":Location:" + latitude + "," + longitude;
    			googleString = GOOGLE_STRING + latitude + "," + longitude + "(" + tagID + ")"; 			
    			
    			if (body.equals(SECRET_LOCATION_A + secretCode)) { 
    				if (senderEnabled) {
        				if (latitude.equals("0.0") | longitude.equals("0.0")) {
        					SmsManager.getDefault().sendTextMessage(address, null, NO_COORDS + tagID, null, null);
        				}
        				else {
        					SmsManager.getDefault().sendTextMessage(address, null, googleString, null, null);
        				}
    				}

                    this.abortBroadcast(); 
    			}
    			else if (body.equals(SECRET_LOCATION_B + secretCode)) {
    				if (senderEnabled) {
        				if (latitude.equals("0.0") | longitude.equals("0.0")) {
        					SmsManager.getDefault().sendTextMessage(address, null, NO_COORDS + tagID, null, null);
        				}
        				else {
        					SmsManager.getDefault().sendTextMessage(address, null, location, null, null);
        				}
    				}
	
    				this.abortBroadcast();
    			}
    			else if (body.contains("Tag_ID:")) {
    				// ADD TO DATABASE
    				MsgDatabaseHandler dbHandler = new MsgDatabaseHandler(context);
    				
    				// VERIFY IF THE TAG EXISTS IN THE ARRAY
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
    				
    				// CHECK IF THE ADDRESS EXISTS FOR NAMING PURPOSES
    				if (addressExists == null) {    					   					
    					dbHandler.Add_Message(new MessageData(tag, address, lat, lon, _time));
    					toastMsg = "Response Received: " + tag;
    				}
    				else {
    					dbHandler.Add_Message(new MessageData(addressExists, address, lat, lon, _time));
    					toastMsg = "Response Received: " + addressExists;
    				}
    				
    				dbHandler.close();
    				
    				Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
    				
    				this.abortBroadcast();
    			}
    			else if (body.contains("Panic!")) {
    				
    				// OVERRIDE THE SILENT FEATURE
    				AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    				int max = audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
    				audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    				audio.setStreamVolume(AudioManager.STREAM_RING, max, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    				
    				// DEFINE THE NOTIFICATION MANAGER
    				notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    				
    				// START A TIMER
        			mytimer = new Timer(true);
        			
        			// SOUND LOCATION ALARM
    				soundUri = Uri.parse(BEGIN_PATH + context.getPackageName() + FILE_PATH);
    				
    				// DISPLAY TAG ID FOR EMERGENCY
    				String[] splitBody = body.split("\n");
    				String fieldTag = splitBody[1];
    				String[] splitTag = fieldTag.split(":");
    				
    				emergencyTag = splitTag[1].trim();
    				
    				// TIMER FOR NOTIFICATIONS
                    mytask = new TimerTask() {
                        public void run() {
            				// RUN NOTIFICATION ON TIMER
            				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
            				        .setSmallIcon(R.drawable.emergency)
            				        .setContentTitle(PANIC_TXT)
            				        .setContentText(emergencyTag + UNDER_DURRESS)
            				        .setSound(soundUri); //This sets the sound to play

            				// DISPLAY THE NOTIFICATION
            				notificationManager.notify(0, mBuilder.build());
                        }
                    };                    
                    // START TIMER AFTER 5 SECONDS
                    mytimer.schedule(mytask, FIVE_SECONDS);
    			}
            }            
        } 
        
	    // CLEAR THE CACHE ON RECEIVING A MESSAGE
        try {
            MyUpdateReceiver.trimCache(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * METHOD TO VERIFY IF A TAG EXISTS IN THE DATABASE
	 * @param address
	 * @return
	 */
	public String VerifyTagExist(String address) {
		String found = null;
		
		// GET DATA FROM THE DATABASE AND CHECK IF THE ADDRESS EXISTS
    	TagDatabaseHandler mytdb = new TagDatabaseHandler(context);
    	ArrayList<MyTags> mytag_array_from_db = mytdb.Get_Tags();

		for (int ix = 0; ix < mytag_array_from_db.size(); ix++) {		
			String mobile = null;
			String tag = null;
			
			mobile = mytag_array_from_db.get(ix).getMyTagPhoneNumber();
			tag = mytag_array_from_db.get(ix).getMyTag();
			
			// IF EXISTS, USE THE NAME FROM OUR TAG DB, NOT THE SENDER NAME
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