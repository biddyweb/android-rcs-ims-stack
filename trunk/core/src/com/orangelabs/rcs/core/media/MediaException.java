package com.orangelabs.rcs.core.media;

/**
 * Media exception
 * 
 * @author JM. Auffret
 */
public class MediaException extends java.lang.Exception {
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 *
	 * @param error Error message
	 */
	public MediaException(String error) {
		super(error);
	}
}