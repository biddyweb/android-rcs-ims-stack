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
package com.orangelabs.rcs.core;

import com.orangelabs.rcs.addressbook.AddressBookManager;
import com.orangelabs.rcs.core.gsm.CallManager;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.capability.CapabilityService;
import com.orangelabs.rcs.core.ims.service.im.InstantMessagingService;
import com.orangelabs.rcs.core.ims.service.presence.PresenceService;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingService;
import com.orangelabs.rcs.core.ims.service.toip.ToIpService;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.utils.Config;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Core (singleton pattern)
 *  
 * @author JM. Auffret
 */
public class Core {
	/**
	 * Singleton instance
	 */
	private static Core instance = null;
	
    /**
     * Core listener
     */
    private CoreListener listener;
    
    /**
     * Core status
     */
	private boolean started = false;

    /**
     * Terminal config
     */
    private Config config;
   
    /**
	 * IMS module
	 */
	private ImsModule imsModule;
	
	/**
	 * Call manager
	 */
	private CallManager callManager;

	/**
	 * User account manager
	 */
	private UserAccountManager userAccountManager;
	
	/**
	 * Address book manager
	 */
	private AddressBookManager addressBookManager;
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    
    /**
     * Returns the singleton instance
     * 
     * @return Core instance
     */
    public static Core getInstance() {
    	return instance;
    }
    
    /**
     * Instanciate the core
     * 
	 * @param listener Listener
     * @return Core instance
     * @throws CoreException
     */
    public synchronized static Core createCore(CoreListener listener) throws CoreException {
    	if (instance == null) {
    		instance = new Core(listener);
    	}
    	return instance;
    }
    
    /**
     * Terminate the core
     */
    public synchronized static void terminateCore() {
    	if (instance != null) {
    		instance.stopCore();
    	}
   		instance = null;
    }

    /**
     * Constructor
     * 
	 * @param listener Listener
     * @throws CoreException
     */
    private Core(CoreListener listener) throws CoreException {
		if (logger.isActivated()) {
        	logger.info("Terminal core initialization");
    	}
    	
		// Set core event listener
		this.listener = listener;

		// Create the terminal configuration file manager
        config = new Config("terminal.xml");

        // Set the default media ports
        NetworkRessourceManager.DEFAULT_LOCAL_RTP_PORT_BASE = RcsSettings.getInstance().getDefaultRtpPort();
        NetworkRessourceManager.DEFAULT_LOCAL_MSRP_PORT_BASE = RcsSettings.getInstance().getDefaultMsrpPort();
        
        // Set the URI format
		PhoneUtils.TEL_URI_SUPPORTED = RcsSettings.getInstance().isTelUriFormatUsed();
		if (logger.isActivated()) {
			logger.debug("Tel-URI support: " + PhoneUtils.TEL_URI_SUPPORTED);
		}
		
		// Set the country code
		PhoneUtils.setCountryCode(AndroidFactory.getApplicationContext());
		if (logger.isActivated()) {
			logger.debug("Country code: " + PhoneUtils.COUNTRY_CODE);
		}

    	// Create the user account manager
        userAccountManager = new UserAccountManager();

        // Create the call manager
    	callManager = new CallManager(this);
    	
    	// Create the IMS module
        imsModule = new ImsModule(this);
        
        // Create the address book manager
        addressBookManager = new AddressBookManager(this);

        if (logger.isActivated()) {
    		logger.info("Terminal core is created with success");
    	}
    }

	/**
	 * Returns the event listener
	 * 
	 * @return Listener
	 */
	public CoreListener getListener() {
		return listener;
	}

	/**
     * Returns the configuration file manager
     * 
     * @return Configuration file manager
     */
	public Config getConfig() {
		return config;
	}
	
	/**
     * Returns the IMS module
     * 
     * @return IMS module
     */
	public ImsModule getImsModule() {
		return imsModule;
	}

	/**
	 * Returns the user account manager
	 * 
	 * @return User account manager
	 */
	public UserAccountManager getAccountManager() {
		return userAccountManager;
	}
	
	/**
	 * Returns the address book manager
	 */
	public AddressBookManager getAddressBookManager(){
		return addressBookManager;
	}
	
	/**
	 * Returns the call manager
	 * 
	 * @return Call manager
	 */
	public CallManager getCallManager() {
		return callManager;
	}

	/**
     * Is core started
     * 
     * @return Boolean
     */
    public boolean isCoreStarted() {
    	return started;
    }

    /**
     * Start the terminal core
     * 
     * @throws CoreException
     */
    public synchronized void startCore() throws CoreException {
    	if (started) {
    		// Already started
    		return;
    	}
    	
    	// Start call monitoring
    	callManager.startCallMonitoring();
    	
    	// Start the IMS module 
    	imsModule.start();

    	// Start the address book monitoring
    	addressBookManager.startAddressBookMonitoring();
    	
    	// Notify event listener
		listener.handleCoreLayerStarted();
    	
		started = true;
    	if (logger.isActivated()) {
    		logger.info("RCS core service has been started with success");
    	}
    }
    	
    /**
     * Stop the terminal core
     */
    public synchronized void stopCore() {
    	if (!started) {
    		// Already stopped
    		return;
    	}    	
    	
    	if (logger.isActivated()) {
    		logger.info("Stop the RCS core service");
    	}
    	   	
    	try {
	    	// Stop call monitoring
	    	callManager.stopCallMonitoring();
	
	    	// Stop the IMS module 
	    	imsModule.stop();
	    	
	    	// Stop the address book monitoring
	    	addressBookManager.stopAddressBookMonitoring();
	    	
    	} catch(Exception e) {
    		if (logger.isActivated()) {
    			logger.error("Error during core shutdown", e);
    		}
    	}
   	
    	// Notify event listener
		listener.handleCoreLayerStopped();

    	started = false;
    	if (logger.isActivated()) {
    		logger.info("RCS core service has been stopped with success");
    	}
    }

	/**
	 * Returns all services
	 * 
	 * @return Array of services
	 */
	public ImsService[] getAllServices() {
		return getImsModule().getImsServices();
	}
	
	/**
	 * Returns the presence service
	 * 
	 * @return Presence service
	 */
	public PresenceService getPresenceService() {
		return getImsModule().getPresenceService();
	}
	
	/**
	 * Returns the capabity service
	 * 
	 * @return Capability service
	 */
	public CapabilityService getCapabilityService() {
		return getImsModule().getCapabilityService();
	}

	/**
	 * Returns the CSh service
	 * 
	 * @return CSh service
	 */
	public ContentSharingService getCShService() {
		return getImsModule().getContentSharingService();
	}
	
	/**
	 * Returns the IM service
	 * 
	 * @return IM service
	 */
	public InstantMessagingService getImService() {
		return getImsModule().getInstantMessagingService();
	}

	/**
	 * Returns the ToIP service
	 * 
	 * @return ToIP service
	 */
	public ToIpService getToIpService() {
		return getImsModule().getToIpService();
	}
}
