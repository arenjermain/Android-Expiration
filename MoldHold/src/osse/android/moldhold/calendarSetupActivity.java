package osse.android.moldhold;

import java.io.IOException;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;

import com.google.api.client.sample.calendar.android.model.CalendarClient;
import com.google.api.client.sample.calendar.android.model.CalendarEntry;
import com.google.api.client.sample.calendar.android.model.CalendarFeed;
import com.google.api.client.sample.calendar.android.model.CalendarUrl;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

public class calendarSetupActivity extends Activity {
	private GoogleAccountManager 		accountManager;
	private String 						gsessionid;
	private String 						authToken;
	private String 						acctName;
	private SharedPreferences			settings;
	
	CalendarClient client;
	private final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	
	private static final String 	PREF = "MoldHoldPrefs";
	private static final String		CALENDAR_NAME = "MoldHold Expiration Dates";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		accountManager = new GoogleAccountManager(this);
		settings = getSharedPreferences(PREF, MODE_PRIVATE);
		
		// Try to get from preferences:
		authToken = settings.getString("AUTH_TOKEN", null);
		gsessionid = settings.getString("GSESSION_ID", null);
		
		final MethodOverride override = new MethodOverride(); // needed for PATCH
		client = new CalendarClient(transport.createRequestFactory(new HttpRequestInitializer() {
	      public void initialize(HttpRequest request) {
	          GoogleHeaders headers = new GoogleHeaders();
	          headers.setApplicationName("MoldHold/1.0");
	          headers.gdataVersion = "2";
	          request.headers = headers;
	          client.initializeParser(request);
	          request.interceptor = new HttpExecuteInterceptor() {

	            public void intercept(HttpRequest request) throws IOException {
	              GoogleHeaders headers = (GoogleHeaders) request.headers;
	              headers.setGoogleLogin(authToken);
	              request.url.set("gsessionid", gsessionid);
	              override.intercept(request);
	            }
	          };
	          request.unsuccessfulResponseHandler = new HttpUnsuccessfulResponseHandler() {

	            public boolean handleResponse(
	                HttpRequest request, HttpResponse response, boolean retrySupported) {
	              switch (response.statusCode) {
	                case 302:
	                  GoogleUrl url = new GoogleUrl(response.headers.location);
	                  gsessionid = (String) url.getFirst("gsessionid");
	                  SharedPreferences.Editor editor = settings.edit();
	                  editor.putString("GSESSION_ID", gsessionid);
	                  editor.commit();
	                  return true;
	                case 401:
	                  accountManager.invalidateAuthToken(authToken);
	                  authToken = null;
	                  SharedPreferences.Editor editor2 = settings.edit();
	                  editor2.remove("AUTH_TOKEN");
	                  editor2.commit();
	                  return false;
	              }
	              return false;
	            }
	          };
	        }
	      }));
		
		
	}
	
}
