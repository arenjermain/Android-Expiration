package osse.android.moldhold;

//Copyright (c) 2010 Michelle Carter, Sarah Cathey, Aren Edlund-Jermain
//See COPYING file for license details.

import java.util.Calendar;

import com.google.api.client.sample.calendar.android.model.CalendarUrl;
import com.google.api.client.sample.calendar.android.model.EventEntry;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;


// Existence of MoldHold calendar was verified at application invocation.
//
// This activity is started by database activity. 

// credit to HelloDatePickerActivity Example
public class calendarActivity extends Activity {
	private String		productName = "";
	private int			shelfLife = 0;	// in days
    private int 		cYear;			// current year
    private int 		cMonth;			// current month
    private int 		cDay;			// current day
    private String		calendarID;
    
    private SharedPreferences		settings;
	
    private static final String 	PREF = "MoldHoldPrefs";
	private static final int 		DATE_DIALOG_ID = 0;
	private static final String		TAG = "calendarActivity";
	
	// UI
	private TextView 	dateDisplay;
	private Button		btnOk;
	private Button		btnPickDate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		settings = getSharedPreferences(PREF, MODE_PRIVATE);
		calendarID = settings.getString("CALENDAR_ID", null);
		if (calendarID == null)
			Log.d(TAG, "Shit, calendarID is null... this should never happend...");
		
		dateDisplay = (TextView) findViewById(R.id.dateDisplay);
		btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
            public void onClick(View v) {
				// add event using date
				// toast to say alarm has been added
                finish();	// return to mainActivity
            }
        });
		btnPickDate = (Button) findViewById(R.id.pickDate);
		btnPickDate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});
		
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			productName = extras.getString("NAME");
			shelfLife = extras.getInt("NUM_DAYS");
		}
		setCurrentDate();
		// take current date, add shelf life, to get date for alarm
		dateDisplay.setText(new StringBuilder()
				.append("Your ").append(productName)
				.append(" has a shelf life of ").append(shelfLife)
				.append("days. An alarm notifying you of its expiration ")
				.append("will be set for:\n")
        		.append(cMonth + 1).append("-")	// Month is 0 based so add 1
        		.append(cDay).append("-")
        		.append(cYear).append(" "));
		setContentView(R.layout.calendar);
	}
	
	
	private void setCurrentDate() {
        // get the current date
        final Calendar c = Calendar.getInstance();
        cYear = c.get(Calendar.YEAR);
        cMonth = c.get(Calendar.MONTH);
        cDay = c.get(Calendar.DAY_OF_MONTH);
	}
	
	
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    cYear = year;
                    cMonth = monthOfYear;
                    cDay = dayOfMonth;
                    // set event??
                }
            };
      
            
            
    //        
    private void addNewEvent() {
    	
    	// need: https://www.google.com/calendar/feeds/<calID>/owncalendars/full
    	
    	CalendarUrl url = CalendarUrl.forEventFeed(calendarID, "private", 
    		"full");
    	
        EventEntry event = newEvent();
        EventEntry result = client.executeInsertEvent(event, url);
    }
    
    
    
    //
    private EventEntry newEvent() {
        EventEntry event = new EventEntry();
        event.title = "Your " + productName + " expires in 3 days";
        When when = new When();
        when.startTime = new DateTime(new Date());
        event.when = when;
        // add alarm
        return event;
      }
}
