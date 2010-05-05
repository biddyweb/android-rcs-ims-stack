package com.orangelabs.rcs.core.ims.service.presence.pidf;

import com.orangelabs.rcs.utils.DateUtils;

public class Person {
	private String id = null;
	private Note note = null;
	private OverridingWillingness willingness = null;
	private StatusIcon statusIcon = null;
	private String homePage = null;
	private long timestamp = -1;

	public Person(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public Note getNote() {
		return note;
	}

	public void setNote(Note note) {
		this.note = note;
	}	

	public OverridingWillingness getOverridingWillingness() {
		return willingness;
	}

	public void setOverridingWillingness(OverridingWillingness status) {
		this.willingness = status;
	}

	public StatusIcon getStatusIcon(){
		return statusIcon;
	}
	
	public void setStatusIcon(StatusIcon statusIcon){
		this.statusIcon = statusIcon;
	}

	public String getHomePage(){
		return homePage;
	}
	
	public void setHomePage(String homePage){
		this.homePage = homePage;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String ts) {
		this.timestamp = DateUtils.decodeDate(ts);
	}
}
