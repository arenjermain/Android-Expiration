package osse.android.moldhold;


import android.app.Activity;
import android.os.Bundle;


// Existence of MoldHold calendar was verified at application invocation,
// and the calendar id was verified to exist in preferences. 
public class calendarActivity extends Activity {
	private String	productName = "";
	private int		shelfLife = 0;		// in days

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			productName = extras.getString("NAME");
			shelfLife = extras.getInt("NUM_DAYS");
		}
	}
}
