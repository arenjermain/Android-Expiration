/*
Copyright � 2011 Sarah Cathey, Michelle Carter, Aren Edlund-Jermain
This project is protected under the Apache license. 
Please see COPYING file in the distribution for license terms.
*/

package com.google.api.client.sample.calendar.android.model;

import com.google.api.client.util.Key;

public class Reminder {
	
	//@Key("@days")
	//public int days;
	
	//@Key("@hours")
	//public int hours;
	
	@Key("@minutes")
	public int minutes;
	
	@Key("@method")
	public String method;

}
