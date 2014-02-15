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

import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mapswithme.maps.api.DownloadMapsWithMeDialog;
import com.mapswithme.maps.api.MapsWithMeApi;
import com.xortech.extras.AppRater;
import com.xortech.multipanic.PanicAddMain;
import com.xortech.multitag.TagAddMain;
import com.xortech.sender.R;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class SenderMain extends FragmentActivity implements ActionBar.TabListener {

	private static final int DIALOG_ASK_PASSWORD = 1;
	private static final String DEFAULT_CODE = "12345";	
	private static final String GMAPS = "1";
	private static final String MWM = "2";		
	private static final int RQS_GooglePlayServices = 1;
	
	private SmsReceiver mReceiver;	
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private SharedPreferences preferences;
	private String mapType = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sender_main);
		
		// REMOVE THE TITLE FROM THE ACTIONBAR
		getActionBar().setDisplayShowTitleEnabled(false);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		mapType = preferences.getString("mapType", GMAPS);
		
		// CHECK FOR "DON'T KEEP ACTIVITIES" IN DEVELOPER OPTIONS
		boolean checkDeveloper = isAlwaysFinishActivitiesOptionEnabled();
		if (checkDeveloper) {
			showDeveloperOptionsScreen();
		}
		
		/*
		 * IF GOOGLE MAPS IS SELECTED AS THE DEFAULT, THEN CHECK TO
		 * SEE IF THE USER HAS THE CORRECT VERSION OF GOOGLE PLAY SERVICES
		 */
		if (mapType.equals(GMAPS)) {
			// GET GOOGLE PLAY STATUS
	        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	        
	        // CHECK IF GOOGLE PLAY SERVICE IS AVAILABLE 
	        try {
	            if (status != ConnectionResult.SUCCESS) {
	            	GooglePlayServicesUtil.getErrorDialog(status, this, RQS_GooglePlayServices).show();
	            }
	        } catch (Exception e) {
	        	Log.e("Error: GooglePlayServiceUtil: ", "" + e);
	        }
		} 
     
        // REGISTER A BROADCAST RECEIVER
        IntentFilter localIntentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        localIntentFilter.setPriority(2147483646);
        mReceiver = new SmsReceiver();
        registerReceiver(mReceiver, localIntentFilter);
			
		try {
			// TRY TO LAUNCH APP RATER 
			AppRater.app_launched(this);
		}
		catch (Exception e) {
			Log.e("Error AppRater: ", "" + e);
		}
		
		// SET UP THE ACTION BAR
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {

			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sender_main, menu);

		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Handle menu options item selected
	 * */
	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.quit:
	        MyUpdateReceiver.trimCache(this);
			System.exit(0);
			return true;
		case R.id.rateApp:
			// CHANGE PREFERENCES TO NEVER SHOW AGAIN
			SharedPreferences prefs = getSharedPreferences("apprater", 0);
			SharedPreferences.Editor editor = prefs.edit();
        	editor.putBoolean("dontshowagain", true);     
        	editor.commit();
        	try {
            	Intent rateApp = new Intent(Intent.ACTION_VIEW);
            	rateApp.setData(Uri.parse("market://details?id=com.xortech.sender"));
            	startActivity(rateApp);
        	} catch (Exception e) {
        		Log.e("Error launching apprater: ", "" + e);
        	}
			return true;
		case R.id.tagsApp:
			try {
				Intent myTagIntent = new Intent(this, TagAddMain.class);
				startActivity(myTagIntent);
			} catch (Exception e) {
				Log.e("Error launching mytags: ", "" + e);
			}
			return true;
		case R.id.panicApp:
			try {
				Intent myPanicIntent = new Intent(this, PanicAddMain.class);
				startActivity(myPanicIntent);
			} catch (Exception e) {
				Log.e("Error launching panic: ", "" + e);
			}
			return true;
		case R.id.donateApp:
			try {
        		Intent upIntent = new Intent();
        		upIntent.setAction(Intent.ACTION_VIEW);
        		upIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        		upIntent.setData(Uri.parse("https://coinbase.com/checkouts/4bc224c6764e7908bea274c12badce5e"));
        		startActivity(upIntent); 
			} catch (Exception e) {
				Log.e("Error launching donate: ", "" + e);
			}
			return true;
	    case android.R.id.home:
	        Intent upIntent = NavUtils.getParentActivityIntent(this);
	        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
	            TaskStackBuilder.create(this)
	                    // ADD ALL THE PARENTS TO THE BACKSTACK
	                    .addNextIntentWithParentStack(upIntent)
	                    // NAVIGATE UP TO THE CLOSEST PARENT
	                    .startActivities();
	        } else {
	            // NAVIGATE UP TO THE LOGICAL PARENT ACTIVITY
	            NavUtils.navigateUpTo(this, upIntent);
	        }
	        return true;
	    case R.id.action_settings:
			showDialog(DIALOG_ASK_PASSWORD);
		}
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
		    Fragment fragment = null;
		    
		    switch(position){
		    case 0:
		        fragment = new SenderSend(SenderMain.this);
		        break;
		    case 1:
		        fragment = new SenderReceive(SenderMain.this);
		        break;
		    default:
		    	break;
		    }
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}
	
	/**
	 * METHOD TO HANDLE THE SENDER DIALOG BOXES
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {		
			case DIALOG_ASK_PASSWORD:
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout = inflater.inflate(R.layout.sender_login, (ViewGroup) findViewById(R.id.root));
                final EditText password1 = (EditText) layout.findViewById(R.id.EditText_Pwd1);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Secure Area");
                builder.setMessage("Please enter your passcode to verify your identity.");
                builder.setView(layout);

                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@SuppressWarnings("deprecation")
					public void onClick(DialogInterface dialog, int whichButton) {
                        removeDialog(DIALOG_ASK_PASSWORD);
                    }
                });
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@SuppressWarnings("deprecation")
					public void onClick(DialogInterface dialog, int which) {
                        String strPassword1 = password1.getText().toString();           
                        String locCode = preferences.getString("lockCode", DEFAULT_CODE);
                        // VALIDATE THE USER INPUT
                        if (strPassword1.equals(locCode)) {
                        	Intent intent = new Intent(SenderMain.this, MyPreferences.class);
                        	startActivity(intent);
                        	// TOAST A SUCCESSFUL LOGIN
                        	Toast.makeText(SenderMain.this,
                        			"Login Successful!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                        	// TOAST FAIL LOGIN
                        	Toast.makeText(SenderMain.this,
                        			"Login Failed", Toast.LENGTH_SHORT).show();
                        }
                        removeDialog(DIALOG_ASK_PASSWORD);
                    }
                });
                AlertDialog passwordDialog = builder.create();
                return passwordDialog;
		}
		return null;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		mapType = preferences.getString("mapType", GMAPS);
		
		if (mapType.equals(MWM) & !MapsWithMeApi.isMapsWithMeInstalled(this)) {
			DownloadMapsWithMeDialog mdialog = new DownloadMapsWithMeDialog(this);
			mdialog.show();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();	
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {	    
	    // Call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		MyUpdateReceiver.trimCache(this);
		super.onStop();
	}
	
	@Override
	public void onBackPressed() {
		
		MyUpdateReceiver.trimCache(this);
		
	    new AlertDialog.Builder(this)
	        .setTitle("Comfirm Exit")
	        .setMessage("Are you sure you want to quit Sender now?")
	        .setNegativeButton(android.R.string.no, null)
	        .setPositiveButton(android.R.string.yes, new OnClickListener() {

	            public void onClick(DialogInterface arg0, int arg1) {
	                SenderMain.super.onBackPressed();
	                finish();
	            }
	        }).create().show();
	}
	
	/**
	 * METHOD TO CHECK IF "DON'T KEEP ACTIVITIES" IS CHECKED IN DEVELOPER OPTIONS
	 * @return
	 */
	public boolean isAlwaysFinishActivitiesOptionEnabled() {
		int alwaysFinishActivitiesInt = 0;
	    if (Build.VERSION.SDK_INT >= 17) {
	    	alwaysFinishActivitiesInt = Settings.System.getInt(getApplicationContext().getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);
	    } else {
	        alwaysFinishActivitiesInt = Settings.System.getInt(getApplicationContext().getContentResolver(), Settings.System.ALWAYS_FINISH_ACTIVITIES, 0);
	    }

	    if (alwaysFinishActivitiesInt == 1) {
	        return true;
	    } else {
	        return false;
	    }
	}
	
	/**
	 * METHOD TO ASK USER TO DISABLE "DON'T KEEP ACTIVITIES" IN DEVELOPER OPTIONS
	 */
	public void showDeveloperOptionsScreen(){
		
	    new AlertDialog.Builder(this)
        .setTitle("Developer Options Detected!")
        .setMessage("In order for Sender to work properly, please uncheck the \"Don't keep activities\" option.")
        .setNegativeButton(android.R.string.no, null)
        .setPositiveButton(android.R.string.yes, new OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
        	    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        	    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        	    startActivity(intent);
        	    finish();
            }
        }).create().show();
	}
}
