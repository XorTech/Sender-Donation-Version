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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.ActionBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import com.xortech.database.MessageData;
import com.xortech.database.MsgDatabaseHandler;
import com.xortech.sender.R;

public class SenderMap extends Activity {
	
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;
    private static final int DOUBLE_ZERO = 00;
    private static final Double FIVE_DIGIT = 100000.0D;
	private static final int NORMAL_MAP = R.id.normal_map;
	private static final int SATELLITE_MAP = R.id.satellite_map;
	private static final int TERRAIN_MAP = R.id.terrain_map;
	private static final int HYBRID_MAP = R.id.hybrid_map;
    
	private LatLngBounds.Builder bounds;
	private GoogleMap gMap;
	private LatLng gCoord;
    
    private int tagCounter = 0;
    private boolean dataExists = false;
    private boolean tagsAvailable =false;
    
    private ArrayList<String> tagArrayList;
    private ArrayList<Integer> markerArrayList;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sender_map);
        
		// REMOVE THE TITLE FROM THE ACTIONBAR
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowTitleEnabled(false);
        
        // CHECK FOR A DATA CONNECTION
        dataExists = CheckInternet();
        
        if (!dataExists) {
        	Toast.makeText(getBaseContext(), "No Data Connection!", Toast.LENGTH_SHORT).show();
        }
        else {
        	// SET UP THE GOOGLE MAP
        	setUpMapIfNeeded();
        	
            tagArrayList = new ArrayList<String>();
            markerArrayList = new ArrayList<Integer>();
            bounds = new LatLngBounds.Builder();
        	
            // PLACE THE ICONS INTO AN ARRAY
            InitializeTagIcons();
            
            GPSTracker gps = new GPSTracker(getBaseContext());
            
            if(gps.canGetLocation()){ 
            	// GET PHONE POSITION
            	double latitude = Math.round(FIVE_DIGIT * gps.getLatitude()) / FIVE_DIGIT;
        		double longitude = Math.round(FIVE_DIGIT * gps.getLongitude()) / FIVE_DIGIT;            
                
                // INITIALIZE LAT/LONG AND ZOOM
                LatLng cameraLatLng = new LatLng(latitude,longitude);
                float cameraZoom = 12;
                
                // CHECK FOR SAVED INSTANCE FROM ROTATION CHANGE
                if(savedInstanceState != null){
                    mapType = savedInstanceState.getInt("map_type", GoogleMap.MAP_TYPE_NORMAL);
                    double savedLat = savedInstanceState.getDouble("lat");
                    double savedLng = savedInstanceState.getDouble("lng");
                    cameraLatLng = new LatLng(savedLat, savedLng);
                    cameraZoom = savedInstanceState.getFloat("zoom", 12);
                }
                
                // MOVE AND ANIMATE CAMERA TO THE CORRECT POSITION
                CameraPosition cameraPosition = new CameraPosition.Builder().target(cameraLatLng).zoom(cameraZoom).build();
                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                
                try {
                	// GET TAGS
                	tagsAvailable = MapMyTags();
                	
                	// ONLY MOVE IF TAG DATA IS AVAILABLE 
                	if (tagsAvailable) {
                    	// SET A LISTENER FOR TAG BOUNDS
                    	gMap.setOnCameraChangeListener(new OnCameraChangeListener() {

                    		@Override
                    		public void onCameraChange(CameraPosition arg0) {
                    			if (tagCounter == 1) {
                    	            // IF ONE TAG, THEN MOVE WITHING ITS BOUNDS
                    				float cameraZoom = 15;
                    	            CameraPosition newCameraPosition = new CameraPosition.Builder().target(gCoord).zoom(cameraZoom).build();
                    	            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                    			}
                    			else {
                        		    // ELSE SHOW BOUNDS
                        		    gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 120));
                    			}
                    		    // REMOVE LISTENER TO PREVENT POSITION RESET ON CAMERA MOVE 
                    		    gMap.setOnCameraChangeListener(null);
                    		}
                    	});
                	}
                }
                catch (Exception e){
                	Toast.makeText(getBaseContext(), "Failed to load tags!", Toast.LENGTH_SHORT).show();
                }
            }
            if (!gps.canGetLocation())
            {
            	showSettingsAlert();
            }     	
        }      
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        boolean backPressed = false;
        
        // Map options
        switch(item.getItemId()){
        case NORMAL_MAP:
            mapType = GoogleMap.MAP_TYPE_NORMAL;
            break;
        case SATELLITE_MAP:
            mapType = GoogleMap.MAP_TYPE_SATELLITE;
            break;
        case TERRAIN_MAP:
            mapType = GoogleMap.MAP_TYPE_TERRAIN;
            break;
        case HYBRID_MAP:
            mapType = GoogleMap.MAP_TYPE_HYBRID;
            break;
        case android.R.id.home:
            onBackPressed();
            backPressed = true;
        }
        
        if (!backPressed) {
        	gMap.setMapType(mapType);
        }
             
        return true;
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // SAVE MAP FOR ORIENTATION CHANGE
        LatLng cameraLatLng = gMap.getCameraPosition().target;
        float cameraZoom = gMap.getCameraPosition().zoom;
        outState.putInt("map_type", mapType);
        outState.putDouble("lat", cameraLatLng.latitude);
        outState.putDouble("lng", cameraLatLng.longitude);
        outState.putFloat("zoom", cameraZoom);
    }
    
    /**
     * METHOD TO GET TAG DATA FROM THE DATABASE
     * @return
     */
	public boolean MapMyTags() {
		boolean tagsAvailable = false;
		
		// INITIALIZE THE DB
		MsgDatabaseHandler mdb = new MsgDatabaseHandler(getBaseContext());
		ArrayList<MessageData> message_array_from_db = mdb.Get_Messages();
		
		// CHECK IF TAG DATA EXISTS, IF SO PROCESS AND ADD MARKERS
		if (message_array_from_db.size() > 0) {
			for (int i = 0; i < message_array_from_db.size(); i++) {
		    	tagCounter++;
				
			    String tag = message_array_from_db.get(i).getTag();
			    String latitude = message_array_from_db.get(i).getLatitude();
			    String longitude = message_array_from_db.get(i).getLongitude();
			    String _time = message_array_from_db.get(i).getTime();
			   		    
			    Double lat = Double.parseDouble(latitude);
			    Double lon = Double.parseDouble(longitude);
			    
		        LatLng coords = new LatLng(lat,lon);
		        gCoord = coords;
		        
		        // PROCESS TIME 
			    Long then = Long.parseLong(_time);
				Long now = System.currentTimeMillis();
				String difference = getDifference(now, then);
				
				// COUNTER FOR TIME SPLIT
				int colon = 0;
				
				// COUNT COLONS FOR PROPER OUTPUT
				for(int ix = 0; ix < difference.length(); ix++) {
				    if(difference.charAt(ix) == ':') colon++;
				}
				
				// SPLIT THE DIFFERENCES BY A ":"
				String[] splitDiff = difference.split(":");
				String hours = null, minutes = null, seconds = null, str = null;
				
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
				try {
					SetMapMarker(tag, coords, str);
					bounds.include(coords);
				}
				catch (Exception e) {
					Log.e("Error: ", "" +e);
				}
			}
			mdb.close();
			tagsAvailable = true;
		}
		else {
			tagsAvailable = false;
		}
		
		return tagsAvailable;
	}
	
	/**
	 * METHOD TO GET THE TIME DIFFERENCE OF THE SMS AND CURRENT TIME
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
	
	/**
	 * METHOD TO PLACE MARKERS ON THE MAP
	 * @param tag
	 * @param coords
	 * @param str
	 */
	public void SetMapMarker(String tag, LatLng coords, String str) {
		int position = 0;
		boolean found = false;

		// SEARCH ARRAY FOR A VALUE
		for (String tagList : tagArrayList) {
			if (tag.equals(tagList)) {
				found = true;
				break;
			}
			else {
				position++;
			}       
		}
	
		// IF NOT FOUND, ADD IT AND ADJUST POSITION
		if (!found) {
			position = 0;
			tagArrayList.add(tag);
			// FIND TAG POSITION, IF EXISTS USE THAT POSITION TO MATCH ICON COLOR
			for (String tagList : tagArrayList) {
				if (tag.equals(tagList)) {
					found = true;
					break;
				}
				else {
					position++;
				}
			}
		}
		
		// MATCH ARRAY POSITION FOR ICONS, IF GREATER THAN 100, ALL ARE BLACK
		if (position <= 19) {					
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(str)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(position))));
		}
		else if (position > 19 && position <= 39) {
			position -= 20;
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(str)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(position))));
		}
		else if (position > 39 && position <= 59) {
			position -= 40;
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(str)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(position))));
		}
		else if (position > 59 && position <= 79) {
			position -= 60;
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(str)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(position))));
		}
		else if (position > 79 && position <= 99) {
			position -= 80;
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(str)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(position))));
		}
		else {
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(str)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(0))));
		}
	} 
	
	/**
	 * METHOD TO SET UP GOOGLE MAPS
	 */
    private void setUpMapIfNeeded() {
        if (gMap == null) 
        {
        	gMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        	
            if (gMap != null) 
            {
                setUpMap();
            }
        }
    }
    
    /**
     * METHOD TO SET UP GOOGLE MAP CONTROLS
     */
    private void setUpMap() {
        gMap.setMyLocationEnabled(true);
        gMap.getUiSettings().setCompassEnabled(true);
        gMap.getUiSettings().setRotateGesturesEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(true);
        gMap.setMapType(mapType);
    }
    
    /**
     * METHOD TO INITIALIZE THE ICON ARRAY
     */
    public void InitializeTagIcons() {
    	markerArrayList.add(R.drawable.black_radar);
    	markerArrayList.add(R.drawable.blue_radar);
    	markerArrayList.add(R.drawable.red_radar);
    	markerArrayList.add(R.drawable.green_radar);
    	markerArrayList.add(R.drawable.orange_radar);
    	markerArrayList.add(R.drawable.pink_radar);
    	markerArrayList.add(R.drawable.yellow_radar);
    	markerArrayList.add(R.drawable.purple_radar);
    	markerArrayList.add(R.drawable.teal_radar);
    	markerArrayList.add(R.drawable.white_radar);
    	markerArrayList.add(R.drawable.brown_radar);
    	markerArrayList.add(R.drawable.deep_radar);
    	markerArrayList.add(R.drawable.gray_radar);
    	markerArrayList.add(R.drawable.light_radar);
    	markerArrayList.add(R.drawable.lime_radar);
    	markerArrayList.add(R.drawable.pea_radar);
    	markerArrayList.add(R.drawable.peach_radar);
    	markerArrayList.add(R.drawable.strawberry_radar);
    	markerArrayList.add(R.drawable.violet_radar);
    	markerArrayList.add(R.drawable.zinc_radar);
    }
    
    /**
     * METHOD TO CHECK FOR AN ACTIVE DATA CONNECTION
     * @return
     */
    public boolean CheckInternet() {
        ConnectivityManager connec = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);      

        if (wifi.isConnected()) {
        	return true;
        } else if (mobile.isConnected()) {
            return true;
        }
        return false;
    }
    
    /**
     * METHOD TO SHOW THE SETTINGS DIALOG FOR THE GPS
     */
	public void showSettingsAlert(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SenderMap.this);
        alertDialogBuilder
                .setMessage("GPS is disabled in your device. Enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                SenderMap.this.startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	}
}


