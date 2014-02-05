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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.xortech.database.MyPanicNumbers;
import com.xortech.database.PanicDatabaseHandler;
import com.xortech.email.Mail;
import com.xortech.map.GPSTracker;
import com.xortech.sender.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
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

public class SenderSend extends Fragment implements OnSharedPreferenceChangeListener {
	
	private static final int THREE_SECONDS = 3000;	
	private static final long ONE_SECOND = 1000L;
	private static final long ONE_MINUTE = 60000L;
	private static final Double FIVE_DIGIT = 100000.0D;
	private static final String GOOGLE_STRING = "http://maps.google.com/maps?q=";
	private static final String PANIC_TEXT = "Panic";
	private static final String STOP_TEXT = "Stop";	
	private static final String DEFAULT_TAGID = "Sender-01";
	private static final String DEFAULT_EMAIL = "myEmail@myEmail.com";
	
	private Button btnShowLocation;
	private Button panicButton;
	private SharedPreferences preferences;
	private Context context;
	private Timer mytimer;
	private Vibrator vibrator;
	private GPSTracker gps;
	
	private boolean panic = false;
	private String latitude = null;
	private String longitude = null;
	private String location = null;
	private String panicLocation = null;
	private String panicMsg = null;
	private String myEmail = null;
	private String name = null;
	
	private ArrayList<String> myList;
	
	/**
	 * CONSTRUCTOR
	 * @param ctx
	 */
	public SenderSend(Context ctx) {
		context = ctx;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.sender_send, container, false);
				
		// GPS INSTANCE
        gps = new GPSTracker(context);
		
		// BUTTONS FOR MAIN LAYOUT
		panicButton = (Button) rootView.findViewById(R.id.panicBtn);
        btnShowLocation = (Button) rootView.findViewById(R.id.sendSABtn);
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

		// LOAD PREFERENCES
        preferences = PreferenceManager.getDefaultSharedPreferences(context);    
        myEmail = preferences.getString("myEmail", DEFAULT_EMAIL);
		name = preferences.getString("tagID", DEFAULT_TAGID);
		
		preferences.registerOnSharedPreferenceChangeListener(this);
		
		// TODO: Check for GMAIL using regular expression
		// (\W|^)[\w.+\-]{0,25}@(yahoo|hotmail|gmail)\.com(\W|$)
		
    	/**
    	 * LISTENER FOR THE SEND LOCATION BUTTON THAT SENDS THIS HANDSETS COORDINATES TO THE 
    	 * PHONE NUMBERS LOCATED IN THE PANIC NUMBERS DATABASE
    	 * 
    	 * */
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {				
		        			        	
		        // GET THE COORDINATE STRING FOR GOOGLE MAPS
		        location = GetCoordinates();
		        
		        try {
		            if (location != null) {
		            	SendToMyPanicNumbers(location);
		            	Toast.makeText(context, "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
		            } else {
		            	Toast.makeText(context, "Coordinates Unavailable", Toast.LENGTH_LONG).show();
		            }
		        } catch (Exception e) {
		        	Log.e("Error sending SMS: ", "" +e );
		        }

			}
		});      
		
    	/**
    	 * LISTENER FOR PANIC BUTTON. SENDS A MESSAGE EVER MINUE FOR TEN MINUTES IF THE PANIC
    	 * FEATURE IS ACTIVATED
    	 * 
    	 * */
        panicButton.setOnClickListener(new View.OnClickListener() {
        	
        	@Override
        	public void onClick(View arg0) {
        		
        		// CHANGE PANIC BUTTON TEXT BASED ON STATUS
        		if (panic == false) {
        			panic = true;
        			panicButton.setText(STOP_TEXT);
        		} else if (panic == true) {
        			panic = false;
        			panicButton.setText(PANIC_TEXT);
        		}
        		
        		if (panic == true) {        			
        			// CRAFT A MESSAGE
        			panicLocation = GetCoordinates();
        			panicMsg = "Panic!\n" + panicLocation;
		                    			
        			// START THE TIMER
        			mytimer = new Timer(true);

                    final TimerTask mytask = new TimerTask() {
                        public void run() {
                        	SendToMyPanicNumbers(panicMsg);
                        }
                    };
                    
                    // START THE TIME AFTER ONE SECOND AND SEND NEW SMS EVERY 60 SECONDS
                    mytimer.schedule(mytask, ONE_SECOND, ONE_MINUTE);
            		
                    // VIBRATE FOR 3000 MILLISECONDS OR 3 SECONDS
            		vibrator.vibrate(THREE_SECONDS);
            		
            		// DISPLAY ACTIVATION
            		Toast.makeText(context, "Panic Activated!", Toast.LENGTH_LONG).show();
        		}
        		// CANCEL PANIC
        		else if (panic == false) {
        			// CANCEL TIMER
        			mytimer.cancel();
        			
        			// DISPLAY CANCEL, SET PANIC FALSE, AND REVERT TEXT 
					Toast.makeText(context, "Panic Cancelled", Toast.LENGTH_LONG).show();
					panic = false;
					panicButton.setText(PANIC_TEXT);
        		}
        	}
        }); 
        return rootView;
	}
	
	/**
	 * METHOD TO CRAFT OUR MESSAGE, WHICH INCLUDES OUR SPECIAL FORMAT FOR THE RECEIVER TO 
	 * LISTEN FOR AND A GOOGLE STRING
	 * 
	 * @param none
	 * @return coordinates
	 * RETURNS A STRING THAT IS LATER USED AS OUR MESSAGE BODY
	 * 
	 * */
	private String GetCoordinates() {
		String coordinates = null;
		
        if (gps.canGetLocation()) {
        	// GET LAST KNOWN LOCATION
        	gps.getLocation();
        	latitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLatitude()) / FIVE_DIGIT);
        	longitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLongitude()) / FIVE_DIGIT);
        	coordinates = "TagID: " + name + "\n" + GOOGLE_STRING + latitude + "," + longitude + "(" + name + ")";
        }  else {
        	showSettingsAlert();
        	coordinates = "TagID: " + name + "\n" + "Unable to retrieve coordinates!";
        }
		return coordinates;
	}
	
	/**
	 * METHOD TO SEND DATA TO OUR PANIC NUMBERS
	 * 
	 * @param msg 
	 * THIS VARIABLE RECEIVES THE STRING WE GENERATED IN THE GETCOORDINATES() METHOD AND
	 * SENDS IT TO THE PHONE NUMBERS/EMAILS LOCATED IN OUR DATABASE
	 *  
	 * */
	public void SendToMyPanicNumbers(String msg) {
    	PanicDatabaseHandler mytdb = new PanicDatabaseHandler(context);
    	ArrayList<MyPanicNumbers> mytag_array_from_db = mytdb.Get_Numbers();
    	String sendMsg = msg;
    	
    	myList = new ArrayList<String>();
    	
		for (int i = 0; i < mytag_array_from_db.size(); i++) {
			String mobile = null;
			String email = null;
			int enabled = 0;
			int pEnabled = 0;
			int eEnabled = 0;
			
			mobile = mytag_array_from_db.get(i).getMyPanicPhoneNumber();	
			enabled = mytag_array_from_db.get(i).getPanicActive();
			email = mytag_array_from_db.get(i).getMyPanicEmail();
			 						
			switch(enabled) {
			case 1:
				pEnabled = mytag_array_from_db.get(i).getPanicActiveP();
				eEnabled = mytag_array_from_db.get(i).getPanicActiveE();
				
				if (enabled != 0 && pEnabled == 1 && eEnabled == 0) {
					SmsManager.getDefault().sendTextMessage(mobile, null, sendMsg, null, null);
				}
				else if (enabled != 0 && pEnabled == 1 && eEnabled == 1) {
					if (email != null || email != " " || email != "") {
						if (email.contains("@")) {
							myList.add(email);
						}
					}
					SmsManager.getDefault().sendTextMessage(mobile, null, sendMsg, null, null);
				}
				else if (enabled != 0 && pEnabled == 0 && eEnabled == 1) {
					if (email != null || email != " " || email != "") {
						myList.add(email);
					}
				} 
				break;
			default:
				/**
				 * DO NOTHING SINCE IT IS DISABLED!
				 */
				break;
			}			
		}
		mytdb.close();
				
		// SendEmail(myList);	
		SendEmailTask task = new SendEmailTask();
		task.execute();
	}
	
	private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
						
			boolean result = false;
			
			String htmlString = GetHTML();
			String myPass = preferences.getString("myPass", "1234");
			String[] emails = new String[myList.size()];
			
			//StringBuilder sEmails = new StringBuilder();
			
			for (int ix = 0; ix < myList.size(); ix++) {
				if (ix == myList.size() - 1) {
					 emails[ix] = myList.get(ix);
				} else {
					emails[ix] = myList.get(ix) + ", ";
				}
			}
						
		    Mail sMail = new Mail(myEmail, myPass); 
		    sMail.setTo(emails); 
		    sMail.setFrom("SenderApp"); 
		    sMail.setSubject("Location Update From " + name); 	    	    
		    sMail.setBody(htmlString); 
		    
		    try {
				result = sMail.send();
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}	
	}
	
	/**
	 * METHOD TO SEND AN EMAIL TO THE PANIC NUMBERS LOCATED IN THE DATABASE
	 * @param panicEmails
	 */
	public void SendEmail(ArrayList<String> panicEmails) {
		String htmlString = GetHTML();
		String myPass = preferences.getString("myPass", "1234");
		String[] emails = new String[panicEmails.size()];

		for (int ix = 0; ix < panicEmails.size(); ix++) {
			if (ix == panicEmails.size() - 1) {
				emails[ix] = panicEmails.get(ix);
			} else {
				emails[ix] = panicEmails.get(ix) + ",";
			}
		}
		
	    Mail sMail = new Mail(myEmail, myPass); 
	    sMail.setTo(emails); 
	    sMail.setFrom("SenderApp"); 
	    sMail.setSubject("Location Update From " + name); 	    	    
	    sMail.setBody(htmlString); 
	    try {
			sMail.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * METHOD TO BUILD AN HTML PAGE FOR AN EMAIL
	 * @return
	 */
	public String GetHTML() {
		String _location = latitude + "," + longitude;
		
		SimpleDateFormat simpleDate = new SimpleDateFormat("MM-dd-yyyy hh:mm");
		String format = simpleDate.format(new Date());
		
		/*
		String locationHTML = String.format("<html><head><title>Sender</title></head><body>" +
				"<h1 style=\"text-align: center;\">Sender Location Update</h1><p " +
				"style=\"text-align: center;\"><img alt=\"Google Map\" " +
				"src=\"http://maps.googleapis.com/maps/api/staticmap?center=%s&amp;zoom=14&amp;" +
				"size=600x300&amp;maptype=roadmap&amp;markers=color:red|label:X|%s&amp;" +
				"key=AIzaSyColiyb-E5gR7KMJzhbN3JUogttE_fEW7g&amp;sensor=false\" " +
				"style=\"width: 600px; height: 300px; border-width: 1px; border-style: " +
				"solid;\" /></p><p style=\"text-align: center;\">" +
				"<strong>TagID:</strong> %s &nbsp;&nbsp;<strong>Last Report:</strong>&nbsp;%s " +
				"&nbsp;&nbsp;<strong>Location:</strong> %s &nbsp;&nbsp;" +
				"<br><br><a href=\"http://maps.google.com/maps?q=%s\">Go To Maps</a></p>" +
				"</body></html>", _location, _location, name, format, _location, _location); 
		*/		
		
		String locationHTML2 = String.format("<html><head><title>Sender</title></head><body><h1 style=\"text-align: center;\">" +
				"Sender Location Update</h1><p style=\"text-align: center;\"><a href=\"http://maps.google.com/maps?q=%s\">" +
				"<img alt=\"Map\" src=\"http://maps.googleapis.com/maps/api/staticmap?center=%s&amp;zoom=15&amp;size=600x300&amp;" +
				"maptype=roadmap&amp;markers=color:red|label:X|%s&amp;key=AIzaSyColiyb-E5gR7KMJzhbN3JUogttE_fEW7g&amp;sensor=false\" " +
				"style=\"width: 600px; height: 300px; border-width: 1px; border-style: solid;\" /></a></p><p style=\"text-align: center;\">" +
				"<strong>TagID:</strong> %s &nbsp; &nbsp;<strong>Time: </strong>%s &nbsp; &nbsp;<strong>Location:</strong>&nbsp;%s</p>" +
				"<p style=\"text-align: center;\"><a href=\"http://maps.google.com/maps?q=%s\">Go To Map</a></p><h3 style=\"text-align: " +
				"center;\">Hybrid View</h3><p style=\"text-align: center;\"><img alt=\"Hybrid\" " +
				"src=\"http://maps.googleapis.com/maps/api/staticmap?center=%s&amp;zoom=17&amp;size=600x300&amp;maptype=hybrid&amp;" +
				"markers=color:red|label:X|%s&amp;key=AIzaSyColiyb-E5gR7KMJzhbN3JUogttE_fEW7g&amp;sensor=false\" style=\"width: 600px; " +
				"height: 300px; border-width: 1px; border-style: solid;\" /></p>" +
				"<h3 style=\"text-align: center;\">Street View</h3><p style=\"text-align: center;\"><img alt=\"Street\" " +
				"src=\"http://maps.googleapis.com/maps/api/streetview?size=600x300&amp;location=%s&amp;key=AIzaSyColiyb-E5gR7KMJzhbN3JUogttE_fEW7g" +
				"&amp;sensor=false\" style=\"border-width: 1px; border-style: solid; " +
				"height: 300px; width: 600px;\" /></p><p style=\"text-align: center;\">" +
				"<strong>Note:</strong> Street View is not guaranteed to show the correct building or perspective to the tag location.</p>" +
				"<p style=\"text-align: center;\"><a href=\"https://coinbase.com/checkouts/4bc224c6764e7908bea274c12badce5e\" " +
				"target=\"_blank\">Donate Bitcoins</a></p><p style=\"text-align: center;\">&nbsp;&copy; 2014 |&nbsp;" +
				"<a href=\"http://sender.xor-tec.com/\">SenderApp</a>&nbsp;by&nbsp;<a href=\"http://xor-tec.com/\">Xor Tech</a>" +
				"&nbsp;| Live in Code</p></body></html>", _location, _location, _location, name, format, _location, _location, _location, _location, _location);
	
		return locationHTML2;
	}
	
    /**
     * METHOD TO SHOW ALERT DIALOG ASKING USER TO ENABLE HIS/HER GPS
     * 
     * @param NONE
     * 
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
      
        // SET DIALOG TITLE
        alertDialog.setTitle("GPS is settings");
  
        // SET DIALOG MESSAGE
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
  
        // PRESS THE SETTIGNS BUTTON, SHOW OPTIONS
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
  
        // ON CANCEL PRESS
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        // SHOW ALERT MESSAGE
        alertDialog.show();
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        preferences.registerOnSharedPreferenceChangeListener(this);
        
        /*
    	myEmail = preferences.getString("myEmail", DEFAULT_EMAIL);
		name = preferences.getString("tagID", DEFAULT_TAGID);
		*/
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (preferences != null) {
        	preferences.unregisterOnSharedPreferenceChangeListener(this);
        }
        MyUpdateReceiver.trimCache(context);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        
        if (preferences != null) {
        	preferences.unregisterOnSharedPreferenceChangeListener(this);
        }
        MyUpdateReceiver.trimCache(context);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("myEmail")) {
			myEmail = preferences.getString("myEmail", DEFAULT_EMAIL);
		}
		else if (key.equals("tagID")) {
			name = preferences.getString("tagID", DEFAULT_TAGID);
		}
	}
}
