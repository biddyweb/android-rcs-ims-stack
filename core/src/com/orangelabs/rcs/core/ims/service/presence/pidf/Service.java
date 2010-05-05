package com.orangelabs.rcs.core.ims.service.presence.pidf;

public class Service {
	private String id = null;
	
	public Service(String id) {
		this.id = id;
	}

	public Service() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
