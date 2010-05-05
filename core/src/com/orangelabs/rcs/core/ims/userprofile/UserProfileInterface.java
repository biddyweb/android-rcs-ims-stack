package com.orangelabs.rcs.core.ims.userprofile;

import com.orangelabs.rcs.core.CoreException;

/**
 * User profile interface
 * 
 * @author jexa7410
 */
public abstract class UserProfileInterface {
	/**
	 * Constructor
	 */
	public UserProfileInterface() {
	}
	
	/**
	 * Read the user profile
	 * 
	 * @return User profile
	 * @exception ImsException
	 */
	public abstract UserProfile read() throws CoreException;
}
