package com.xortech.sender;

import java.util.ArrayList;

import com.xortech.database.MsgDatabaseHandler;
import com.xortech.database.MessageData;
import com.xortech.database.MyTags;
import com.xortech.database.TagDatabaseHandler;
import com.xortech.map.SenderMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SenderReceive extends Fragment {

	public static final String DEFAULT_PHONE = "+18775550000";
	public static final String SECRET_LOCATION_C = "*3*";
	public static final String SECRET_LOCATION_D = "*4*";
	public static final String DEFAULT_PASSWORD = "1234";
	public static final String ADDRESS = "address";
	public static final String BODY = "body";
	public static final String GOOGLE_STRING = "http://maps.google.com/maps?q=";
	public static final int DOUBLE_ZERO = 00;
	public static final int ZERO = 0;
	public static final String AT = "@";
	public static final int RESET = 1;
	public static final int SEND = 2;
	
	Button locateTags;
	Button deleteSMS;
	Button updateList;
	Button mapTags;
	SharedPreferences preferences;
	Context context;
	ListView smsListView;
    Message_Adapter mAdapter;
    MsgDatabaseHandler db;
    String toastMsg;
    	
	String msg = null;
	int counter = 0;
    
    ArrayList<MessageData> message_data = new ArrayList<MessageData>();
    
	/**
	 * SenderReceive Constructor
	 * */
	public SenderReceive(Context ctx) {
		context = ctx;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.sender_receive, container, false);		
		
        // Load preferences
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        
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
		 * Listener to request A-GPS update
		 * */
        updateList.setOnLongClickListener(new OnLongClickListener() { 
            @Override
            public boolean onLongClick(View v) {
            	SendToMyTags(RESET);
            	Toast.makeText(context, "GPS reset sent. This could take a bit.", Toast.LENGTH_LONG).show();
                return true;
            }
        });
        
		/**
		 * Listener to update the list in the main view
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
		 * Listener for clicking tags on the map
		 * */
        mapTags.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View arg0) {
        		try {
            		Intent mapIntent = new Intent(getActivity().getBaseContext(), SenderMap.class);       		
            		startActivity(mapIntent);
        		} catch (Exception e) {
        			Log.e("Error loading map.", "" + e);
        			Toast.makeText(context, "Error: Problem loading Google Maps!", Toast.LENGTH_LONG).show();
        		}

        	}
        });
        
        /**
		 * Listener for the delete button
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
		 * Listener for the locate button
		 * */
        locateTags.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				try {
					SendToMyTags(SEND);
			        Toast.makeText(context, "Location Requests Sent", Toast.LENGTH_LONG).show();				
				} catch (Exception e) {
					Log.e("Error locating tags: ", "" + e);
				}

			}
		}); 
        return rootView;
	}
	
	/**
	 * Function to get the time difference between SMS and current time
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
			return "Time Error!";
		}
    }
	
	/**
	 * Function to refresh data from DB into the list
	 * */
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
	 * Function to send SMS to tags
	 * */
	public void SendToMyTags(int action) {
    	TagDatabaseHandler mytdb = new TagDatabaseHandler(context);
    	ArrayList<MyTags> mytag_array_from_db = mytdb.Get_Tags();
    	
		for (int i = 0; i < mytag_array_from_db.size(); i++) {
			String tagMsg = null;
			String mobile = null;
			String secret = null;
			
			mobile = mytag_array_from_db.get(i).getMyTagPhoneNumber();
			secret = mytag_array_from_db.get(i).getTagSecret();
			
			if (action == SEND) {
				tagMsg = SECRET_LOCATION_C + secret;	
			}
			else if (action == RESET) {
				tagMsg = SECRET_LOCATION_D + secret;
			}
			SmsManager.getDefault().sendTextMessage(mobile, null, tagMsg, null, null);
		}
		mytdb.close();
	}
	
	/**
	 * Function for toast messages
	 * */
	public void Show_Toast(String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		try {
			Set_Referash_Data();
		}
		catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Main class used for managing array of tag data
	 * */
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
		 * Function to get tag data from array for display
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
		   	    
		    // Process Time
		    String _time = (message.getTime());
		
		    Long then = Long.parseLong(_time);
			Long now = System.currentTimeMillis();
			String difference = getDifference(now, then);
			
			// counter for time split
			int colon = 0;
			
			// Count colons for proper output
			for(int i = 0; i < difference.length(); i++) {
			    if(difference.charAt(i) == ':') colon++;
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
			
			/**
			 * Listener to show data on item click
			 * */
		    holder.lastReport.setText(str);
		    holder.locate.setOnClickListener(new OnClickListener() {

		    	@Override
		    	public void onClick(View v) {
		    		// TODO Auto-generated method stub
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
			 * Listener delete an individual message
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

		class UserHolder {
		    TextView tagID;
		    TextView location;
		    TextView lastReport;
		    Button locate;
		    Button remove;
		}
	}
}