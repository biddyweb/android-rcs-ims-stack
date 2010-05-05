package com.orangelabs.rcs.provider.eab;

/**
 * Rich address book exception
 * 
 * @author jexa7410
 */
public class RichAddressBookException extends java.lang.Exception {
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 *
	 * @param error Error message
	 */
	public RichAddressBookException(String error) {
		super(error);
	}
}