package com.orangelabs.rcs.core.access;

/**
 * Abstract network access
 * 
 * @author jexa7410
 */
public abstract class NetworkAccess {
    /**
     * Local IP address given to the network access
     */
	protected String ipAddress = null;

	/**
	 * Type of access
	 */
	protected String type = null;
	
	/**
	 * Constructor
	 */
	public NetworkAccess() {
	}

	/**
	 * Return the local IP address
	 * 
	 * @return IP address
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Return the type of access
	 * 
	 * @return Type
	 */
	public abstract String getType();

	/**
     * Connect to the network access
     * 
     * @param ipAddress IP address
     */
    public abstract void connect(String ipAddress);
    
	/**
     * Disconnect from the network access
     */
    public abstract void disconnect();
}
