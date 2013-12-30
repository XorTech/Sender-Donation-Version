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
	
    private GoogleMap gMap;
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;
    public static final int DOUBLE_ZERO = 00;
    private static final Double FIVE_DIGIT = 100000.0D;
	private static final int NORMAL_MAP = R.id.normal_map;
	private static final int SATELLITE_MAP = R.id.satellite_map;
	private static final int TERRAIN_MAP = R.id.terrain_map;
	private static final int HYBRID_MAP = R.id.hybrid_map;
    
    LatLngBounds.Builder bounds;
    
    int tagCounter = 0;
    boolean dataExists = false;
    boolean tagsAvailable =false;
    LatLng gCoord;
    
    ArrayList<MessageData> map_data;    
    ArrayList<String> tagArrayList;
    ArrayList<Integer> markerArrayList;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sender_map);
        
        // Add up button functionality to send user back to home
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Check data connection
        dataExists = CheckInternet();
        
        if (!dataExists) {
        	Toast.makeText(getBaseContext(), "No Data Connection!", Toast.LENGTH_SHORT).show();
        }
        else {
        	// Set up map
        	setUpMapIfNeeded();
        	
        	// Initialize arrays 
            map_data = new ArrayList<MessageData>();
            tagArrayList = new ArrayList<String>();
            markerArrayList = new ArrayList<Integer>();
            bounds = new LatLngBounds.Builder();
        	
            // Put icons into array
            InitializeTagIcons();
            
            GPSTracker gps = new GPSTracker(getBaseContext());
            
            if(gps.canGetLocation()){ 
            	// Get my position with accuracy if available
            	double latitude = Math.round(FIVE_DIGIT * gps.getLatitude()) / FIVE_DIGIT;
        		double longitude = Math.round(FIVE_DIGIT * gps.getLongitude()) / FIVE_DIGIT;            
                
                // Initialize latlng and set zoom
                LatLng cameraLatLng = new LatLng(latitude,longitude);
                float cameraZoom = 12;
                
                // Check if instance is saved when changing landscape
                if(savedInstanceState != null){
                    mapType = savedInstanceState.getInt("map_type", GoogleMap.MAP_TYPE_NORMAL);
                    double savedLat = savedInstanceState.getDouble("lat");
                    double savedLng = savedInstanceState.getDouble("lng");
                    cameraLatLng = new LatLng(savedLat, savedLng);
                    cameraZoom = savedInstanceState.getFloat("zoom", 12);
                }
                
                // Move and animate camera to current position
                CameraPosition cameraPosition = new CameraPosition.Builder().target(cameraLatLng).zoom(cameraZoom).build();
                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                
                try {
                	// Get my tags
                	tagsAvailable = MapMyTags();
                	
                	// Only move if tags available
                	if (tagsAvailable) {
                    	// Set listener for tag bounds
                    	gMap.setOnCameraChangeListener(new OnCameraChangeListener() {

                    		@Override
                    		public void onCameraChange(CameraPosition arg0) {
                    			if (tagCounter == 1) {
                    	            // If one tag, move to it without bounds
                    				float cameraZoom = 15;
                    	            CameraPosition newCameraPosition = new CameraPosition.Builder().target(gCoord).zoom(cameraZoom).build();
                    	            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                    			}
                    			else {
                        		    // Else show bounds
                        		    gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 120));
                    			}
                    		    // Remove listener to prevent position reset on camera move.
                    		    gMap.setOnCameraChangeListener(null);
                    		}
                    	});
                	}
                }
                // Catch error if unable to get tags
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
    
	/**
	 * Function to create options menu
	 * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }
    
	/**
	 * Function to listen for options item selected
	 * */
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
            break;
        }
        
        if (!backPressed) {
        	gMap.setMapType(mapType);
        }
        
        return true;
    }
    
	/**
	 * Function to save map instance for portrait/landscape movements
	 * */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save the map type so when we change orientation, the map type can be restored
        LatLng cameraLatLng = gMap.getCameraPosition().target;
        float cameraZoom = gMap.getCameraPosition().zoom;
        outState.putInt("map_type", mapType);
        outState.putDouble("lat", cameraLatLng.latitude);
        outState.putDouble("lng", cameraLatLng.longitude);
        outState.putFloat("zoom", cameraZoom);
    }
    
	/**
	 * Function to get tag data from DB
	 * */
	public boolean MapMyTags() {
		boolean tagsAvailable = false;
		
		// Initialize db and arrayList for db elements
		MsgDatabaseHandler mdb = new MsgDatabaseHandler(getApplicationContext());
		ArrayList<MessageData> message_array_from_db = mdb.Get_Messages();
		
		// Check if tags exist. If so, process and add markers
		if (message_array_from_db.size() > 0) {
			// Strip data from db and send as a marker
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
		        
		        // Process Time
			    Long then = Long.parseLong(_time);
				Long now = System.currentTimeMillis();
				String difference = getDifference(now, then);
				
				// counter for time split
				int colon = 0;
				
				// Count colons for proper output
				for(int ix = 0; ix < difference.length(); ix++) {
				    if(difference.charAt(ix) == ':') colon++;
				}
				
				// Split the difference by colon
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
	 * Function to check time difference of SMS and current time
	 * */
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
	 * Function to place markers on map per tag
	 * */
	public void SetMapMarker(String tag, LatLng coords, String str) {
		int position = 0;
		boolean found = false;

		// Search array for value
		for (String tagList : tagArrayList) {
			if (tag.equals(tagList)) {
				found = true;
				break;
			}
			else {
				position++;
			}       
		}
	
		// If not found in array, add it and adjust position for new icon
		if (!found) {
			position = 0;
			tagArrayList.add(tag);
			// Find tag position in array. If exist use that position to match icon color
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
		
		// Match array positions for icons. If greater than 100 all are black
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
		// Else all icons are black!
		else {
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(str)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(0))));
		}
	} 
	
	/**
	 * Function to set up gMap, if required
	 * */
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
	 * Function to set up map controls
	 * */
    private void setUpMap() {
        gMap.setMyLocationEnabled(true);
        gMap.getUiSettings().setCompassEnabled(true);
        gMap.getUiSettings().setRotateGesturesEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(true);
        gMap.setMapType(mapType);
    }
    
	/**
	 * Function to initialize icon array
	 * */
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
	 * Function to for data connection
	 * */
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
	 * Function to show settings alert dialog
	 * On pressing Settings button will launch Settings Options
	 * */
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
        // TODO Auto-generated method stub
        super.onBackPressed();
    }
}


