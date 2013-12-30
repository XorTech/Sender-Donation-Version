package com.xortech.multipanic;

import java.util.ArrayList;

import com.xortech.database.MyPanicNumbers;
import com.xortech.database.PanicDatabaseHandler;
import com.xortech.sender.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PanicAddMain extends Activity {
	public static String PANIC_ID = "PanicID: ";
	public static String PANIC_PN = "Mobile: ";
	
    Button addBtn;
    ListView panicList;
    ArrayList<MyPanicNumbers> panic_data = new ArrayList<MyPanicNumbers>();
    Panic_Adapter pAdapter;
    PanicDatabaseHandler tdb;
    String toastMsg;
    
	/**
	 * PanicAddMain OnCreate
	 * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.sender_tags);
    	
        // Add up button functionality to send user back to home
        getActionBar().setDisplayHomeAsUpEnabled(true);
    	
    	// Try to get data
    	try {
    		panicList = (ListView) findViewById(R.id.tagList);
    		panicList.setItemsCanFocus(false);
    		addBtn = (Button) findViewById(R.id.add_btn);

    		Set_Referash_Data();

    	} 
    	catch (Exception e) {
    		Log.e("some error", "" + e);
    	}
    	
    	/**
    	 * Listener for the add new panic number button
    	 * */
    	addBtn.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			// Start PanicAddUpdate intent
    			Intent addPanic = new Intent(PanicAddMain.this, PanicAddUpdate.class);
    			addPanic.putExtra("called", "add");
    			addPanic.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    			startActivity(addPanic);
    			finish();
    		}
    	});
    }
    
	/**
	 * Function to check database for panic numbers and display
	 * */
    public void Set_Referash_Data() {
    	panic_data.clear();
    	tdb = new PanicDatabaseHandler(this);
    	ArrayList<MyPanicNumbers> tag_array_from_db = tdb.Get_Numbers();
    	
    	// Get numbers from database
		for (int i = 0; i < tag_array_from_db.size(); i++) {

			int tTagid = tag_array_from_db.get(i).getID();
			String tag = tag_array_from_db.get(i).getMyPanicTag();
			String mobile = tag_array_from_db.get(i).getMyPanicPhoneNumber();
			
			MyPanicNumbers mTag = new MyPanicNumbers();
			mTag.setID(tTagid);
			mTag.setMyPanicTag(tag);
			mTag.setMyPanicPhoneNumber(mobile);

			panic_data.add(mTag);
		}
		tdb.close();
		pAdapter = new Panic_Adapter(PanicAddMain.this, R.layout.sender_display_panic, panic_data);
		panicList.setAdapter(pAdapter);
		pAdapter.notifyDataSetChanged();
    }
    
	/**
	 * Function for showing toast messages
	 * */
    public void Show_Toast(String msg) {
    	Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    
	/**
	 * On Resume Function
	 * */
    @Override
    public void onResume() {
    	super.onResume();
    	try {
    		Set_Referash_Data();
    	}
    	catch (Exception e) {
    		Log.e("Resume Error", "" + e);
    	}
    }
    
	/**
	 * Function to listen for options item selected
	 * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        switch(item.getItemId()){
        case android.R.id.home:
            onBackPressed();
        }
             
        return true;
    }
    
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
    }
    
	/**
	 * Class to handle viewing active numbers
	 * */
    public class Panic_Adapter extends ArrayAdapter<MyPanicNumbers> {
    	Activity activity;
    	int layoutResourceId;
    	MyPanicNumbers mTag;
    	ArrayList<MyPanicNumbers> data = new ArrayList<MyPanicNumbers>();

    	public Panic_Adapter(Activity act, int layoutResourceId, ArrayList<MyPanicNumbers> data) {
    		super(act, layoutResourceId, data);
    		this.layoutResourceId = layoutResourceId;
    		this.activity = act;
    		this.data = data;
    		notifyDataSetChanged();
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		View row = convertView;
    		UserHolder holder = null;

    		if (row == null) {
    			LayoutInflater inflater = LayoutInflater.from(activity);

    			row = inflater.inflate(layoutResourceId, parent, false);
    			holder = new UserHolder();
    			holder.tag = (TextView) row.findViewById(R.id.tag_name_txt);
    			holder.mobile = (TextView) row.findViewById(R.id.tag_phone_txt);
    			holder.edit = (Button) row.findViewById(R.id.btnUpdate);
    			holder.delete = (Button) row.findViewById(R.id.btnDelete);
    			row.setTag(holder);
    		} 
    		else {
    			holder = (UserHolder) row.getTag();
    		}
    		
    		mTag = data.get(position);
    		holder.edit.setTag(mTag.getID());
    		holder.delete.setTag(mTag.getID());
    		String tagName = (mTag.getMyPanicTag());
    		holder.tag.setText(PANIC_ID + tagName);
    		String mobileNum = (mTag.getMyPanicPhoneNumber());
    		holder.mobile.setText(PANIC_PN + mobileNum);

    		holder.edit.setOnClickListener(new OnClickListener() {

    			@Override
    			public void onClick(View v) {
    				Log.i("Edit Button Clicked", "**********");

    				Intent update_user = new Intent(activity, PanicAddUpdate.class);
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

    	class UserHolder {
    		TextView tag;
    		TextView mobile;
    		Button edit;
    		Button delete;
    	}
    }
}
