package osse.android.moldhold;

import java.util.Calendar;
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
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupWindow;


// Application displays two buttons, scan and update (currently a stub). 
// Clicking the "scan" button invokes the zing application. 
public class mainActivity extends Activity implements OnClickListener {
	Button		btnScan;
	Button		btnUpdate;
	DatePicker	datePicker1;
	EditText	editText1;
	
	
	public static String ScanResults = null; //to make sure of initial value
	private static final String DATABASE_NAME = "appdata";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_CREATE = "create table groc (upc_id TEXT PRIMARY KEY NOT NULL, " + "Expiration INTEGER NOT NULL, " + "Description TEXT NOT NULL);";
	private View myPopup;
	private DataBaseHelper dbh;
	private int day; //to get day from user datePicker1
	private int month; //to get month from user datePicker1
	private int year;  //to get year
	private String description; 
	private long shelflife; //the absolute shelf life of the product
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		dbh = new DataBaseHelper(this);
		// connect buttons to xml file and set listeners
		btnScan = (Button) findViewById(R.id.btnScan);
		btnUpdate = (Button) findViewById(R.id.btnUpdate);
		btnScan.setOnClickListener(this);
		btnUpdate.setOnClickListener(this);
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
				startActivityForResult(intent, 0);
				break;
			case R.id.btnUpdate:
				// STUB
				break;
		}
    }

	
	
	@Override
	//make popupwindow into alert dialog with inflator then you can get content
	//via onclicklistener setting private member data set inside the main 
	//activity
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// call super??
		Debug.startMethodTracing("mylog");
	    try {
	    if (requestCode == 0) {
	        if (resultCode == RESULT_OK) {
	        	ScanResults = intent.getStringExtra("SCAN_RESULT");
	            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
	            SQLiteDatabase data = dbh.getWritableDatabase(); //opens or creates database
	            String my_query = "SELECT * FROM groc WHERE upc_id = " + ScanResults + ";";
	            Cursor search = data.rawQuery(my_query, null); //the query of our database
	            if (search.getCount() == 0){
	            	MyAlert("Product Not Found!", "Please Enter Product Information:");
	            	Date end = new GregorianCalendar(year, month, day).getTime();
	            	Date today = new Date();
	            	long diff = end.getTime() - today.getTime();
	            	shelflife = (diff / (1000L*60L*60L*24L));
	            	ContentValues values = new ContentValues();
	            	values.put("upc", ScanResults);
	            	values.put("Expiration", shelflife);
	            	values.put("Description", description);
	            	data.insert("groc", null, values);
	            	//Intent new intent(this, calendarActivity.class);
	            	//intent.putExtra();
	            	//startactivityforresult(intent);
	            }
	            else {
	            	int descripInt = search.getColumnIndex("Description");
	            	int exprInt = search.getColumnIndex("Expiration");
	            	
	            	if ((descripInt == -1) || (exprInt == -1)) {
	            		//figure this out because -1 means column don't exist
	            	} else {
	            		description = search.getString(descripInt);
	            		shelflife = search.getInt(exprInt);
	            	}
	            }
	            // Handle successful scan
	            // "contents" contains the barcode number
	            
	            
	            
	            // Will do...
	        } else if (resultCode == RESULT_CANCELED) {
	            // Handle cancel
	        }
	    }
	    } catch (Exception e) {
	    	Log.i("MOLDHOLD", "Caught exception", e);
	    }
	    Debug.stopMethodTracing();
	}
	
	//the following is adapted from Fun Runner app by Charles Capps, thanks to 
	//Charles for suggesting this method
	public void MyAlert(String title, String msg){
		
		datePicker1 = (DatePicker) findViewById(R.id.datePicker1);
		editText1 = (EditText) findViewById(R.id.editText1);
		
		LayoutInflater myInflator = LayoutInflater.from(this);
		myPopup = myInflator.inflate(R.layout.data_popup, null);
		
		AlertDialog.Builder lertBuild = new AlertDialog.Builder(this);
		
		lertBuild.setMessage(msg);
		lertBuild.setTitle(title);
		lertBuild.setView(myPopup);
		
		lertBuild.setPositiveButton("Enter", new DialogInterface.OnClickListener(){ public void onClick(DialogInterface dialog, int id) {
			day = datePicker1.getDayOfMonth();
			month = datePicker1.getMonth();
			year = datePicker1.getYear();
			description = editText1.getText().toString();
			dialog.dismiss();
		}
		});
		
		lertBuild.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){ public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
		}
		});
				
		AlertDialog popup = lertBuild.create();
		popup.setCancelable(true);
		popup.show();
	}
	
	//Adapted from Vogella.org database tutorial
	public class DataBaseHelper extends SQLiteOpenHelper {

		public DataBaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
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
}