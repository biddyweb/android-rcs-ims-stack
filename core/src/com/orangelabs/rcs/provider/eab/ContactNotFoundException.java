package com.orangelabs.rcs.provider.eab;

/**
 * Core service not available exception
 * 
 * @author jexa7410
 */
public class ContactNotFoundException extends RichAddressBookException {
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 */
	public ContactNotFoundException() {
		super("Contact not found in database");
	}
}