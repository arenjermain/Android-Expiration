package osse.android.moldhold;

// Copyright (c) 2010 Michelle Carter, Sarah Cathey, Aren Edlund-Jermain
// See COPYING file 

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;

import com.google.api.client.sample.calendar.android.model.CalendarClient;
import com.google.api.client.sample.calendar.android.model.CalendarEntry;
import com.google.api.client.sample.calendar.android.model.CalendarFeed;
import com.google.api.client.sample.calendar.android.model.CalendarUrl;
import com.google.api.client.util.DateTime;
import com.google.common.collect.Lists;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

public class calendarSetupActivity extends Activity {
	private GoogleAccountManager 		accountManager;
	private String 						gsessionid;
	private String 						authToken;
	private String 						accountName;
	private SharedPreferences			settings;
	private boolean						calendarExists = false; 
	
	CalendarClient 						client;
	private final List<CalendarEntry> 	calendars = Lists.newArrayList();
	private final HttpTransport 		transport = AndroidHttp.newCompatibleTransport();
	
	private static final String		TAG = "calSetup";
	private static final String 	PREF = "MoldHoldPrefs";
	private static final String		CALENDAR_NAME = "MoldHold Expiration Dates";
	private static final String 	AUTH_TOKEN_TYPE = "cl"; // for calendar
	private static final int 		REQUEST_AUTHENTICATE = 0;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		accountManager = new GoogleAccountManager(this);
		settings = getSharedPreferences(PREF, MODE_PRIVATE);
		
		// Try to get from preferences (will be empty at initial run)
		authToken = settings.getString("AUTH_TOKEN", null);
		gsessionid = settings.getString("GSESSION_ID", null);
		
		final MethodOverride override = new MethodOverride(); // needed for PATCH
		Log.d(TAG, "createing new CalendarClient...");
		client = new CalendarClient(transport.createRequestFactory(
				new HttpRequestInitializer() {
	     
			public void initialize(HttpRequest request) {
				GoogleHeaders headers = new GoogleHeaders();
				headers.setApplicationName("MoldHold/1.0");
				headers.gdataVersion = "2";
				request.headers = headers;
				client.initializeParser(request);
				request.interceptor = new HttpExecuteInterceptor() {
	
					public void intercept(HttpRequest request) 
						throws IOException {
						GoogleHeaders headers = (GoogleHeaders) request.headers;
						headers.setGoogleLogin(authToken);
						request.url.set("gsessionid", gsessionid);
						override.intercept(request);
					}
				};
				request.unsuccessfulResponseHandler = 
					new HttpUnsuccessfulResponseHandler() {
	
					public boolean handleResponse(HttpRequest request, 
							HttpResponse response, boolean retrySupported) {
						switch (response.statusCode) {
							case 302:	// found
								GoogleUrl url = 
									new GoogleUrl(response.headers.location);
								gsessionid = (String) url.getFirst("gsessionid");
								SharedPreferences.Editor editor = settings.edit();
								editor.putString("GSESSION_ID", gsessionid);
								editor.commit();
								return true;
							case 401:	// unauthorized -> bad authToken 
								accountManager.invalidateAuthToken(authToken);
								authToken = null;
								SharedPreferences.Editor editor2 = settings.edit();
								editor2.remove("AUTH_TOKEN");
								editor2.commit();
								return false;
						} 
						return false;	// any other status code
					}
				};
			} // end public void initialize
		}));  // end new calendarClient
	    gotAccount();
	    if (!calendarExists) {
	    	Log.d(TAG, "creating new calendar...");
	    	//createNewCalendar();
	    }
	    finish(); 	// return to mainActivity
	} 
	

	
	
	private void gotAccount() {
		Account account = accountManager.getAccountByName(accountName);
		if (account != null) {
			// invalid token
			if (authToken == null) {	
				accountManager.manager.getAuthToken(account, AUTH_TOKEN_TYPE, 
						true, new AccountManagerCallback<Bundle>() {

							public void run(AccountManagerFuture<Bundle> future) {
								try {
									Bundle bundle = future.getResult();
									if (bundle.containsKey(
											AccountManager.KEY_INTENT)) {
										Intent intent = bundle.getParcelable(
												AccountManager.KEY_INTENT);
										int flags = intent.getFlags();
										flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK; // ???
										intent.setFlags(flags);
										startActivityForResult(intent, 
												REQUEST_AUTHENTICATE);
									} else if (bundle.containsKey(
											AccountManager.KEY_AUTHTOKEN)) {
										setAuthToken(bundle.getString(
												AccountManager.KEY_AUTHTOKEN));
										calendarExists = executeRefreshCalendars();
									}
								} catch (Exception e) {
									handleException(e);
								}
							}
						}, null);
			// valid token
			} else 					
				calendarExists = executeRefreshCalendars();
		} else			// if account is null
			chooseAccount();
	}
	
	
	
	// This method is called if getAccountByName fails in gotAccount() (see
	// above). Verifies the existence of MoldHold calendar. 
	private void chooseAccount() { 
		accountManager.manager.getAuthTokenByFeatures(
				GoogleAccountManager.ACCOUNT_TYPE, AUTH_TOKEN_TYPE, null,
				calendarSetupActivity.this, null, null,
				new AccountManagerCallback<Bundle>() {

					public void run(AccountManagerFuture<Bundle> future) {
						Bundle bundle;
						try {
							bundle = future.getResult();
							setAccountName(bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
							setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
							calendarExists = executeRefreshCalendars();
						} catch (OperationCanceledException e) {
							// user canceled
						} catch (AuthenticatorException e) {
							handleException(e);
						} catch (IOException e) {
							handleException(e);
						}
					}
				}, null);
	}	
	
	
	
	// Saves the authorization token to preferences and to class variable
	// "authToken."
	void setAuthToken(String authToken) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("AUTH_TOKEN", authToken);
		editor.commit();
		this.authToken = authToken;
	}
	
	
	
	//
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    switch (requestCode) {
	    	case REQUEST_AUTHENTICATE:
	            if (resultCode == RESULT_OK) 
	                gotAccount();
	            else 
	                chooseAccount();
	    		break;
	    }
	}
	
	
	
	// Saves the account name to preferences and to class variable
	// "accountName."
	void setAccountName(String accountName) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("ACCOUNT_NAME", accountName);
		editor.remove("GSESSION_ID");
		editor.commit();
		this.accountName = accountName;
		gsessionid = null;
	}
	  
	
	
	// Refreshes calendars and checks to see if MoldHold calendar exits.
	// Returns true if found, else false. 
	private boolean executeRefreshCalendars() {
	    String 		calendarName;
	    boolean 	found = false;
	    
	    List<CalendarEntry> calendars = this.calendars;
	    calendars.clear();
	    try {
	    	CalendarUrl url = CalendarUrl.forAllCalendarsFeed();
	    	// add all existing calendars to list
	    	while (true) {
	    		CalendarFeed feed = client.executeGetCalendarFeed(url);
	    		if (feed.calendars != null) 
	    			calendars.addAll(feed.calendars);
	    		String nextLink = feed.getNextLink();
	    		if (nextLink == null) 
	    			break;
	    	} 
	    	
	    	// iterate through list and make sure that moldHold calendar
	    	// exists
	    	int numCalendars = calendars.size();
	    	for (int i = 0; i < numCalendars; i++) {
	    		calendarName = calendars.get(i).title;
	    		Log.d(TAG, "calendar title: " + calendarName);
	    		if (calendarName.equals(CALENDAR_NAME))
	    			found = true;
	    	}
	    } catch (IOException e) {
	    	handleException(e);
	    	calendars.clear();
	    }
	    
	    return found;
	 }
	
	
	
	// Create MoldHold calendar
	private void createNewCalendar() {
        CalendarUrl url = CalendarUrl.forOwnCalendarsFeed();
        CalendarEntry calendar = new CalendarEntry();
        calendar.title = CALENDAR_NAME;
        calendar.summary = "This calendar contains the expiration dates " +
        		"managed by MoldHold";
        try {
          client.executeInsertCalendar(calendar, url);
        } catch (IOException e) {
          handleException(e);
        }
        executeRefreshCalendars(); // disregard return value
	}
	
	
	// CHANGES???
	void handleException(Exception e) {
		e.printStackTrace();
		if (e instanceof HttpResponseException) {
			HttpResponse response = ((HttpResponseException) e).response;
		    int statusCode = response.statusCode;
		    try {
		    	response.ignore();
		    } catch (IOException e1) {
		        e1.printStackTrace();
		    }
		    // TODO(yanivi): should only try this once to avoid infinite loop ?????
		    if (statusCode == 401) {
		    	gotAccount();
		        return;
		    }
		}
		Log.e(TAG, e.getMessage(), e);
	}
	
}
