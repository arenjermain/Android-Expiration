package com.google.api.client.sample.calendar.android.model;

public class EventEntry extends Entry {

	public String getEventFeedLink() {
		return Link.find(links, "http://schemas.google.com/gCal/2005#eventFeed");
	}

	
	
	@Override
	public EventEntry clone() {
		return (EventEntry) super.clone();
	}
}
