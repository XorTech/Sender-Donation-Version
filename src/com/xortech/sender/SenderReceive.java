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

import com.xortech.database.MsgDatabaseHandler;
import com.xortech.database.MessageData;
import com.xortech.database.MyTags;
import com.xortech.database.TagDatabaseHandler;
import com.xortech.map.MapsWithMe;
import com.xortech.map.SenderMap;
import com.xortech.sender.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SenderReceive extends Fragment implements OnSharedPreferenceChangeListener {

	private static final String SECRET_LOCATION_C = "*3*";
	private static final String GOOGLE_STRING = "http://maps.google.com/maps?q=";
	private static final int DOUBLE_ZERO = 00;
	private static final String GMAP = "1";
	
	private Button locateTags;
	private Button deleteSMS;
	private Button updateList;
	private Button mapTags;
	private Context context;
	private ListView smsListView;
	private Message_Adapter mAdapter;
	private MsgDatabaseHandler db;
	private SharedPreferences preferences;
	private String mapType = null;
    
	private static ArrayList<MessageData> message_data = new ArrayList<MessageData>();
    
	public SenderReceive(Context ctx) {
		context = ctx;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.sender_receive, container, false);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		mapType = preferences.getString("mapType", GMAP);
		
		preferences.registerOnSharedPreferenceChangeListener(this);
        
    	try {
    		smsListView = (ListView) rootView.findViewById(R.id.SMSList);
    		smsListView.setItemsCanFocus(false);
    	} 
    	catch (Exception e) {
    	    Log.e("Error: ", "" + e);
    	}
        
	    locateTags = (Button) rootView.findViewById(R.id.getLocs);
	    deleteSMS = (Button) rootView.findViewById(R.id.deleteBtn);
	    updateList = (Button) rootView.findViewById(R.id.UpdateList);
	    mapTags = (Button) rootView.findViewById(R.id.getMapBtn);
        
		/**
		 * LISTEN FOR THE UPDATE BUTTON PRESS
		 * */
        updateList.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View arg0) {
        		try {
            		Set_Referash_Data();
        		} catch (Exception e) {
        			Log.e("Error updating list: ", "" + e);
        		}
        	}
        });
              
        /**
		 * LISTEN FOR THE MAP BUTTON PRESS
		 * */
        mapTags.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View arg0) {
        		
        		if (mapType.equals(GMAP)) {
            		try {
                		Intent mapIntent = new Intent(getActivity().getBaseContext(), SenderMap.class);       		
                		startActivity(mapIntent);
            		} catch (Exception e) {
            			Log.e("Error loading map.", "" + e);
            			Toast.makeText(context, "Error: Problem loading Google Maps!", Toast.LENGTH_LONG).show();
            		}
        		} else {
            		try {
                		Intent mapIntent = new Intent(getActivity().getBaseContext(), MapsWithMe.class);       		
                		startActivity(mapIntent);
            		} catch (Exception e) {
            			Log.e("Error loading map.", "" + e);
            			Toast.makeText(context, "Error: Problem loading Google Maps!", Toast.LENGTH_LONG).show();
            		}
        		}

        	}
        });
        
        /**
		 * LISTEN FOR THE DELETE BUTTON PRESS
		 * */
        deleteSMS.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View arg0) {
        		db = new MsgDatabaseHandler(context); 
        		try {
        			db.Delete_All_Messages();
        			Toast.makeText(context, "Messages Deleted", Toast.LENGTH_LONG).show();      			
        		}
        		catch (Exception e) {
        			Toast.makeText(context, "Error Deleting Messages", Toast.LENGTH_LONG).show();
        		}  
        		Set_Referash_Data();
        	}
        });
          
        /**
		 * LISTEN FOR THE LOCATE BUTTON PRESS
		 * */
        locateTags.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				try {
					SendToMyTags();
			        Toast.makeText(context, "Location Requests Sent", Toast.LENGTH_LONG).show();				
				} catch (Exception e) {
					Log.e("Error locating tags: ", "" + e);
				}

			}
		}); 
        return rootView;
	}
	
	/**
	 * METHOD TO GET THE TIME DIFFERENCE FROM A SMS AND THE CURRENT TIME
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
			return "Time Error!";
		}
    }
	
	/**
	 * METHOD TO UPDATE THE LISTVIEW DATA
	 */
	public void Set_Referash_Data() {
		message_data.clear();
		db = new MsgDatabaseHandler(context);
		ArrayList<MessageData> message_array_from_db = db.Get_Messages();

		for (int i = 0; i < message_array_from_db.size(); i++) {

		    int tempIdNo = message_array_from_db.get(i).getID();
		    String tag = message_array_from_db.get(i).getTag();
		    String mobile = message_array_from_db.get(i).getPhoneNumber();
		    String latitude = message_array_from_db.get(i).getLatitude();
		    String longitude = message_array_from_db.get(i).getLongitude();
		    String time = message_array_from_db.get(i).getTime();
		    
		    MessageData msg = new MessageData();
		    
		    msg.setID(tempIdNo);
		    msg.setTag(tag);
		    msg.setPhoneNumber(mobile);
		    msg.setLatitude(latitude);
		    msg.setLongitude(longitude);
		    msg.setTime(time);
		    
		   	message_data.add(msg);
		}
		
		db.close();
		
		mAdapter = new Message_Adapter(getActivity(), R.layout.sender_display_msg, message_data);
		smsListView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}
	
	/**
	 * METHOD TO SEND SMS TO TAGS IN THE DATABASE
	 * @param action
	 */
	public void SendToMyTags() {
    	TagDatabaseHandler mytdb = new TagDatabaseHandler(context);
    	ArrayList<MyTags> mytag_array_from_db = mytdb.Get_Tags();
    	
		for (int i = 0; i < mytag_array_from_db.size(); i++) {
			String tagMsg = null;
			String mobile = null;
			String secret = null;
			int enabled = 0;
			
			mobile = mytag_array_from_db.get(i).getMyTagPhoneNumber();
			secret = mytag_array_from_db.get(i).getTagSecret();
			enabled = mytag_array_from_db.get(i).getTagStatus();
			
			System.out.println(mobile);
			
			if (enabled == 1) {
				tagMsg = SECRET_LOCATION_C + secret;	
				SmsManager.getDefault().sendTextMessage(mobile, null, tagMsg, null, null);
			}
		}
		mytdb.close();
	}
	
	/**
	 * METHOD TO HANDLE TOAST MESSAGES
	 * @param msg
	 */
	public void Show_Toast(String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		preferences.registerOnSharedPreferenceChangeListener(this);
		
		mapType = preferences.getString("mapType", GMAP);
		
		try {
			Set_Referash_Data();
		}
		catch (Exception e) {
			Log.e("Error resuming ", "" + e);
		}
	}
	
	/**
	 * CLASS FOR HANDLING MESSAGE DATA AND THE LAYOUT
	 *
	 */
	public class Message_Adapter extends ArrayAdapter<MessageData> {
		Activity activity;
		int layoutResourceId;
		MessageData message;
		ArrayList<MessageData> data = new ArrayList<MessageData>();
		
		public Message_Adapter(Activity act, int layoutResourceId, ArrayList<MessageData> data) {
		    super(act, layoutResourceId, data);
		    this.layoutResourceId = layoutResourceId;
		    this.activity = act;
		    this.data = data;
		    notifyDataSetChanged();
		}
		
		/**
		 * METHOD TO GET DATA FROM THE ARRAY..AND DISPLAY IT
		 * */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
		    UserHolder holder = null;

		    if (row == null) {
		    	LayoutInflater inflater = LayoutInflater.from(activity);
		    	row = inflater.inflate(layoutResourceId, parent, false);
		    	holder = new UserHolder();
		    	holder.tagID = (TextView) row.findViewById(R.id.tag_id_txt);
		    	holder.location = (TextView) row.findViewById(R.id.location_txt);
		    	holder.lastReport = (TextView) row.findViewById(R.id.last_report_txt);
		    	holder.locate = (Button) row.findViewById(R.id.btn_locate);
		    	holder.remove = (Button) row.findViewById(R.id.btn_remove);
		    	row.setTag(holder);
		    }
		    else {
		    	holder = (UserHolder) row.getTag();
		    }
		    
		    message = data.get(position);
		    holder.locate.setTag(message.getID());
		    holder.remove.setTag(message.getID());
		    
		    String _tag = (message.getTag());
		    final String _tagID = "Tag: " + _tag;
		    holder.tagID.setText(_tagID);
		    
		    final String lat = (message.getLatitude());
		    final String lon = (message.getLongitude());
		    String location = "Location: " + lat + "," + lon;
		    holder.location.setText(location);
		   	    
		    // PROCESS TIME
		    String _time = (message.getTime());
		
		    Long then = Long.parseLong(_time);
			Long now = System.currentTimeMillis();
			String difference = getDifference(now, then);
			
			// COUNTER FOR TIME SPLIT
			int colon = 0;
			
			// COUNT COLONS FOR OUTPUT
			for(int i = 0; i < difference.length(); i++) {
			    if(difference.charAt(i) == ':') colon++;
			}
			
			// SPLIT THE DIFFERENCE BY THE ":"
			String[] splitDiff = difference.split(":");
			String hours = null, minutes = null, seconds = null, str = null;
			
			// CALCULATE THE TIME DISPLAY FOR EACH TAG
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
			
		    holder.lastReport.setText(str);
		    
		    holder.locate.setOnClickListener(new OnClickListener() {

		    	@Override
		    	public void onClick(View v) {
		    		try {
		    			String latlon = lat + "," + lon;
		    			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(GOOGLE_STRING + latlon + "(" + _tagID + ")"));
		    			startActivity(intent);
		    		} 
		    		catch (Exception e) {
		    			Log.e("Error: ", "" + e);
		    		}
		    	}
		    });
		    
		    /**
			 * LISTENER TO REMOVE AN INDIVIDUAL TAG ENTRY
			 * */
		    holder.remove.setOnClickListener(new OnClickListener() {

		    	@Override
		    	public void onClick(final View v) {
		    		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		    		adb.setTitle("Confirm Delete!");
		    		adb.setMessage("Are you sure you want to delete this position?");
		    		final int user_id = Integer.parseInt(v.getTag().toString());
		    		adb.setNegativeButton("Cancel", null);
		    		adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
		    			@Override
		    			public void onClick(DialogInterface dialog,int which) {
		    				// MyDataObject.remove(positionToRemove);
		    				MsgDatabaseHandler dBHandler = new MsgDatabaseHandler(context);
		    				dBHandler.Delete_Message(user_id);
		    				Set_Referash_Data();
		    				SenderReceive.this.onResume();
		    			}
				    });
		    		adb.show();
		    	}
		    });
		    return row;
		}
		/**
		 * HOLDER CLASS
		 *
		 */
		class UserHolder {
		    TextView tagID;
		    TextView location;
		    TextView lastReport;
		    Button locate;
		    Button remove;
		}
	}
    
    @Override
    public void onStart() {
        super.onStart();
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
    }
    
    @Override 
    public void onDestroy() {
    	super.onDestroy();
    	if (preferences != null) {
    		preferences.unregisterOnSharedPreferenceChangeListener(this);
    	}
    	MyUpdateReceiver.trimCache(context);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals("mapType")) {
			mapType = preferences.getString("mapType", GMAP);
		}		
	}
}