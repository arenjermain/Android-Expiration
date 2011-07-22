package osse.android.moldhold;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;



// Application displays two buttons, scan and update (currently a stub). 
// Clicking the "scan" button invokes the zxing application. 
public class mainActivity extends Activity implements OnClickListener {
	private static final String 	TAG = "MoldHold";
	private static final int		ZXING = 0;

	private Button					btnScan;
	private Button					btnUpdate;
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Verify login info is stored in preferences (if not add) and
		// check that calendar exists (if not create)
		Intent intent = new Intent(this, calendarSetupActivity.class);
		startActivity(intent);
		
		setContentView(R.layout.main);
		
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
				startActivityForResult(intent, ZXING);
				break;
			case R.id.btnUpdate:
				// STUB
				break;
		}
    }


	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// call super??
	    if (requestCode == ZXING) {
	        if (resultCode == RESULT_OK) {
	            String contents = intent.getStringExtra("SCAN_RESULT");
	            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
	            // Handle successful scan
	            // "contents" contains the barcode number
	            
	            // Intent intent = new Intent(this, <db??>.class);
	            // startActivity(intent);
	            // activate database activity
	        } else if (resultCode == RESULT_CANCELED) {
	            // Handle cancel
	        }
	    } 
	}
	
	
	
}