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
package com.xortech.map;

import java.util.ArrayList;

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MapsWithMeApi;
import com.xortech.database.MessageData;
import com.xortech.database.MsgDatabaseHandler;
import com.xortech.sender.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MapsWithMe extends Activity {

private static final int DOUBLE_ZERO = 00;
private SharedPreferences preferences;
private GPSTracker gps;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		// REMOVE THE TITLE BAR FROM THE ACTIONBAR
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowTitleEnabled(false);
        
        gps = new GPSTracker (this);
        
        // MAP TAGS IF THEY ARE AVAILABLE
        MapMyTags();
              
    }
    
    /**
     * METHOD TO ADD POINTS ON MAPS WITH ME MAP
     */
	public void MapMyTags() {

		// INITIALIZE THE DB AND ARRAYLIST FOR DB ELEMENTS 
		MsgDatabaseHandler mdb = new MsgDatabaseHandler(getApplicationContext());
		ArrayList<MessageData> message_array_from_db = mdb.Get_Messages();
		
		final MWMPoint[] points = new MWMPoint[message_array_from_db.size()];
		
		// CHECK IF TAGS EXISTS, IF SO PROCESS THE TAGS
		if (message_array_from_db.size() > 0) {
			
			for (int i = 0; i < message_array_from_db.size(); i++) {

			    String tag = message_array_from_db.get(i).getTag();
			    String latitude = message_array_from_db.get(i).getLatitude();
			    String longitude = message_array_from_db.get(i).getLongitude();
			    String _time = message_array_from_db.get(i).getTime();
			   		    
			    Double lat = Double.parseDouble(latitude);
			    Double lon = Double.parseDouble(longitude);
			     
		        // PROCESS THE TIME DIFFERENCE
			    Long then = Long.parseLong(_time);
				Long now = System.currentTimeMillis();
				String difference = getDifference(now, then);
				
				// COUNTER FOR TIME SPLIT
				int colon = 0;
				
				// Count colons for proper output
				for(int ix = 0; ix < difference.length(); ix++) {
				    if(difference.charAt(ix) == ':') colon++;
				}
				
				// SPLIT THE DIFFERENCE BY A ":"
				String[] splitDiff = difference.split(":");
				String hours = null, minutes = null, seconds = null, str = null;
				
				// CALCULATE THE TIME DIFFERENCE
				switch (colon) {
				case 1:
					if (Integer.parseInt(splitDiff[0]) == DOUBLE_ZERO) {
						seconds = splitDiff[1];
						if (Integer.parseInt(seconds) > 1) {
							str = "Occurred: " + splitDiff[1] + " seconds ago";
						}
						else if (Integer.parseInt(seconds) == 1) {
							str = "Occurred: " + splitDiff[1] + " second ago";
						}
						else if (Integer.parseInt(seconds) == DOUBLE_ZERO) {
							str = "Occurred: " + "Happening Now";
						}		
					}
					else {
						minutes = splitDiff[0];
						if (Integer.parseInt(minutes) > 1) {						
							str = "Occurred: " + splitDiff[0] + " minutes ago";						
						}
						else {
							str = "Occurred: " + splitDiff[0] + " minute ago";
						}		
					}
					break;
				case 2:
					hours = splitDiff[0];
					if (Integer.parseInt(hours) > 1) {
						str = "Occurred: " + splitDiff[0] + " hours ago";
					}
					else {
						str = "Occurred: " + splitDiff[0] + " hour ago";
					}	
					break;
				default:
					str = "Happening Now";
				}
				
				String mPoint = tag + "\n" + str;
				// CALL MAPS WITH ME AND PLOT POINTS ON THE MAP
			    points[i] = new MWMPoint(lat, lon, mPoint);
			    MapsWithMeApi.showPointsOnMap(this, "Total Tags: " + message_array_from_db.size(), points);
			}
			mdb.close();
		}
		else {
			// GET THE NAME OF THIS DEVICE 
			preferences = PreferenceManager.getDefaultSharedPreferences(this);
			String myTagName = preferences.getString("tagID", "Sender-01");
			
			gps.getLocation();
			MapsWithMeApi.showPointOnMap(this, gps.getLatitude(), gps.getLongitude(), myTagName + "\nMy Position");
		}		
	}
	
	public void MapMyData(Double lat, Double lon, String name, int size) {
		try {
			// CONVERT OBJECTS TO MWMPOINTS
		    final MWMPoint[] points = new MWMPoint[size];
		    
		    for (int ix = 0; ix < size; ix++) {
		    	// GET LAT, LONG, AND NAME FROM OBJECT AND ASSIGN IT TO A NEW MWMPOINT
		        points[ix] = new MWMPoint(lat, lon, name);
		    }
		    // SHOW ALL POINTS ON THE MAP
		    MapsWithMeApi.showPointsOnMap(this, "Total Points: " + size, points);
		}
		catch (Exception e) {
			Log.e("Error loading points from Map With Me", " " + e);
		}
	}
	
	/**
	 * METHOD TO CHECK THE TIME DIFFERENCE OF THE RECEVIED SMS WITH THE CURRENT TIME
	 * @param now
	 * @param then
	 * @return
	 */
	public String getDifference(long now, long then){
		
		try {
	        if(now > then) {
	            return DateUtils.formatElapsedTime((now - then)/1000L);
	        }
	        else {
	            return "Time Error!";
	        }
		}
		catch (Exception e) {
			Log.e("Error: ", "" + e);
			return "Time Error!";
		}
    }
	
	@Override
	public void onResume() {
		super.onResume();
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.other_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        switch(item.getItemId()){
        case R.id.quitView:
        	finish();
        	return true;
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
        }
             
        return true;
    }
}

