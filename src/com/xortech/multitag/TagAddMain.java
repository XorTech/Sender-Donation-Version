package com.xortech.multitag;

import java.util.ArrayList;

import com.xortech.database.MyTags;
import com.xortech.database.TagDatabaseHandler;
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

public class TagAddMain extends Activity {
	public static String TAG_ID = "TagID: ";
	public static String TAG_PN = "Mobile: ";
	public static String TAG_SC = "Secret: ";
	
    Button addBtn;
    ListView tagList;
    ArrayList<MyTags> tag_data = new ArrayList<MyTags>();
    Tag_Adapter tAdapter;
    TagDatabaseHandler tdb;
    String toastMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.sender_tags);
    	
        // Add up button functionality to send user back to home
        getActionBar().setDisplayHomeAsUpEnabled(true);
    	
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
    }

    public void Set_Referash_Data() {
    	tag_data.clear();
    	tdb = new TagDatabaseHandler(this);
    	ArrayList<MyTags> tag_array_from_db = tdb.Get_Tags();

		for (int i = 0; i < tag_array_from_db.size(); i++) {

			int tTagid = tag_array_from_db.get(i).getID();
			String tag = tag_array_from_db.get(i).getMyTag();
			String mobile = tag_array_from_db.get(i).getMyTagPhoneNumber();
			String secret = tag_array_from_db.get(i).getTagSecret();
			
			MyTags mTag = new MyTags();
			mTag.setID(tTagid);
			mTag.setMyTag(tag);
			mTag.setTagSecret(secret);
			mTag.setMyTagPhoneNumber(mobile);

			tag_data.add(mTag);
		}
		tdb.close();
		tAdapter = new Tag_Adapter(TagAddMain.this, R.layout.sender_display_tag, tag_data);
		tagList.setAdapter(tAdapter);
		tAdapter.notifyDataSetChanged();
    }

    public void Show_Toast(String msg) {
    	Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

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

    public class Tag_Adapter extends ArrayAdapter<MyTags> {
    	Activity activity;
    	int layoutResourceId;
    	MyTags mTag;
    	ArrayList<MyTags> data = new ArrayList<MyTags>();

    	public Tag_Adapter(Activity act, int layoutResourceId, ArrayList<MyTags> data) {
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
    			holder.secret = (TextView) row.findViewById(R.id.tag_secret_txt);
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
    		String tagName = (mTag.getMyTag());
    		holder.tag.setText(TAG_ID + tagName);
    		String mobileNum = (mTag.getMyTagPhoneNumber());
    		holder.mobile.setText(TAG_PN + mobileNum);
    		String secretCode = (mTag.getTagSecret());
    		holder.secret.setText(TAG_SC + secretCode);

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

    	class UserHolder {
    		TextView tag;
    		TextView mobile;
    		TextView secret;
    		Button edit;
    		Button delete;
    	}
    }
}
