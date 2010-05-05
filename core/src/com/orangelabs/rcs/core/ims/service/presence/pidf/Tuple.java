package com.orangelabs.rcs.core.ims.service.presence.pidf;

import java.util.Vector;

public class Tuple {

	private String id = null;
	private Status status = null;
	private Service service = null;
	private Vector<Contact> contactList = new Vector<Contact>();

	public Tuple(String id) {
		this.id = id;
	}

	public Vector<Contact> getContactList() {
		return contactList;
	}

	public void addContact(Contact contact) {
		contactList.addElement(contact);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}
}
