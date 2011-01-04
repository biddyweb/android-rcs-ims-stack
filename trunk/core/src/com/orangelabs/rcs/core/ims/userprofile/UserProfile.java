/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright © 2010 France Telecom S.A.
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
	 * User display name
	 */
	private String displayName;

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
	 * Outbound proxy address
	 */
	private String outboundProxyAddr;
	
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
	 * Constructor
	 * 
	 * @param username Username
	 * @param displayName Display name
	 * @param privateID Private id
	 * @param password Password
	 * @param homeDomain Home domain
	 * @param outboundProxy Outbound proxy address
	 * @param xdmServerAddr XDM server address
	 * @param xdmServerLogin Outbound proxy address
	 * @param xdmServerPassword Outbound proxy address
	 * @param imConferenceUri IM conference factory URI
	 */
	public UserProfile(String username,
			String displayName,
			String privateID,
			String password,
			String homeDomain,
			String outboundProxyAddr,
			String xdmServerAddr,
			String xdmServerLogin,
			String xdmServerPassword,
			String imConferenceUri) {
		this.username = username;
		this.displayName = displayName;
		this.privateID = privateID;
		this.password = password;
		this.homeDomain = homeDomain;
		this.outboundProxyAddr = outboundProxyAddr;
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
	 * Get the user public SIP URI
	 * 
	 * @return Public SIP URI
	 */
	public String getPublicUri() {
		return "sip:" + username + "@" + homeDomain;
	}
	
	/**
	 * Get the user public SIP URI prefixed with the display name
	 * 
	 * @return Public SIP URI with display name
	 */
	public String getNameAddress() {
		String uri = "\""+ displayName + "\"" + " <" +getPublicUri()+ ">";			
		return uri;
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
	 * Returns the user display name
	 * 
	 * @return Display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Set the user display name
	 * 
	 * @param displayName Display name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
	 * Returns the outbound proxy address
	 * 
	 * @return Proxy address
	 */
	public String getOutboundProxyAddr() {
		return outboundProxyAddr;
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
			+ "IMS display name=" + displayName + ", "
			+ "IMS home domain=" + homeDomain + ", "
			+ "IMS outbound proxy=" + outboundProxyAddr + ", "
			+ "XDM server=" + xdmServerAddr + ", "
			+ "XDM login=" + xdmServerLogin + ", "
			+ "XDM password=" + xdmServerPassword + ", " 
			+ "IM Conference URI=" + imConferenceUri;
		return result;
	}	
}
