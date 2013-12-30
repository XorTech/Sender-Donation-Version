package com.xortech.sender;

import java.io.File;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.xortech.multipanic.PanicAddMain;
import com.xortech.extras.AppRater;
import com.xortech.multitag.TagAddMain;

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
import android.os.Bundle;
import android.preference.PreferenceManager;
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
	
	// Constants
	public static final int DIALOG_ASK_PASSWORD = 1;
	public static final String DEFAULT_CODE = "12345";	
	private static final int ZERO = 0;
	private static final int MENU_EXIT = Menu.FIRST;
	private static final int MENU_RATE = Menu.FIRST + 1;	
	private static final int MENU_TAGS = Menu.FIRST + 2;
	private static final int MENU_PANIC = Menu.FIRST + 3;
	final int RQS_GooglePlayServices = 1;
	
	private SmsReceiver mReceiver;
	int numberRuns = 0;
		
	// Instances
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sender_main);
		
		// Check status of Google Play Services
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        // Check Google Play Service Available
        try {
            if (status != ConnectionResult.SUCCESS) {
            	GooglePlayServicesUtil.getErrorDialog(status, this, RQS_GooglePlayServices).show();
            }
        } catch (Exception e) {
        	Log.e("Error: GooglePlayServiceUtil: ", "" + e);
        }
        
        // Register broadcast receiver
        IntentFilter localIntentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        localIntentFilter.setPriority(2147483647);
        mReceiver = new SmsReceiver();
        registerReceiver(mReceiver, localIntentFilter);
				
		// Load preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		try {
			// Try to launch app rater
			AppRater.app_launched(this);
		}
		catch (Exception e) {
			Log.e("Error AppRater: ", "" + e);
		}
		
		// Set up the action bar.
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
		menu.add(0, MENU_EXIT, MENU_EXIT, "Exit");
		menu.add(0, MENU_RATE, MENU_RATE, "Rate This App");				
		menu.add(0, MENU_TAGS, MENU_TAGS, "My Tags");	
		menu.add(0, MENU_PANIC, MENU_PANIC, "Panic Numbers");
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case MENU_EXIT:
	        try {
	            trimCache(this);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
			System.exit(0);
			return true;
		case MENU_RATE:
			// Change preferences to never show again
			SharedPreferences prefs = getSharedPreferences("apprater", ZERO);
			SharedPreferences.Editor editor = prefs.edit();
        	editor.putBoolean("dontshowagain", true);     
        	editor.commit();
        	
        	try {
            	// Start apprater
            	Intent rateApp = new Intent(Intent.ACTION_VIEW);
            	rateApp.setData(Uri.parse("market://details?id=com.xortech.sender"));
            	startActivity(rateApp);
        	} catch (Exception e) {
        		Log.e("Error launching apprater: ", "" + e);
        	}
			return true;
		case MENU_TAGS:
			try {
				Intent myTagIntent = new Intent(this, TagAddMain.class);
				startActivity(myTagIntent);
			} catch (Exception e) {
				Log.e("Error launghing mytags: ", "" + e);
			}
			return true;
		case MENU_PANIC:
			try {
				Intent myPanicIntent = new Intent(this, PanicAddMain.class);
				startActivity(myPanicIntent);
			} catch (Exception e) {
				Log.e("Error launghing panic: ", "" + e);
			}
			return true;
	    case android.R.id.home:
	        Intent upIntent = NavUtils.getParentActivityIntent(this);
	        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
	            TaskStackBuilder.create(this)
	                    // Add all of this activity's parents to the back stack
	                    .addNextIntentWithParentStack(upIntent)
	                    // Navigate up to the closest parent
	                    .startActivities();
	        } else {
	            // navigate up to the logical parent activity.
	            NavUtils.navigateUpTo(this, upIntent);
	        }
	        return true;
		default:
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
			//Bundle args = new Bundle();
		    switch(position){
		    case 0:
		        fragment = new SenderSend(SenderMain.this);
		        break;
		    case 1:
		        //fragment = new SenderReceive(getApplicationContext(), SenderMain.this);
		    	// add above to pass activity into fragment
		        fragment = new SenderReceive(SenderMain.this);
		        break;
		    default:
		    	break;
		    }
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
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
                        // Validate user input
                        if (strPassword1.equals(locCode)) {
                        	Intent intent = new Intent(SenderMain.this, MyPreferences.class);
                        	startActivity(intent);
                        	// Toast successful login
                        	Toast.makeText(SenderMain.this,
                        			"Login Successful!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                        	// Toast fail login
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
	
	/**
	 * Functions for handling App state
	 * */
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
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onStop();
		
	    try {
	    	trimCache(this);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	        .setTitle("Comfirm Exit")
	        .setMessage("Are you sure you want to quit Sender now?")
	        .setNegativeButton(android.R.string.no, null)
	        .setPositiveButton(android.R.string.yes, new OnClickListener() {

	            public void onClick(DialogInterface arg0, int arg1) {
	                SenderMain.super.onBackPressed();
	            }
	        }).create().show();
	}
	
    public static void trimCache(Context context) {
        try {
           File dir = context.getCacheDir();
           if (dir != null && dir.isDirectory()) {
              deleteDir(dir);
           }
        } catch (Exception e) {
           // TODO: handle exception
        }
     }

     public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
           String[] children = dir.list();
           for (int i = 0; i < children.length; i++) {
              boolean success = deleteDir(new File(dir, children[i]));
              if (!success) {
                 return false;
              }
           }
        }

        // The directory is now empty so delete it
        return dir.delete();
     }
}
