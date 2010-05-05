package com.orangelabs.rcs.core.ims.service.presence.watcherinfo;

public class Watcher {

	private String id;
	private String status;
	private String event;
	private String uri;
	private String displayName;
	
	public Watcher(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String name) {
		this.displayName = name;
	}
}
