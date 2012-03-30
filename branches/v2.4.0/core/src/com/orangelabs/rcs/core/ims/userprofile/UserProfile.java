/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.core.ims.userprofile;

import javax2.sip.address.Address;
import javax2.sip.address.SipURI;
import javax2.sip.address.TelURL;
import javax2.sip.address.URI;

import com.orangelabs.rcs.core.ims.network.sip.SipUtils;

/**
 * User profile 
 *
 * @author JM. Auffret
 */
public class UserProfile {
	
	/**
	 * User name
	 */
	private String username;

	/**
	 * User private ID
	 */
	private String privateID;

	/**
	 * User password
	 */
	private String password;

	/**
	 * Home domain
	 */
	private String homeDomain;

	/**
	 * XDM server address
	 */
	private String xdmServerAddr;

	/**
	 * XDM server login
	 */
	private String xdmServerLogin;

	/**
	 * XDM server password
	 */
	private String xdmServerPassword;

	/**
	 * IM conference URI
	 */
	private String imConferenceUri;

	/**
	 * Public URI or associated URI
	 */
	private String associatedUri = null;
	
	/**
	 * Constructor
	 * 
	 * @param username Username
	 * @param privateID Private id
	 * @param password Password
	 * @param homeDomain Home domain
	 * @param xdmServerAddr XDM server address
	 * @param xdmServerLogin Outbound proxy address
	 * @param xdmServerPassword Outbound proxy address
	 * @param imConferenceUri IM conference factory URI
	 */
	public UserProfile(String username,
			String privateID,
			String password,
			String homeDomain,
			String xdmServerAddr,
			String xdmServerLogin,
			String xdmServerPassword,
			String imConferenceUri) {
		this.username = username;
		this.privateID = privateID;
		this.password = password;
		this.homeDomain = homeDomain;
		this.xdmServerAddr = xdmServerAddr;
		this.xdmServerLogin = xdmServerLogin;
		this.xdmServerPassword = xdmServerPassword;
		this.imConferenceUri = imConferenceUri;
	}

	/**
	 * Get the user name
	 * 
	 * @return Username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Set the user name
	 * 
	 * @param username Username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get the user public URI or associated URI
	 * 
	 * @return Public URI
	 */
	public String getPublicUri() {
		if (associatedUri == null) {
			// Use end user profile as public URI
			return "sip:" + username + "@" + homeDomain;
		} else {
			// Use P-Associated-URI as public URI
			return associatedUri;
		}
	}
	
	/**
	 * Set the user public URI or associated URI
	 * 
	 * @param uri Public URI
	 */
	public void setPublicUri(String uri) {
		try {
			Address addr = SipUtils.ADDR_FACTORY.createAddress(uri);
			URI uriObj = addr.getURI();
			if (uriObj instanceof SipURI) {
				// SIP-URI
				SipURI sip = (SipURI)uriObj;
				this.associatedUri = sip.toString();
			} else
			if (uriObj instanceof TelURL) {
				// Tel-URI
				TelURL tel = (TelURL)addr.getURI();
				this.associatedUri = tel.toString();
			}			
		} catch(Exception e) {
			this.associatedUri = null;
		}
	}
	
	/**
	 * Get the user private ID
	 * 
	 * @return Private ID
	 */
	public String getPrivateID() {
		return privateID;
	}
	
	/**
	 * Returns the user password
	 * 
	 * @return Password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the home domain
	 * 
	 * @return Home domain
	 */
	public String getHomeDomain() {
		return homeDomain;
	}
	
	/**
	 * Set the home domain
	 * 
	 * @param domain Home domain
	 */
	public void setHomeDomain(String domain) {
		this.homeDomain = domain;
	}
	
	/**
	 * Set the XDM server address
	 * 
	 * @param addr Server address
	 */
	public void setXdmServerAddr(String addr) {
		this.xdmServerAddr = addr;
	}

	/**
	 * Returns the XDM server address
	 * 
	 * @return Server address
	 */
	public String getXdmServerAddr() {
		return xdmServerAddr;
	}
	
	/**
	 * Set the XDM server login
	 * 
	 * @param login Login
	 */
	public void setXdmServerLogin(String login) {
		this.xdmServerLogin = login;
	}

	/**
	 * Returns the XDM server login
	 * 
	 * @return Login
	 */
	public String getXdmServerLogin() {
		return xdmServerLogin;
	}

	/**
	 * Set the XDM server password
	 * 
	 * @param pwd Password
	 */
	public void setXdmServerPassword(String pwd) {
		this.xdmServerPassword = pwd;
	}

	/**
	 * Returns the XDM server password
	 * 
	 * @return Password
	 */
	public String getXdmServerPassword() {
		return xdmServerPassword;
	}
	
	/**
	 * Set the IM conference URI
	 * 
	 * @param uri URI
	 */
	public void setImConferenceUri(String uri) {
		this.imConferenceUri = uri;
	}

	/**
	 * Returns the IM conference URI
	 * 
	 * @return URI
	 */
	public String getImConferenceUri() {
		return imConferenceUri;
	}

	/**
     * Returns the profile value as string
     * 
     * @return String
     */
	public String toString() {
		String result = "IMS username=" + username + ", " 
			+ "IMS private ID=" + privateID + ", "
			+ "IMS password=" + password + ", "
			+ "IMS home domain=" + homeDomain + ", "
			+ "XDM server=" + xdmServerAddr + ", "
			+ "XDM login=" + xdmServerLogin + ", "
			+ "XDM password=" + xdmServerPassword + ", " 
			+ "IM Conference URI=" + imConferenceUri;
		return result;
	}
}
