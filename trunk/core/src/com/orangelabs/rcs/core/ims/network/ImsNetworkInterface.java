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
package com.orangelabs.rcs.core.ims.network;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.core.access.NetworkAccess;
import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.registration.RegistrationManager;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Abstract IMS network interface
 *  
 * @author JM. Auffret
 */
public abstract class ImsNetworkInterface {
	/**
	 * IMS module
	 */
	private ImsModule imsModule;
	
	/**
	 * Network interface type
	 */
	private int type;
	
    /**
	 * Network access
	 */
	private NetworkAccess access;
	
    /**
     * SIP manager
     */
    private SipManager sip;

    /**
     * Registration manager
     */
    private RegistrationManager registration;
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 * 
	 * @param imsModule IMS module
	 * @param type Network interface type
	 * @param access Network access
	 */
	public ImsNetworkInterface(ImsModule imsModule, int type, NetworkAccess access) {
		this.imsModule = imsModule;
		this.type = type;
		this.access = access;
		
        // Instanciates the SIP manager
        sip = new SipManager(this,
        		ImsModule.IMS_USER_PROFILE.getOutboundProxyAddr(),
        		imsModule.getCore().getConfig().getInteger("SipListeningPort", 5060));
         
        // Instanciates the registration manager
        registration = new RegistrationManager(this,
        		imsModule.getCore().getConfig().getInteger("RegisterExpirePeriod", 3600));
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
	 * Returns the network interface type
	 * 
	 * @return Type (see ConnectivityManager class)
	 */
	public int getType() {
		return type;
	}
	
	/**
     * Returns the network access
     * 
     * @return Network access
     */
    public NetworkAccess getNetworkAccess() {
    	return access;
    }

	/**
     * Returns the SIP manager
     * 
     * @return SIP manager
     */
    public SipManager getSipManager() {
    	return sip;
    }

    /**
     * Is registered
     * 
     * @return Return True if the terminal is registered, else return False
     */
    public boolean isRegistered() {
        return registration.isRegistered();
    }    

    /**
     * Register to the IMS
     * 
     * @return Registration result
     */
    public boolean register() {
		if (logger.isActivated()) {
			logger.debug("Register to IMS");
		}

		// Initialize the SIP stack
		try {
	    	sip.initStack(access.getIpAddress());
	    	sip.getSipStack().addSipListener(imsModule);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't instanciate the SIP stack", e);
			}
			return false;
		}
		
    	// Register to IMS
		boolean registered = registration.registration();
		if (!registered) {
			if (logger.isActivated()) {
				logger.debug("IMS registration has failed");
			}
		} else {
			if (logger.isActivated()) {
				logger.debug("IMS registration successful");
			}
		}
    	
    	return registered;
    }
    
	/**
     * Unregister from the IMS
     */
    public void unregister() {
		if (logger.isActivated()) {
			logger.debug("Unregister from IMS");
		}

		// Unregister from IMS
		registration.unRegistration();
    	
    	// Close the SIP stack
    	sip.closeStack();
    }
    
	/**
     * Registration terminated
     */
    public void registrationTerminated() {
		if (logger.isActivated()) {
			logger.debug("Registration has been terminated");
		}

		// Stop registration
		registration.stopRegistration();

		// Close the SIP stack
    	sip.closeStack();
    }
    
    /**
     * Returns the network access info
     * 
     * @return String
     * @throws CoreException
     */
    public String getAccessInfo() throws CoreException {
    	return getNetworkAccess().getType();
    }
}
