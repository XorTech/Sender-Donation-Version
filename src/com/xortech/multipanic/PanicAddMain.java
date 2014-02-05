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
package com.xortech.multipanic;

import java.util.ArrayList;

import com.xortech.database.MyPanicNumbers;
import com.xortech.database.PanicDatabaseHandler;
import com.xortech.extras.ExpandAnimation;
import com.xortech.sender.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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

public class PanicAddMain extends Activity {
	
	private static String PANIC_ID = "PanicID: ";
	private static String PANIC_PN = "Mobile: ";
	private static String PANIC_EMAIL = "Email: ";
	
	private Button addBtn;
	private ListView panicList;
	private Panic_Adapter listAdapter;
	private PanicDatabaseHandler tdb;
	
	private ArrayList<MyPanicNumbers> panic_data = new ArrayList<MyPanicNumbers>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.sender_tags);
    	    	
		// REMOVE THE TITLE FROM THE ACTIONBAR
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowTitleEnabled(false);
    	
    	panicList = (ListView) findViewById(R.id.tagList);
    	panicList.setItemsCanFocus(false);
    	addBtn = (Button) findViewById(R.id.add_btn);
    	
    	Set_Referash_Data();
  	
    	/**
    	 * LISTENER TO ADD OR UPDATE A NEW USER TO THE PANIC NUMBERS DATABASE
    	 * */
    	addBtn.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {

    			Intent addPanic = new Intent(PanicAddMain.this, PanicAddUpdate.class);
    			addPanic.putExtra("called", "add");
    			addPanic.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    			startActivity(addPanic);
    			finish();
    		}
    	});
    	
    	/**
    	 * LISTENER TO OPEN/CLOSE THE EXPANSION OF EACH CLICKED ITEM IN THE LISTVIEW
    	 */
        panicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                View toolbar = view.findViewById(R.id.expandable);

                // Creating the expand animation for the item
                ExpandAnimation expandAni = new ExpandAnimation(toolbar, 500);

                // Start the animation on the toolbar
                toolbar.startAnimation(expandAni);
            }
        });
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
	 * METHOD TO CLEAR AND FETCH DATA FROM THE PANIC DATABASE INTO AN ARRAY LIST. ONCE RECEIVED, START 
	 * THE ADAPTER AND PLACE THE DATA IN A LISTVIEW
	 * 
	 * @param NONE
	 * 
	 * */
    public void Set_Referash_Data() {
    	panic_data.clear();
    	tdb = new PanicDatabaseHandler(this);
    	ArrayList<MyPanicNumbers> tag_array_from_db = tdb.Get_Numbers();
    	    	
    	// Cycle through the database and place the new data into an ArrayList
		for (int i = 0; i < tag_array_from_db.size(); i++) {

			int tTagid = tag_array_from_db.get(i).getID();
			String tag = tag_array_from_db.get(i).getMyPanicTag();
			String mobile = tag_array_from_db.get(i).getMyPanicPhoneNumber();
			String email = tag_array_from_db.get(i).getMyPanicEmail();
			int tActive = tag_array_from_db.get(i).getPanicActive();
			int pActive = tag_array_from_db.get(i).getPanicActiveP();
			int eActive = tag_array_from_db.get(i).getPanicActiveE();
			
			MyPanicNumbers mTag = new MyPanicNumbers();
			
			mTag.setID(tTagid);
			mTag.setMyPanicTag(tag);
			mTag.setMyPanicPhoneNumber(mobile);
			mTag.setMyPanicEmail(email);
			mTag.setPanicActive(tActive);
			mTag.setPanicActiveP(pActive);
			mTag.setPanicActiveE(eActive);
						
			panic_data.add(mTag);
		}
		
		tdb.close();
				
		listAdapter = new Panic_Adapter(PanicAddMain.this, R.layout.sender_panic_item, panic_data);
		panicList.setAdapter(listAdapter);
		listAdapter.notifyDataSetChanged();

    }
    
	/**
	 * ADAPTER CLASS
	 * 
	 * */
    public class Panic_Adapter extends ArrayAdapter<MyPanicNumbers> {
    	private Activity activity;
    	private MyPanicNumbers mTag;
    	private String tagName = null;
    	private String mobileNum = null;
    	private String tagEmail = null;
    	private boolean tActive = false;
		private boolean pActive = false;
		private boolean eActive = false;
		private int layoutResourceId;
		private int activeHolder = 0;
		private UserHolder holder = null;
						
		private ArrayList<MyPanicNumbers> data = new ArrayList<MyPanicNumbers>();
		
		/**
		 * CONSTRUCTOR
		 * 
		 * @param act
		 * THE ACTIVITY PASSED FROM THE MAIN ACTIVITY
		 * @param layoutResourceId
		 * THE LAYOUT ID FOR EACH INDIVIDUAL ITEM IN THE LISTVIEW
		 * @param data
		 * AN ARRAY LIST OF DATA FROM OUR PANIC NUMBERS DATABASE
		 * 
		 */
    	public Panic_Adapter(Activity act, int layoutResourceId, ArrayList<MyPanicNumbers> data) {
    		super(act, layoutResourceId, data);
    		this.layoutResourceId = layoutResourceId;
    		this.activity = act;
    		this.data = data;
    		notifyDataSetChanged();
    	}
    	
    	/**
    	 * GET THE CURRENT SELECTION AND ITS DATA TO ESTABLISH THE LISTVIW, AND SEND DATA TO
    	 * THE APPROPRIATE METHOD FOR PROCESSING IF A BUTTON OR TOGGLE IS PRESSED
    	 * 
    	 * @param position
    	 * THE POSITION IN THE LISTVIEW STACK FOR EACH ITEM IN THE LISTVIEW
    	 * @param convertView
    	 * THE DATA ASSOCIATED WITH THE SPECIFIC ITEM IN THE LISTVIEW THAT WAS CLICKED
    	 * @param parent
    	 * 
    	 */
    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		View row = convertView;
    		
    		if (row == null) {
    			LayoutInflater inflater = LayoutInflater.from(activity);

    			row = inflater.inflate(layoutResourceId, parent, false);
    			holder = new UserHolder();    			
    			holder.tag = (TextView) row.findViewById(R.id.tag_name_txt);
    			holder.mobile = (TextView) row.findViewById(R.id.tag_phone_txt);
    			holder.email = (TextView) row.findViewById(R.id.tag_email_txt);
    			holder.bPhone = (ToggleButton) row.findViewById(R.id.phoneToggle);
        		holder.bEmail = (ToggleButton) row.findViewById(R.id.emailToggle);
    			holder.bEdit = (Button) row.findViewById(R.id.btnUpdate);
    			holder.bDelete = (Button) row.findViewById(R.id.btnDelete);
    			holder.bExpand = (ToggleButton) row.findViewById(R.id.expandBtn);
     		        		
    			row.setTag(holder);
    		} else {
    			holder = (UserHolder) row.getTag();
    		}
    		
    		mTag = data.get(position);
    		
    		holder.bEdit.setTag(mTag.getID());
    		holder.bDelete.setTag(mTag.getID());
    		    		
    		if (mTag.getPanicActive() == 1) {
    			tActive = true;
    			
	    		holder.bEmail.setVisibility(View.VISIBLE);
	    		holder.bPhone.setVisibility(View.VISIBLE);
    		} else {
    			tActive = false;
    			
	    		holder.bEmail.setVisibility(View.GONE);
	    		holder.bPhone.setVisibility(View.GONE);
    		} 
    		holder.bExpand.setChecked(tActive);

    		if (mTag.getPanicActiveP() == 1) {
    			pActive = true;
    		} else {
    			pActive = false;
    		}
    		holder.bPhone.setChecked(pActive);
    		  		
    		if (mTag.getPanicActiveE() == 1) {
    			eActive = true;
    		} else {
    			eActive = false;
    		}
    		holder.bEmail.setChecked(eActive);
    		    		
    		tagName = (mTag.getMyPanicTag());
    		holder.tag.setText(PANIC_ID + tagName);
    		
    		mobileNum = (mTag.getMyPanicPhoneNumber());
    		holder.mobile.setText(PANIC_PN + mobileNum);

    		tagEmail = (mTag.getMyPanicEmail());
    		
    		if (tagEmail == null || tagEmail.equals("")) {
    			holder.email.setText(PANIC_EMAIL + "Feature Disabled");
    			holder.bEmail.setChecked(false);
    		} else {
    			holder.email.setText(PANIC_EMAIL + tagEmail);
    		}
    		
    		if (mobileNum == null || mobileNum.equals("")) {
    			holder.mobile.setText(PANIC_PN + "Feature Disabled");
    			holder.bPhone.setChecked(false);
    		} else {
    			holder.mobile.setText(PANIC_PN + mobileNum);
    		}
    		
    		/**
    		 * LISTENER FOR THE ACTIVE/INACTIVE STATUS OF THE PHONE NUMBER TOGGLE LOCATED IN THE SUBMENU
    		 * OF A PARTICULAR PANIC NUMBER. IF DISABLED, THE USER WILL NOT SEND SMS NOTIFICATIONS
    		 * TO THIS NUMBER
    		 * 
    		 */     		
			holder.bPhone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					
					if (isChecked) {   		    			
		    			pActive = true;
		    			activeHolder = 1;
		    		} else {
		    			pActive = false;
		    			activeHolder = 0;
		    		}
		    		UpdatePanicDB(position, activeHolder, 2);				
				}   			
    		});
    		
    		/**
    		 * LISTENER FOR THE ACTIVE/INACTIVE STATUS OF THE EMAIL TOGGLE LOCATED IN THE SUBMENU
    		 * OF A PARTICULAR PANIC NUMBER. IF DISABLED, THE USER WILL NOT SEND EMAIL NOTIFICATIONS
    		 * TO THIS NUMBER
    		 * 
    		 */    		
    		holder.bEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    		if (isChecked) {  
		    			eActive = true;
		    			activeHolder = 1;
		    		} else {
		    			eActive = false;
		    			activeHolder = 0;
		    		}
	    			UpdatePanicDB(position, activeHolder, 3);		
				}
			});
    		
    		/**
    		 * LISTENER FOR THE ACTIVE/INACTIVE STATUS OF THE PANIC NUMBER LOCATED IN THE LISTVIEW.
    		 * IF DISABLED, THE USER WILL NOT SEND EMAILS OR SMS NOTIFICATIONS TO THIS NUMBER
    		 * 
    		 */
    		holder.bExpand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					
					if (isChecked) {  
		    			tActive = true;
		    			activeHolder = 1;
		    			holder.bEmail.setVisibility(View.VISIBLE);
		    			holder.bPhone.setVisibility(View.VISIBLE);
		    		} else {
		    			tActive = false;
		    			activeHolder = 0;
		    			holder.bEmail.setVisibility(View.GONE);
		    			holder.bPhone.setVisibility(View.GONE);
		    		}
	    			UpdatePanicDB(position, activeHolder, 1);							
				}
				
			});
    		
    		/**
    		 * LISTENER FOR THE EDIT BUTTON. IF PRESSED, THE USER WILL BE BROUGHT TO THE PANIC
    		 * ADD/UPDATE MAIN ACTIVITY, WHICH WILL ALLOW THE USER MAKE EDITS TO THE CURRENT SELECTED
    		 * PANIC NUMBER
    		 * 
    		 */
    		holder.bEdit.setOnClickListener(new OnClickListener() {

    			@Override
    			public void onClick(View v) {

    				Intent update_user = new Intent(activity, PanicAddUpdate.class);
    				update_user.putExtra("called", "update");
    				update_user.putExtra("USER_ID", v.getTag().toString());
    				activity.startActivity(update_user);
    			}
    			
    		});
    		
    		/**
    		 * LISTENER FOR THE DELETE BUTTON. IF PRESSED, GET THE CURRENT POSITION AND CONFIRM
    		 * WITH THE USER TO DELETE THE ITEM FROM THE LIST.
    		 * 
    		 */
    		holder.bDelete.setOnClickListener(new OnClickListener() {

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

    						PanicDatabaseHandler dBHandler = new PanicDatabaseHandler(getBaseContext());
    						dBHandler.Delete_Number(tag_id);
    						PanicAddMain.this.onResume();   						
    					}
    					
    				});
    				adb.show();
    			}
    			
    		});
    		return row;   		
    	}
    	
    	/**
    	 * METHOD TO UPDATE THE PANIC NUMBERS DATABASE BASED ON THE USER INTERACTION WITH THE 
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
    	public void UpdatePanicDB(int position, int value, int section) {
    		 		
			MyPanicNumbers updateNumbers = new MyPanicNumbers();
			PanicDatabaseHandler dBHandlerE = new PanicDatabaseHandler(getBaseContext());
			
    		updateNumbers = data.get(position);
    		    		
    		switch (section) {
    		case 1:
    			updateNumbers.setPanicActive(value);
    			break;
    		case 2:
    			updateNumbers.setPanicActiveP(value);
    			break;
    		case 3:
    			updateNumbers.setPanicActiveE(value);
    			break;
    		}
    		
			updateNumbers = new MyPanicNumbers(updateNumbers.getID(), updateNumbers.getMyPanicTag(), 
					updateNumbers.getMyPanicPhoneNumber(), updateNumbers.getMyPanicEmail(), updateNumbers.getPanicActive(), 
					updateNumbers.getPanicActiveP(), updateNumbers.getPanicActiveE());

			dBHandlerE.Update_Number(updateNumbers);
			dBHandlerE.close();
			
			notifyDataSetChanged();
					
    	}
    	
    	/**
    	 * HOLDER CLASS FOR LISTVIEW
    	 * 
    	 */
    	class UserHolder {
    		TextView tag;
    		TextView mobile;
    		TextView email;   		
    		Button bEdit;
    		Button bDelete;
    		ToggleButton bPhone;
    		ToggleButton bEmail;    
    		ToggleButton bExpand;
    	}
    }
}
