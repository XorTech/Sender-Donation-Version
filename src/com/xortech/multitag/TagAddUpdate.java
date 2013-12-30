package com.xortech.multitag;

import com.xortech.database.MyTags;
import com.xortech.database.TagDatabaseHandler;
import com.xortech.sender.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TagAddUpdate extends Activity {
    EditText add_tag, add_mobile, add_secret;
    Button add_save_btn, add_view_all, update_btn, update_view_all;
    LinearLayout add_view, update_view;
    String valid_mob_number = null, valid_secret = null, valid_tag = null,
	    toastMsg = null, valid_user_id = "";
    int USER_ID;
    MyTags newTag = null;
    TagDatabaseHandler dbHandler = new TagDatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.sender_add_update);
    	
        // Add up button functionality to send user back to home
        getActionBar().setDisplayHomeAsUpEnabled(true);

    	// set screen
    	Set_Add_Update_Screen();

    	// set visibility of view as per calling activity
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
    		// dbHandler.close();
    	}
    	
    	add_mobile.addTextChangedListener(new TextWatcher() {

    		@Override
    		public void onTextChanged(CharSequence s, int start, int before, int count) {
    			// TODO: 
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
    			// TODO: 
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
    			// TODO: 
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
	    	
    				newTag = new MyTags(valid_tag, valid_mob_number, valid_secret);
	    		

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

    			// check the value state is null or not
    			if (valid_tag != null && valid_mob_number != null
	    			&& valid_secret != null && valid_tag.length() != 0
	    			&& valid_mob_number.length() != 0
	    			&& valid_secret.length() != 0) {
	    		
    				newTag = new MyTags(USER_ID, valid_tag, valid_mob_number, valid_secret);
	    		    		
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
    
	/**
	 * Function to handle the display
	 * */
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
	 * Function to validate the phone number
	 * */
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
	 * Function to validate the secret code
	 * */
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
	 * Function to validate tag name
	 * */
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
	 * Function to handle toast messages
	 * */
    public void Show_Toast(String msg) {
    	Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    
	/**
	 * Function to reset text on display
	 * */
    public void ResetText() {
    	add_tag.getText().clear();
    	add_mobile.getText().clear();
    	add_secret.getText().clear();
    }
    
	/**
	 * Function reset errors on display
	 * */
    public void ResetError() {
    	add_tag.setError(null);
    	add_mobile.setError(null);
    	add_secret.setError(null);
    }
    
	/**
	 * Function handle animation from invalid user input
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
    
	/**
	 * Function to override the back button
	 * */
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
    }
    
	/**
	 * Function to return to TagAddMain
	 * */
    public void ReturnToMain() {
    	Intent view_user = new Intent(TagAddUpdate.this, TagAddMain.class);
		view_user.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(view_user);
		finish();
    }
}
