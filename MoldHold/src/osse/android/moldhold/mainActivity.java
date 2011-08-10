/*
Copyright (c) 2011 Sarah Cathey, Michelle Carter, Aren Edlund-Jermain
This project is protected under the Apache license. 
Please see COPYING file in the distribution for license terms.
*/

package osse.android.moldhold;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


// Clicking the "scan" button invokes the zxing application. 
//
// This application uses SharedPreferences which contains the following keys:
//		"AUTH_TOKEN"
//		"GSESSION_ID"
//		"ACCOUNT_NAME"
//		"CALENDAR_ID"
//		"ACCEPTED"
//
public class mainActivity extends Activity implements OnClickListener {
	private static final String 	TAG = "MoldHold"; // for Android logging
	private static final String 	PREF = "MoldHoldPrefs"; // preferences name
	private SharedPreferences 		settings;
	private static final int		REQUEST_ZXING = 0;
	private static final int		REQUEST_CALENDAR = 1;
	private static final int		REQUEST_DB = 2;
	private static final int		DISCLAIM_DIALOG = 3;
	

	private Button			btnScan;
	private Button			btnUpdate;
	private Button			btnQuit;
	
	public static String 	ScanResults = null; //to make sure of initial value

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Check whether the user has already accepted the disclaimer.
		// The user will only be prompted to agree to the disclaimer the first 
		// time they run the application.
		settings = getSharedPreferences(PREF, MODE_PRIVATE);
		boolean accepted = settings.getBoolean("ACCEPTED", false);
		
		if(!accepted)
			showDialog(DISCLAIM_DIALOG);
 
		setMainMenu();
	}
	
	
	// Single onClick handler, uses switch statement to determine which
	// button was clicked. 
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnScan:	// invoke zxing scanner application
				setContentView(R.layout.processing);
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

	
	
	// Set's the main screen and connects buttons.
	public void setMainMenu() {
		setContentView(R.layout.main);
		
		// connect buttons to xml file and set listeners
		btnScan = (Button) findViewById(R.id.btnScan);
		btnUpdate = (Button) findViewById(R.id.btnUpdate);
		btnQuit = (Button) findViewById(R.id.btnQuit);
		
		btnQuit.setOnClickListener(this);
		btnScan.setOnClickListener(this);
		btnUpdate.setOnClickListener(this);
	}

	
	// This method can be invoked by return from Zxing, DataBaseActivity, or
	// calendarActivity.
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// call super??

		//Debug.startMethodTracing("mylog");
	    try {
		    if (requestCode == REQUEST_ZXING) {
		        if (resultCode == RESULT_OK) {	// handle successful scan
		        	setContentView(R.layout.processing);
		        	ScanResults = intent.getStringExtra("SCAN_RESULT");
		        	
		        	Intent dbIntent = new Intent(this, DataBaseActivity.class);
		        	dbIntent.putExtra("SCAN_RESULT", ScanResults);   	
	            	startActivityForResult(dbIntent, REQUEST_DB);	
		        } else if (resultCode == RESULT_CANCELED) {
		            // Handle cancel...
		        	setMainMenu();
		        	Log.d(TAG, "zxing result cancelled...");
		        }
		    } else if (requestCode == REQUEST_DB) {
		    	if (resultCode == RESULT_OK) {
		    		setContentView(R.layout.processing);
		    		String description = intent.getStringExtra("DESCRIPTION");
		    		Long shelflife = intent.getLongExtra("SHELF_LIFE", -1);
		    		
		    		Intent calIntent = new Intent(this, calendarActivity.class);
	            	calIntent.putExtra("DESCRIPTION", description); 
	            	calIntent.putExtra("SHELF_LIFE", shelflife);  	
	            	startActivityForResult(calIntent, REQUEST_CALENDAR);
		    	} else if (resultCode == RESULT_CANCELED) {
		    		setMainMenu();
		        }
		    } else if (requestCode == REQUEST_CALENDAR) {
		         if (resultCode == RESULT_OK) {	
		        	 Log.d(TAG, "calActivity result OK");
		        	 setMainMenu();
		         } else if (resultCode == RESULT_CANCELED) {
		        	 setMainMenu();
		         }
		    } 
	    } catch (Exception e) {
	    	Log.i("MOLDHOLD", "Caught exception", e);
	    }
	}
	
	
	
    // Dialog display. Uses switch statement to see which dialog was called.
	// Displays disclaimer to user, if they select "Agree" the application will  
	// continue, otherwise if they select "Disagree" the application will close.  
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DISCLAIM_DIALOG:  //Dialog to display disclaimer before the application can run
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setMessage("LEGAL DISCLAIMER: \n" +
		    	"The MoldHold application is strictly for fun and convenience. " +
		    	"The creators of MoldHold are not responsible for any health " +
		   		"problems resulting from expired food. As the user, you are responsible for " +
		   		"setting an accurate expiration date for your grocery item. \n" +
		   		"In addition to the above agreement, this application will require " +
		   		"access to your Google Calendar account. It will simply create a calendar" +
	    		" for your food expiration dates and will not change any of the existing calendars." +
	    		" By agreeing to this disclaimer you are allowing access to your calendars" +
	    		" and waiving the right to sue for health problems due to expired food." )
		           .setCancelable(false)
		           .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                   //if the user agrees, set preferences to true and don't check again
		                   SharedPreferences.Editor editor = settings.edit();
		                   editor.putBoolean("ACCEPTED", true);
		                   // Commit the changes made to preferences
		                   editor.commit();                    
		               }
		           })
		           .setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
		        	   public void onClick(DialogInterface dialog, int id) {
		        		   // if user does not agree to disclaimer, application
		        		   // will terminate
		                   finish();
		                 }
		           });
		    AlertDialog alert = builder.create();
		    return alert;
		}
		return null;
	}
}

