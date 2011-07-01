/*******************************************************************************
 * Software Name : RCS IMS Stack
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
import com.orangelabs.rcs.core.ims.network.registration.GibaRegistrationProcedure;
import com.orangelabs.rcs.core.ims.network.registration.HttpDigestRegistrationProcedure;
import com.orangelabs.rcs.core.ims.network.registration.RegistrationManager;
import com.orangelabs.rcs.core.ims.network.registration.RegistrationProcedure;
import com.orangelabs.rcs.core.ims.network.sip.SipManager;
import com.orangelabs.rcs.core.ims.userprofile.GibaUserProfileInterface;
import com.orangelabs.rcs.core.ims.userprofile.SettingsUserProfileInterface;
import com.orangelabs.rcs.core.ims.userprofile.UserProfile;
import com.orangelabs.rcs.core.ims.userprofile.UserProfileInterface;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;
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
	 * IMS authentication mode associated to the network interface
	 */
	protected String authentMode;

    /**
     * IMS transport protocol
     */
    protected String protocol;

    /**
     * IMS proxy
     */
    private String imsProxyAddr;

    /**
	 * Registration procedure associated to the network interface
	 */
	protected RegistrationProcedure registrationProcedure;

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
     * @param imsProxyAddr IMS proxy address
     * @param authentMode Authentication mode
     */
	public ImsNetworkInterface(ImsModule imsModule, int type, NetworkAccess access,
            String imsProxyAddr, String authentMode, String protocol) {
		this.imsModule = imsModule;
		this.type = type;
		this.access = access;
        this.imsProxyAddr = imsProxyAddr;
		this.authentMode = authentMode;
        this.protocol = protocol;

        // Instantiates the SIP manager
        sip = new SipManager(this);

        // Load the registration procedure
        loadRegistrationProcedure();

        // Instantiates the registration manager
        registration = new RegistrationManager(this, registrationProcedure);
	}

    /**
     * Is behind a NAT
     *
     * @return Boolean
     */
    public boolean isBehindNat() {
		return registration.isBehindNat();
    }
	
	/**
     * Returns the IMS authentication mode
     *
     * @return Authentication mode
     */
	public String getAuthenticationMode() {
		return authentMode;
	}

	/**
     * Load the registration procedure associated to the network access
     */
	public void loadRegistrationProcedure() {
		if (authentMode.equals(RcsSettingsData.GIBA_AUTHENT)) {
			if (logger.isActivated()) {
				logger.debug("Load GIBA authentication procedure");
			}
			this.registrationProcedure = new GibaRegistrationProcedure();
		} else
		if (authentMode.equals(RcsSettingsData.DIGEST_AUTHENT)) {
			if (logger.isActivated()) {
				logger.debug("Load HTTP Digest authentication procedure");
			}
			this.registrationProcedure = new HttpDigestRegistrationProcedure();
        }
	}

	/**
     * Returns the user profile associated to the network access
     *
     * @return User profile
     */
	public UserProfile getUserProfile() {
		UserProfileInterface intf;
		if (authentMode.equals(RcsSettingsData.GIBA_AUTHENT)) {
			if (logger.isActivated()) {
				logger.debug("Load user profile derived from IMSI (GIBA)");
			}
    		intf = new GibaUserProfileInterface();
    	} else {
			if (logger.isActivated()) {
				logger.debug("Load user profile from RCS settings database");
			}
            intf = new SettingsUserProfileInterface();
    	}
    	return intf.read();
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
            sip.initStack(access.getIpAddress(), imsProxyAddr, protocol);
	    	sip.getSipStack().addSipEventListener(imsModule);
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't instanciate the SIP stack", e);
			}
			return false;
		}

		// Init the registration procedure
		registration.init();

    	// Register to IMS
		boolean registered = registration.registration();
		if (registered) {
			if (logger.isActivated()) {
				logger.debug("IMS registration successful");
			}

            // Start keep-alive for NAT if activated
            if (registration.isBehindNat() && RcsSettings.getInstance().isSipKeepAliveEnabled()) {
                sip.getSipStack().getKeepAliveManager().start();
            }
		} else {
			if (logger.isActivated()) {
				logger.debug("IMS registration has failed");
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
