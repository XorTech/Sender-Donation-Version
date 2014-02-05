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
package com.xortech.multitag;

import java.util.ArrayList;

import com.xortech.database.MyTags;
import com.xortech.database.TagDatabaseHandler;
import com.xortech.extras.ExpandAnimation;
import com.xortech.sender.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TagAddMain extends Activity {
	
	private static String TAG_ID = "TagID: ";
	private static String TAG_PN = "Mobile: ";
	private static String TAG_SC = "Secret: ";
	
	private Button addBtn;
	private ListView tagList;
	private ArrayList<MyTags> tag_data = new ArrayList<MyTags>();
	private Tag_Adapter tAdapter;
	private TagDatabaseHandler tdb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.sender_tags);
    	    	
		// REMOVE THE TITLE FROM THE ACTIONBAR
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowTitleEnabled(false);
    	
    	try {
    		tagList = (ListView) findViewById(R.id.tagList);
    		tagList.setItemsCanFocus(false);
    		addBtn = (Button) findViewById(R.id.add_btn);

    		Set_Referash_Data();

    	} 
    	catch (Exception e) {
    		Log.e("some error", "" + e);
    	}
    	
    	addBtn.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			Intent addTag = new Intent(TagAddMain.this, TagAddUpdate.class);
    			addTag.putExtra("called", "add");
    			addTag.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    			startActivity(addTag);
    			finish();
    		}
    	});
    	
    	/**
    	 * LISTENER TO OPEN/CLOSE THE EXPANSION OF EACH CLICKED ITEM IN THE LISTVIEW
    	 */
        tagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                View toolbar = view.findViewById(R.id.expandable2);

                // CREATE AND EXPAND ANIMATION FOR THE ITEM 
                ExpandAnimation expandAni = new ExpandAnimation(toolbar, 500);

                // START ANIMATION ON THE TOOLBAR
                toolbar.startAnimation(expandAni);
            }
        });
    }
    
    /**
     * METHOD TO REFRESH THE TAG DATA ON THE USER'S SCREEN
     */
    public void Set_Referash_Data() {
    	tag_data.clear();
    	tdb = new TagDatabaseHandler(this);
    	ArrayList<MyTags> tag_array_from_db = tdb.Get_Tags();

		for (int i = 0; i < tag_array_from_db.size(); i++) {

			int tTagid = tag_array_from_db.get(i).getID();
			String tag = tag_array_from_db.get(i).getMyTag();
			String mobile = tag_array_from_db.get(i).getMyTagPhoneNumber();
			String secret = tag_array_from_db.get(i).getTagSecret();
			int active = tag_array_from_db.get(i).getTagStatus();
			
			MyTags mTag = new MyTags();
			mTag.setID(tTagid);
			mTag.setMyTag(tag);
			mTag.setTagSecret(secret);
			mTag.setMyTagPhoneNumber(mobile);
			mTag.setTagStatus(active);

			tag_data.add(mTag);
		}
		tdb.close();
		
		tAdapter = new Tag_Adapter(TagAddMain.this, R.layout.sender_tag_item, tag_data);
		tagList.setAdapter(tAdapter);
		tAdapter.notifyDataSetChanged();
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
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    }

    @Override
    public void onResume() {
    	super.onResume();   	
    	Set_Referash_Data();
    }
    
	/**
	 * CLASS TO HANDLE THE TAG VIEW
	 * */
    public class Tag_Adapter extends ArrayAdapter<MyTags> {
    	private Activity activity;
    	private int layoutResourceId;
    	private MyTags mTag;
		private int activeHolder = 0;
		private UserHolder holder = null;
		private boolean tActive = false;
		
    	ArrayList<MyTags> data = new ArrayList<MyTags>();

    	public Tag_Adapter(Activity act, int layoutResourceId, ArrayList<MyTags> data) {
    		super(act, layoutResourceId, data);
    		this.layoutResourceId = layoutResourceId;
    		this.activity = act;
    		this.data = data;
    		notifyDataSetChanged();
    	}

    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		View row = convertView;

    		if (row == null) {
    			LayoutInflater inflater = LayoutInflater.from(activity);

    			row = inflater.inflate(layoutResourceId, parent, false);
    			holder = new UserHolder();
    			holder.tag = (TextView) row.findViewById(R.id.tag_name_txt);
    			holder.mobile = (TextView) row.findViewById(R.id.tag_phone_txt);
    			holder.secret = (TextView) row.findViewById(R.id.tag_secret_txt);
    			holder.edit = (Button) row.findViewById(R.id.btnUpdate2);
    			holder.delete = (Button) row.findViewById(R.id.btnDelete2);
    			holder.active = (ToggleButton) row.findViewById(R.id.expandBtn2);
    			
    			row.setTag(holder);
    		} else {
    			holder = (UserHolder) row.getTag();
    		}
    		
    		mTag = data.get(position);
    		
    		holder.edit.setTag(mTag.getID());
    		holder.delete.setTag(mTag.getID());
    		
    		holder.tag.setText(TAG_ID + mTag.getMyTag());
    		holder.mobile.setText(TAG_PN + mTag.getMyTagPhoneNumber());
    		holder.secret.setText(TAG_SC + mTag.getTagSecret());
    		
    		if (mTag.getTagStatus() == 1) {
    			tActive = true;
    		} else {
    			tActive = false;
    		} 
    		
    		holder.active.setChecked(tActive);

    		/**
    		 * LISTENER FOR THE ACTIVE/INACTIVE STATUS OF THE PANIC NUMBER LOCATED IN THE LISTVIEW.
    		 * IF DISABLED, THE USER WILL NOT SEND EMAILS OR SMS NOTIFICATIONS TO THIS NUMBER
    		 * 
    		 */
    		holder.active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					
					if (isChecked) {  
		    			tActive = true;
		    			activeHolder = 1;
		    		} else {
		    			tActive = false;
		    			activeHolder = 0;
		    		}
	    			UpdateTagDB(position, activeHolder);							
				}
				
			});
    		
    		holder.edit.setOnClickListener(new OnClickListener() {

    			@Override
    			public void onClick(View v) {
    				Log.i("Edit Button Clicked", "**********");

    				Intent update_user = new Intent(activity, TagAddUpdate.class);
    				update_user.putExtra("called", "update");
    				update_user.putExtra("USER_ID", v.getTag().toString());
    				activity.startActivity(update_user);
    			}
    		});
    		
    		holder.delete.setOnClickListener(new OnClickListener() {

    			@Override
    			public void onClick(final View v) {

    				AlertDialog.Builder adb = new AlertDialog.Builder(activity);
    				adb.setTitle("Comfirm Delete!");
    				adb.setMessage("Are you sure you want to delete this tag? ");
    				final int tag_id = Integer.parseInt(v.getTag().toString());
    				adb.setNegativeButton("Cancel", null);
    				adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						// MyDataObject.remove(positionToRemove);
    						TagDatabaseHandler dBHandler = new TagDatabaseHandler(getBaseContext());
    						dBHandler.Delete_Tag(tag_id);
    						TagAddMain.this.onResume();
    					}
    				});
    				adb.show();
    			}
    		});
    		return row;
    	}
    	
    	/**
    	 * METHOD TO UPDATE THE TAG DATABASE BASED ON THE USER INTERACTION WITH THE 
    	 * INDIVIDUAL PANIC NUMBER CONTENT
    	 * 
    	 * @param position
    	 * THE POSITION OF THE LIST VIEW TO READ/CHANGE AN INDIVIDUAL ROW'S DATA
    	 * @param value
    	 * INTEGER VALUE FOR TRUE OR FALSE...BASED ON THE TOGGLE BUTTON'S CURRENT POSITION
    	 * @param section
    	 * THE TOGGLE BUTTON ON THE UI THAT WAS CHANGED --> 1 FOR ACTIVE, 2 FOR PHONE NUMBER, 3 FOR EMAIL
    	 * 
    	 */
    	public void UpdateTagDB(int position, int value) {
    		 		
			MyTags updateTags = new MyTags();
			TagDatabaseHandler dBHandlerE = new TagDatabaseHandler(getBaseContext());
			
			updateTags = data.get(position);
    		updateTags.setTagStatus(value);    		

    		
    		updateTags = new MyTags(updateTags.getID(), updateTags.getMyTag(), 
    				updateTags.getMyTagPhoneNumber(), updateTags.getTagSecret(), 
    				updateTags.getTagStatus());

			dBHandlerE.Update_Tag(updateTags);
			dBHandlerE.close();
			
			notifyDataSetChanged();
					
    	}

    	class UserHolder {
    		TextView tag;
    		TextView mobile;
    		TextView secret;
    		ToggleButton active;
    		Button edit;
    		Button delete;
    	}
    }
}
