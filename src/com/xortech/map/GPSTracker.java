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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
 
public class GPSTracker extends Service implements LocationListener {
 
    private final Context mContext;
 
    // FLAG FOR GPS STATUS 
    boolean isGPSEnabled = false;
 
    // FLAG FOR NETWORK STATUS
    boolean isNetworkEnabled = false;
 
    // FLAG FOR GPS STATUS
    boolean canGetLocation = false;
 
    Location location; 
    double latitude;
    double longitude; 
 
    // THE MINIMUM DISTANCE TO CHANGE UPDATES - IN METERS
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 5 METERS
 
    // THE MINIMUM TIME BETWEEN UPDATES IN MILLISECONDS
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 MINUTE
 
    // DECLARING A LOCATION MANAGER
    protected LocationManager locationManager;
    
    /**
     * CONSTRUCTOR
     * @param context
     */
    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }
    
    /**
     * METHOD TO CHECK THE STATUS OF LOCATION SERVICES
     * @return
     */
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);
 
            // GET GPS STATUS 
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
 
            // GET NETWORK STATUS 
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
            if (!isGPSEnabled && !isNetworkEnabled) {
                // NO NETWORK PROVIDERS AVAILABLE 
            } else {
            	this.canGetLocation = true;
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				// IF GPS IS ENABLED GET THE LAT/LONG USING GPS SERVICES
				if (isGPSEnabled) {
					if (location != null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS Enabled", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return location;
    }
     
    /**
     * METHOD TO STOP RECEIVING GPS UPDATES - SAVES BATTERY!!
     */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }       
    }
     
    /**
     * METHOD TO GET THE LATITUDE OF THE PHONE
     * @return
     */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
         
        return latitude;
    }
     
    /**
     * METHOD TO GET THE LONGITUDE OF THE PHONE 
     * @return
     */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
         
        return longitude;
    }
     
    /**
     * METHOD TO CHECK IF THE GPS OR NETWORK PROVIDE ARE ENABLED FOR GETTING LOCATION DATA
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
 
    @Override
    public void onLocationChanged(Location loc) {
    	latitude = loc.getLatitude();
    	longitude = loc.getLongitude();
    }
 
    @Override
    public void onProviderDisabled(String provider) {
    }
 
    @Override
    public void onProviderEnabled(String provider) {
    }
 
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
 
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
 
}

