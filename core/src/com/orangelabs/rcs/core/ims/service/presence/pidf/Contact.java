package com.orangelabs.rcs.core.ims.service.presence.pidf;

public class Contact {

	private String uri = null;
	private String priority = null;
	private String contactType = null;

	public Contact() {
	}

	public Contact(String priority) {
		this.priority = priority;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
	}
}
