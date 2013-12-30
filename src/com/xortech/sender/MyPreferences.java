package com.xortech.sender;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class MyPreferences extends PreferenceActivity {
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle paramBundle)
	{
		super.onCreate(paramBundle);		
		addPreferencesFromResource(R.xml.preferences);
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
}
