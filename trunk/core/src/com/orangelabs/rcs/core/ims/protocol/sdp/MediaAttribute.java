package com.orangelabs.rcs.core.ims.protocol.sdp;

/**
 * Media attribute
 * 
 * @author jexa7410
 */
public class MediaAttribute {
	/**
	 * Attribute name
	 */
	private String name;

	/**
	 * Attribute value
	 */
	private String value;

	/**
	 * Constructor
	 * 
	 * @param name Attribute name
	 * @param value Attribute value
	 */
	public MediaAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Returns the attribute name
	 * 
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the attribute value
	 * 
	 * @return Value
	 */
	public String getValue() {
		return value;
	}
}
