package com.orangelabs.rcs.core;

/**
 * Core module exception
 * 
 * @author JM. Auffret
 */
public class CoreException extends java.lang.Exception {
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 *
	 * @param error Error message
	 */
	public CoreException(String error) {
		super(error);
	}
}