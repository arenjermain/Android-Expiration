package osse.android.moldhold;

// Copyright (c) 2010 Michelle Carter, Sarah Cathey, Aren Edlund-Jermain
// See COPYING file for license details. 

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;



// Application displays two buttons, scan and update (currently a stub). 
// Clicking the "scan" button invokes the zxing application. 
//
// This application uses SharedPreferences which contains the following keys:
//		"AUTH_TOKEN"
//		"GSESSION_ID"
//		"ACCOUNT_NAME"
//		"CALENDAR_ID"
//
public class mainActivity extends Activity implements OnClickListener {
	private static final String 	TAG = "MoldHold";
	private static final int		REQUEST_SETUP = 0;
	private static final int		REQUEST_ZXING = 1;

	private Button					btnScan;
	private Button					btnUpdate;
	private Button					btnQuit;
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Verify login info is stored in preferences (if not add) and
		// check that calendar exists (if not create)
		Log.d(TAG, "initiating intent for calendarSetupActivity...");
		Intent intent = new Intent(this, calendarSetupActivity.class);
		startActivityForResult(intent, REQUEST_SETUP);
	}
	
	
	
	
	// Single onClick handler, uses switch statement to determine which
	// button was clicked. 
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnScan:	// invoke zing scanner application
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.setPackage("com.google.zxing.client.android");
				intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
				startActivityForResult(intent, REQUEST_ZXING);
				break;
			case R.id.btnUpdate:
				// STUB
				break;
			case R.id.btnQuit:
				finish();
		}
    }


	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// call super??
	    if (requestCode == REQUEST_ZXING) {
	        if (resultCode == RESULT_OK) {
	            String contents = intent.getStringExtra("SCAN_RESULT");
	            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
	            // Handle successful scan
	            // "contents" contains the barcode number
	            
	            // Intent intent = new Intent(this, <db??>.class);
	            // startActivity(intent);
	            // activate database activity
	        } else if (resultCode == RESULT_CANCELED) {
	            // Handle cancel...
	        }
	    } else if (requestCode == REQUEST_SETUP) {
	    	if (resultCode == RESULT_OK) {
	    		Log.d(TAG, "in onActivityResult, requestSetup");
	    		// no extras to retrieve...??
	    		setContentView(R.layout.main);
	    		
	    		// connect buttons to xml file and set listeners
	    		btnScan = (Button) findViewById(R.id.btnScan);
	    		btnUpdate = (Button) findViewById(R.id.btnUpdate);
	    		btnQuit = (Button) findViewById(R.id.btnQuit);
	    		
	    		btnQuit.setOnClickListener(this);
	    		btnScan.setOnClickListener(this);
	    		btnUpdate.setOnClickListener(this);
	    	} else if (resultCode == RESULT_CANCELED) {
	    		// Handle cancel...
	    		Log.d(TAG, "result cancelled...");
	    	}
	    }
	}
	
	
	
}