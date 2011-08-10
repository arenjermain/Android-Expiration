/*
Copyright (c) 2011 Sarah Cathey, Michelle Carter, Aren Edlund-Jermain
This project is protected under the Apache license. 
Please see COPYING file in the distribution for license terms.
*/

package osse.android.moldhold;

import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;


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
	private static final int		REQUEST_ZXING = 0;
	private static final int		REQUEST_CALENDAR = 1;
	private static final int		REQUEST_DB = 2;
	

	private Button			btnScan;
	private Button			btnUpdate;
	private Button			btnQuit;
	
	public static String 	ScanResults = null; //to make sure of initial value

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Verify login info is stored in preferences (if not add) and
		// check that calendar exists (if not create)
	/*	
        Long sl = (long) 10;
    	Intent calIntent = new Intent(this, calendarActivity.class);
    	calIntent.putExtra("DESCRIPTION", "milk"); // CHANGE BACK - description
    	calIntent.putExtra("SHELF_LIFE", sl);  		// CHANGE BACK - shelflife
    	startActivityForResult(calIntent, REQUEST_CALENDAR); */
 
		setContentView(R.layout.main);
		
		// connect buttons to xml file and set listeners
		btnScan = (Button) findViewById(R.id.btnScan);
		btnUpdate = (Button) findViewById(R.id.btnUpdate);
		btnQuit = (Button) findViewById(R.id.btnQuit);
		
		btnQuit.setOnClickListener(this);
		btnScan.setOnClickListener(this);
		btnUpdate.setOnClickListener(this);

		

	}
	
	
	// Single onClick handler, uses switch statement to determine which
	// button was clicked. 
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnScan:	// invoke zxing scanner application
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


	
	// make popupwindow into alert dialog with inflator then you can get content
	// via onclicklistener setting private member data set inside the main 
	// activity
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// call super??

		//Debug.startMethodTracing("mylog");
	    try {
		    if (requestCode == REQUEST_ZXING) {
		        if (resultCode == RESULT_OK) {	// handle successful scan
		        	
		        	ScanResults = intent.getStringExtra("SCAN_RESULT");
		        	
		        	Intent dbIntent = new Intent(this, DataBaseActivity.class);
		        	dbIntent.putExtra("SCAN_RESULT", ScanResults);   	
	            	startActivityForResult(dbIntent, REQUEST_DB);
		        	
	            	
		        } else if (resultCode == RESULT_CANCELED) {
		            // Handle cancel...
		        	Log.d(TAG, "zxing result cancelled...");
		        }
		    } else if (requestCode == REQUEST_DB) {
		    	if (resultCode == RESULT_OK) {
		    		String description = intent.getStringExtra("DESCRIPTION");
		    		Long shelflife = intent.getLongExtra("SHELF_LIFE", -1);
		    		
		    		Intent calIntent = new Intent(this, calendarActivity.class);
	            	calIntent.putExtra("DESCRIPTION", description); 
	            	calIntent.putExtra("SHELF_LIFE", shelflife);  	
	            	startActivityForResult(calIntent, REQUEST_CALENDAR);
		    	} else if (resultCode == RESULT_CANCELED) {
		            // Handle cancel...
		        	Log.d(TAG, "zxing result cancelled...");
		        }
		    } else if (requestCode == REQUEST_CALENDAR) {
		         if (resultCode == RESULT_OK) {	
		        	 Log.d(TAG, "calActivity result OK");
		         } else if (resultCode == RESULT_CANCELED) {
		        	 // Handle cancel...
		        	 Log.d(TAG, "calActivity result cancelled");
		         }
		    }
		    
	    } catch (Exception e) {
	    	Log.i("MOLDHOLD", "Caught exception", e);
	    }
	}
}

