package com.orangelabs.rcs.platform;

/**
 * Factory exception
 * 
 * @author JM. Auffret
 */
public class FactoryException extends java.lang.Exception {
	static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 *
	 * @param error Error message
	 */
	public FactoryException(String error) {
		super(error);
	}
}