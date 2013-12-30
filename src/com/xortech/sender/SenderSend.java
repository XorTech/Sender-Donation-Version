package com.xortech.sender;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.xortech.database.MyPanicNumbers;
import com.xortech.database.PanicDatabaseHandler;
import com.xortech.map.GPSTracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class SenderSend extends Fragment {
	
	// Constants
	public static final int THREE_SECONDS = 3000;	
	public static final long ONE_SECOND = 1000L;
	public static final long ONE_MINUTE = 60000L;
	public static final Double FIVE_DIGIT = 100000.0D;
	public static final String GOOGLE_STRING = "http://maps.google.com/maps?q=";
	public static final String DEFAULT_PHONE = "+18775550000";
	public static final String PANIC_TEXT = "Panic";
	public static final String STOP_TEXT = "Stop";	
	public static final String DEFAULT_NUMBER = "+18775550000";
	public static final String DEFAULT_TAGID = "Sender-01";
	
	// Instances
	Button btnShowLocation;
	Button panicButton;
	SharedPreferences preferences;
	Context context;
	Timer mytimer;
	Vibrator vibrator;
	GPSTracker gps;
	
	// Initialize variables
	boolean panic = false;
	String latitude = null;
	String longitude = null;
	String location = null;
	String panicLocation = null;
	String returnNumber = null;
	String panicMsg = null;
	
	/**
	 * SenderSend Constructor
	 * */
	public SenderSend(Context ctx) {
		context = ctx;
	}
	
	/**
	 * SenderSend OnCreate
	 * */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.sender_send, container, false);
		
		// GPS Instance
        gps = new GPSTracker(context);
		
		// Buttons for main layout
		panicButton = (Button) rootView.findViewById(R.id.panicBtn);
        btnShowLocation = (Button) rootView.findViewById(R.id.sendSABtn);
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

		// Load preferences
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);       
		
    	/**
    	 * Normal location sender button for sending coordinates
    	 * */
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {				
		        			        	
		        // Get coordinates string for Google Maps
		        location = GetCoordinates();
		        
		        try {
		            if (location != null) {
		            	SendToMyPanicNumbers(location);
		            	Toast.makeText(context, "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
		            }
		            else {
		            	Toast.makeText(context, "Coordinates Unavailable", Toast.LENGTH_LONG).show();
		            }
		        } catch (Exception e) {
		        	Log.e("Error sending SMS: ", "" +e );
		        }

			}
		});      
		
    	/**
    	 * Listener for panic button. Sends message every minute for ten minutes
    	 * */
        panicButton.setOnClickListener(new View.OnClickListener() {
        	
        	@Override
        	public void onClick(View arg0) {
        		
        		// Change adjust panic button text based on boolean
        		if (panic == false) {
        			panic = true;
        			panicButton.setText(STOP_TEXT);
        		}
        		else if (panic == true) {
        			panic = false;
        			panicButton.setText(PANIC_TEXT);
        		}
        		
        		if (panic == true) {        			
        			// Craft message
        			panicLocation = GetCoordinates();
        			panicMsg = "Panic!\n" + panicLocation;
		                    			
        			// Start timer
        			mytimer = new Timer(true);

                    final TimerTask mytask = new TimerTask() {
                        public void run() {
                        	SendToMyPanicNumbers(panicMsg);
                        }
                    };
                    
                    // Start timer after 1 second and send new SMS every 60 seconds
                    mytimer.schedule(mytask, ONE_SECOND, ONE_MINUTE);
            		
                    // Vibrate for 3000 milliseconds or 3 seconds
            		vibrator.vibrate(THREE_SECONDS);
            		
            		// Display activation
            		Toast.makeText(context, "Panic Activated!", Toast.LENGTH_LONG).show();
        		}
        		// Cancel panic
        		else if (panic == false) {
        			// Cancel timer
        			mytimer.cancel();
        			
        			// Display cancel, set panic false, and revert text
					Toast.makeText(context, "Panic Cancelled", Toast.LENGTH_LONG).show();
					panic = false;
					panicButton.setText(PANIC_TEXT);
        		}
        	}
        }); 
        return rootView;
	}
	
	/**
	 * Function craft coordinates into a Google String
	 * */
	private String GetCoordinates() {
		String coordinates = null;
		String tagID = null;
		tagID = preferences.getString("tagID", DEFAULT_TAGID);
		
        if (gps.canGetLocation()) {
        	// Get last known location
        	latitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLatitude()) / FIVE_DIGIT);
        	longitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLongitude()) / FIVE_DIGIT);
        	coordinates = "TagID: " + tagID + "\n" + GOOGLE_STRING + latitude + "," + longitude + "(" + tagID + ")";
        }
        else {
        	showSettingsAlert();
        	coordinates = "TagID: " + tagID + "\n" + "Unable to retrieve coordinates!";
        }
		return coordinates;
	}
	
	/**
	 * Function to send SMS to tags
	 * */
	public void SendToMyPanicNumbers(String msg) {
    	PanicDatabaseHandler mytdb = new PanicDatabaseHandler(context);
    	ArrayList<MyPanicNumbers> mytag_array_from_db = mytdb.Get_Numbers();
    	String sendMsg = msg;
    	
		for (int i = 0; i < mytag_array_from_db.size(); i++) {
			String mobile = null;
			mobile = mytag_array_from_db.get(i).getMyPanicPhoneNumber();			
			SmsManager.getDefault().sendTextMessage(mobile, null, sendMsg, null, null);
		}
		mytdb.close();
	}
	
    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
      
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
  
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
}
