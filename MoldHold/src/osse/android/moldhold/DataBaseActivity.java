/*
Copyright © 2011 Sarah Cathey, Michelle Carter, Aren Edlund-Jermain
This project is protected under the Apache license. 
Please see COPYING file in the distribution for license terms.
*/

package osse.android.moldhold;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

public class DataBaseActivity extends Activity implements OnClickListener {
	public EditText 		textedit;		// product name
	public DatePicker 		datepick;		// date of expiration
	private String 			scanResults;
	private DataBaseHelper 	dbh;
	private int 			day; //to get day from user datePicker1
	private int 			month; //to get month from user datePicker1
	private int 			year;  //to get year
	private String 			description; 
	private long 			shelflife; //the absolute shelf life of the product
	private SQLiteDatabase 	data = null;
	private Button 			btnOk;
	private Button 			btnCancel;
	
	private static final String 	TAG = "DBActivity"; // for Android logging
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// get extras
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			scanResults = extras.getString("SCAN_RESULT");
			
		}
	
		datepick = new DatePicker(DataBaseActivity.this);
		dbh = new DataBaseHelper(DataBaseActivity.this);
		data = dbh.getWritableDatabase(); 		// opens or creates database
		String my_query = "SELECT * FROM " + DataBaseHelper.productTable + 
				" WHERE " + DataBaseHelper.colID + "=?"; 
	    String[] args = {scanResults};
	    Cursor search = data.rawQuery(my_query, args); // the query of our database
	    
	    if (search.moveToFirst() == true) { // item found in database
	    	int descripInt = search.getColumnIndex("Description");
	    	int exprInt = search.getColumnIndex("Expiration");
	    	
	    	if ((descripInt == -1) || (exprInt == -1)) {
	    		// figure this out because -1 means column don't exist
	    	} else {
	    		description = search.getString(descripInt);
	    		shelflife = search.getInt(exprInt);
	    	}
	    	data.close();
	    	wereDone(); //to finish activity
	    } else {							// item not found in database
	    	setContentView(R.layout.data_popup);
	    	
	    	textedit = (EditText) findViewById(R.id.etProductName);
	    	datepick = (DatePicker) findViewById(R.id.datePicker1);
	    	
	    	btnOk = (Button) findViewById(R.id.btnDateOk);
	    	btnCancel = (Button) findViewById(R.id.btnCancel);
	    	
	    	btnOk.setOnClickListener(DataBaseActivity.this);
	    	btnCancel.setOnClickListener(DataBaseActivity.this);
	    }
	
	}

	

	// Adapted from Vogella.org database tutorial
	public class DataBaseHelper extends SQLiteOpenHelper {
		static final String dbname = "MoldHoldDB";
		static final String productTable = "Products";
		static final String colID = "UpcID";
		static final String colDescription = "Description";
		static final String colExpiration = "Expiration";
		
		public DataBaseHelper(Context context) {
			super(context, dbname, null, 1);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + productTable + " (" + colID + 
					" INTEGER PRIMARY KEY , " + colDescription + " TEXT , " +
					colExpiration + " INTEGER)");
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(DataBaseHelper.class.getName(),
					"Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS todo");
			onCreate(db);
		}
	
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnDateOk:
			// get date from datepicker
			day = datepick.getDayOfMonth();
			month = datepick.getMonth();
			year = datepick.getYear();
			
			description = textedit.getText().toString();
			Calendar c = Calendar.getInstance();// today's date
			c.set(year, month, day);			// change to user entered date
			Date end = c.getTime();				// convert Calendar to Date
	    	Date today = new Date();			// today's date
	    	long diff = end.getTime() - today.getTime(); 
	    	shelflife = (diff / (1000L*60L*60L*24L));
	    	ContentValues newData = new ContentValues();
	    	// add info to database
	    	newData.put(DataBaseHelper.colDescription, description);
	    	newData.put(DataBaseHelper.colExpiration, shelflife);
	    	newData.put(DataBaseHelper.colID, scanResults);
	    	data.insert(DataBaseHelper.productTable, DataBaseHelper.colID, newData);
	    	data.close();
	    	wereDone(); 	//because we're done
			break;
		case R.id.btnCancel:
			finish();
			break;
		}
		
	} 
	
	
	
	// Saves shelflife and description class variables to extras and returns
	// to mainActivity.
	public void wereDone(){
		Intent intent = new Intent();
    	intent.putExtra("DESCRIPTION", description);
    	intent.putExtra("SHELF_LIFE", shelflife);
    	setResult(Activity.RESULT_OK, intent);
    	finish();
	}
	
}
