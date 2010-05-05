package com.orangelabs.rcs.core.ims.service.presence.rlmi;

public class ResourceInstance {

	private String uri;
	private String state = null;
	private String reason = null;

	public ResourceInstance(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public String getState() {
		return state;
	}

	public String getReason() {
		return reason;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
