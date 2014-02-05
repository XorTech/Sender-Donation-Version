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

import com.xortech.database.MyTags;
import com.xortech.database.TagDatabaseHandler;
import com.xortech.sender.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TagAddUpdate extends Activity {
	
	private static final int REQUEST_CODE_PICK_CONTACTS = 1;
	
	private EditText add_tag, add_mobile, add_secret;
	private Button add_save_btn, add_view_all, update_btn, update_view_all;
	private LinearLayout add_view, update_view;
	private String valid_mob_number = null, valid_secret = null, valid_tag = null,
	    toastMsg = null;
	private int USER_ID, active;
	private MyTags newTag = null;
    private Uri uriContact;
    private String contactID;
    private String contactName;
    private String contactNumber;
	
	private TagDatabaseHandler dbHandler = new TagDatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.sender_add_update);
    	    	
		// REMOVE THE TITLE FROM THE ACTIONBAR
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowTitleEnabled(false);

    	// SET UP THE SCREEN
    	Set_Add_Update_Screen();

    	// SET THE VISIBILITY
    	String called_from = getIntent().getStringExtra("called");

    	if (called_from.equalsIgnoreCase("add")) {
    		add_view.setVisibility(View.VISIBLE);
    		update_view.setVisibility(View.GONE);
    	} 
    	else {
    		update_view.setVisibility(View.VISIBLE);
    		add_view.setVisibility(View.GONE);
    		USER_ID = Integer.parseInt(getIntent().getStringExtra("USER_ID"));

    		MyTags c = dbHandler.Get_Tag(USER_ID);

    		add_tag.setText(c.getMyTag());
    		add_mobile.setText(c.getMyTagPhoneNumber());
    		add_secret.setText(c.getTagSecret());
    		active = c.getTagStatus();
    		
    		dbHandler.close();
    	}
    	
    	add_mobile.addTextChangedListener(new TextWatcher() {

    		@Override
    		public void onTextChanged(CharSequence s, int start, int before, int count) {
    			// TODO: Auto-generated method stub
    		}

    		@Override
    		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    			// TODO Auto-generated method stub
    		}

    		@Override
    		public void afterTextChanged(Editable s) {
    			Is_Valid_Sign_Number_Validation(6, 16, add_mobile);
    		}
    	});
    	
    	add_secret.addTextChangedListener(new TextWatcher() {

    		@Override
    		public void onTextChanged(CharSequence s, int start, int before, int count) {
    			// TODO: Auto-generated method stub
    		}

    		@Override
    		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    			// TODO Auto-generated method stub
    		}

    		@Override
    		public void afterTextChanged(Editable s) {
    			Is_Valid_Secret(add_secret);
    		}
    	});

    	add_tag.addTextChangedListener(new TextWatcher() {

    		@Override
    		public void onTextChanged(CharSequence s, int start, int before,int count) {
    			// TODO Auto-generated method stub 
    		}

    		@Override
    		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    			// TODO Auto-generated method stub
    		}

    		@Override
    		public void afterTextChanged(Editable s) {
    			// TODO Auto-generated method stub
    			Is_Valid_Tag_Name(add_tag);
    		}
    	});

    	add_save_btn.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			if (valid_tag != null && valid_mob_number != null
	    			&& valid_secret != null && valid_tag.length() != 0
	    			&& valid_mob_number.length() != 0
	    			&& valid_secret.length() != 0) {
	    	
    				newTag = new MyTags(valid_tag, valid_mob_number, valid_secret, 1);
	    		
    				dbHandler.Add_Tag(newTag);
    				toastMsg = "Tag Added";
    				Show_Toast(toastMsg);
    				ResetText();
    				ResetError();
    				ReturnToMain();
    			}
    			else {
    				VibrateError(add_tag, add_mobile, add_secret);	    		
    				toastMsg = "Invalid Input!";
    				Show_Toast(toastMsg);
    				ResetText();
    				ResetError();
    			}
    		}
    	});

    	update_btn.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {

    			valid_tag = add_tag.getText().toString();
    			valid_mob_number = add_mobile.getText().toString();
    			valid_secret = add_secret.getText().toString();

    			// CHECK IF THE VALUE STATE IS NULL OR NOT
    			if (valid_tag != null && valid_mob_number != null
	    			&& valid_secret != null && valid_tag.length() != 0
	    			&& valid_mob_number.length() != 0
	    			&& valid_secret.length() != 0) {
	    		
    				newTag = new MyTags(USER_ID, valid_tag, valid_mob_number, valid_secret, active);
	    		  				    		
    				dbHandler.Update_Tag(newTag);
    				dbHandler.close();
    				toastMsg = "Tag Update Successful";
    				Show_Toast(toastMsg);
    				ResetText();
    				ResetError();
    				ReturnToMain();
    			} 
    			else {
    				VibrateError(add_tag, add_mobile, add_secret);
    				toastMsg = "Invalid input detected.\nPlease fill in all fields.";
    				Show_Toast(toastMsg);
    				ResetError();
    			}
    		}
    	});
	
    	update_view_all.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			ReturnToMain();
    		}
    	});

    	add_view_all.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			ReturnToMain();
    		}
    	});
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contact_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        switch(item.getItemId()){
        case R.id.quitView:
        	finish();
        	return true;
        case R.id.contactBtn:
        	OnClickSelectContact();
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
    }
    
    /**
     * METHOD TO HANDLE THE SCREEN LAYOUT AND VIEW
     */
    public void Set_Add_Update_Screen() {

    	add_tag = (EditText) findViewById(R.id.add_tag);
    	add_mobile = (EditText) findViewById(R.id.add_mobile);
    	add_secret = (EditText) findViewById(R.id.add_secret);

    	add_save_btn = (Button) findViewById(R.id.add_save_btn);
    	update_btn = (Button) findViewById(R.id.update_btn);
    	add_view_all = (Button) findViewById(R.id.add_view_all);
    	update_view_all = (Button) findViewById(R.id.update_view_all);

    	add_view = (LinearLayout) findViewById(R.id.add_view);
    	update_view = (LinearLayout) findViewById(R.id.update_view);

    	add_view.setVisibility(View.GONE);
    	update_view.setVisibility(View.GONE);

    }
    
    /**
     * METHOD TO VALIDATE THE PHONE NUMBER
     * @param MinLen
     * @param MaxLen
     * @param edt
     * @throws NumberFormatException
     */
    public void Is_Valid_Sign_Number_Validation(int MinLen, int MaxLen,
	    EditText edt) throws NumberFormatException {
    	
    	if (edt.getText().toString().length() <= 0) {
    		edt.setError("Invalid Number"); 		
    		valid_mob_number = null;
    	} 
    	else if (edt.getText().toString().length() < MinLen) {
    		edt.setError("Minimum Length " + MinLen);
    		valid_mob_number = null;
    	} 
    	else if (edt.getText().toString().length() > MaxLen) {
    		edt.setError("Maximum Length " + MaxLen);
    		valid_mob_number = null;
    	} 
    	else {
    		edt.setError(null);
    		valid_mob_number = edt.getText().toString();
    	}
    } 
    
    /**
     * METHOD TO VALIDATE THE SECRET
     * @param edt
     */
    public void Is_Valid_Secret(EditText edt) {

    	if (edt.getText().toString().length() <= 0) {
    		edt.setError("Invalid Secret");
    	    valid_secret = null;
    	} 
    	else {
    		edt.setError(null);
    	    valid_secret = edt.getText().toString();
    	}
    }
   
    /**
     * METHOD TO VALIDATE THE TAG NAME
     * @param edt
     */
    public void Is_Valid_Tag_Name(EditText edt) {
    	
    	if (edt.getText().toString().length() <= 0) {
    		edt.setError("Invalid Tag");
    	    valid_tag = null;
    	} 
    	else {
    		edt.setError(null);
    		valid_tag = edt.getText().toString();
    	}  
    }
    
    /**
     * METHOD TO HANDLE TOAST MESSAGES
     * @param msg
     */
    public void Show_Toast(String msg) {
    	Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    
    /**
     * METHOD TO RESET TEXT FIELDS IN THE FORM 
     */
    public void ResetText() {
    	add_tag.getText().clear();
    	add_mobile.getText().clear();
    	add_secret.getText().clear();
    }
    
    /**
     * METHOD TO RESET THE ERROR MESSAGES
     */
    public void ResetError() {
    	add_tag.setError(null);
    	add_mobile.setError(null);
    	add_secret.setError(null);
    }
    
	/**
	 * METHOD TO HANDLE INVALID USER INPUTS - SHAKE AND VIBRATE
	 * */
    public void VibrateError(EditText mEditText, EditText nEditText, EditText oEditText) {
    	Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    	v.vibrate(500);
    	Animation shake = AnimationUtils.loadAnimation(getBaseContext(), R.anim.shake);
    	mEditText.startAnimation(shake);
    	nEditText.startAnimation(shake);
    	oEditText.startAnimation(shake);
    }
    
	/**
	 * METHOD TO RETURN TO THE MAIN TAG SCREEN
	 * */
    public void ReturnToMain() {
    	Intent view_user = new Intent(TagAddUpdate.this, TagAddMain.class);
		view_user.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(view_user);
		finish();
    }
    
    /**
     * METHOD TO ALLOW A USER TO IMPORT THE NAME AND NUMBER FROM THE CONTACT LIST
     */
    public void OnClickSelectContact() { 	 
        // PICK A CONTACT FROM THE CONTACT LIST
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            uriContact = data.getData();
 
            RetrieveContactName();
            RetrieveContactNumber();
            
            if (contactName != null) {
                add_tag.setText(contactName);
            } 
            if (contactNumber != null) {
            	contactNumber = contactNumber.replaceAll("[\\D]", "");
                add_mobile.setText(contactNumber);
            }
        }
    }
    
    /**
     * METHOD TO RETRIEVE THE CONTACT'S PHONE NUMBER
     */
    private void RetrieveContactNumber() {
  
        // GET THE CONTACTS ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);
 
        if (cursorID.moveToFirst()) {
 
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }
 
        cursorID.close();
  
        // USING THE CONTACT ID TO GET THE PHONE NUMBER
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
 
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
 
                new String[]{contactID},
                null);
 
        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }       
        cursorPhone.close(); 
    }
    
    /**
     * METHOD TO RETRIEVE THE CONTACT'S NAME
     */
    private void RetrieveContactName() {
  
        // QUERYING CONTACT DATA STORE
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
 
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));   
        }
        cursor.close();  
    }
}
