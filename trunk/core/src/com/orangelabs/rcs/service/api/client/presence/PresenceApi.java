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
package com.orangelabs.rcs.service.api.client.presence;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.orangelabs.rcs.core.ims.service.presence.PresenceInfo;
import com.orangelabs.rcs.provider.eab.RichAddressBook;
import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.service.api.client.ClientApiException;
import com.orangelabs.rcs.service.api.client.CoreServiceNotAvailableException;

/**
 * Presence API
 * 
 * @author jexa7410
 */
public class PresenceApi extends ClientApi {
	/**
	 * Application context
	 */
	private Context ctx;
	
	/**
	 * Core service API
	 */
	private IPresenceApi coreApi = null;

	/**
     * Constructor
     * 
     * @param ctx Application context
     */
    public PresenceApi(Context ctx) {
    	this.ctx = ctx;
    	
    	// Initialize EAB content provider
    	RichAddressBook.createInstance(ctx);
    }
    
    /**
     * Connect API
     */
    public void connectApi() {
		ctx.bindService(new Intent(IPresenceApi.class.getName()), apiConnection, 0);
    }
    
    /**
     * Disconnect API
     */
    public void disconnectApi() {
		ctx.unbindService(apiConnection);
    }

    /**
     * Returns the core service API
     * 
     * @return API
     */
	public IPresenceApi getCoreServiceApi() {
		return coreApi;
	}

	/**
	 * Core service API connection
	 */
	private ServiceConnection apiConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            coreApi = IPresenceApi.Stub.asInterface(service);
       
        	// Notify event listener
        	notifyEventApiConnected();
        }

        public void onServiceDisconnected(ComponentName className) {
        	// Notify event listener
        	notifyEventApiDisconnected();

        	coreApi = null;
        }
    };

	/**
	 * Set my presence info
	 * 
	 * @param info Presence info
	 * @return Boolean result
	 * @throws ClientApiException
	 */
	public boolean setMyPresenceInfo(PresenceInfo info) throws ClientApiException {
		if (coreApi != null) {
			try {
				// Publish my presence info
				return coreApi.setMyPresenceInfo(info);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}

	/**
	 * Set my hyper-availability status
	 * 
	 * @param status Status
	 * @return Boolean result
	 * @throws ClientApiException
	 */
	public boolean setMyHyperAvailabilityStatus(boolean status) throws ClientApiException {
		if (coreApi != null) {
			try {
				// Publish my hyper-availability status
				return coreApi.setMyHyperAvailabilityStatus(status);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}

	/**
	 * Get the hyper-availability expiration time
	 * 
	 * @return Expiration time in milliseconds or 0 if no period activated
	 * @throws ClientApiException
	 */
	public long getHyperAvailabilityExpiration() throws ClientApiException {
		if (coreApi != null) {
			try {
				// Read expiration value from the poke manager
				return coreApi.getHyperAvailabilityExpiration();
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}

	/**
	 * Invite a contact to share its presence
	 * 
	 * @param contact Contact
	 * @return Boolean result
	 * @throws ClientApiException
	 */
	public boolean inviteContact(String contact) throws ClientApiException {
		if (coreApi != null) {
			try {
				// Update XDM server
				return coreApi.inviteContact(contact);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
		
	/**
	 * Accept the sharing invitation
	 * 
	 * @param contact Contact
	 * @return Boolean result
	 * @throws ClientApiException
	 */
	public boolean acceptSharingInvitation(String contact) throws ClientApiException {
		if (coreApi != null) {
			try {
				// Update XDM server
				return coreApi.acceptSharingInvitation(contact);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
	
	/**
	 * Reject the sharing invitation
	 *
	 * @param contact Contact
	 * @return Boolean result
	 * @throws ClientApiException
	 */
	public boolean rejectSharingInvitation(String contact) throws ClientApiException {
		if (coreApi != null) {
			try {
				// Update XDM server
				return coreApi.rejectSharingInvitation(contact);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
	
	/**
	 * Ignore the sharing invitation
	 *
	 * @param contact Contact
	 * @throws ClientApiException
	 */
	public void ignoreSharingInvitation(String contact) throws ClientApiException {
		if (coreApi != null) {
			try {
				coreApi.ignoreSharingInvitation(contact);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}

	/**
	 * Revoke a shared contact
	 * 
	 * @param contact Contact
	 * @return Boolean result
	 * @throws ClientApiException
	 */
	public boolean revokeContact(String contact) throws ClientApiException {
		if (coreApi != null) {
			try {
				// Update XDM server
				return coreApi.revokeContact(contact);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}

	/**
	 * Unrevoke a revoked contact
	 * 
	 * @param contact Contact
	 * @return Boolean result
	 * @throws ClientApiException
	 */
	public boolean unrevokeContact(String contact) throws ClientApiException {
		if (coreApi != null) {
			try {
				// Update XDM server
				return coreApi.unrevokeContact(contact);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}	

	/**
	 * Unblock a blocked contact
	 * 
	 * @param contact Contact
	 * @return Boolean result
	 * @throws ClientApiException
	 */
	public boolean unblockContact(String contact) throws ClientApiException {
		if (coreApi != null) {
			try {
				// Update XDM server
				return coreApi.unblockContact(contact);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}	

	/**
	 * Get the list of granted contacts
	 * 
	 * @return List of contacts
	 * @throws ClientApiException
	 */
	public List<String> getGrantedContacts() throws ClientApiException {
		if (coreApi != null) {
			try {
				// Read XDM server
				return coreApi.getGrantedContacts();
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
	
	/**
	 * Get the list of revoked contacts
	 * 
	 * @return List of contacts
	 * @throws ClientApiException
	 */
	public List<String> getRevokedContacts() throws ClientApiException {
		if (coreApi != null) {
			try {
				// Read XDM server
				return coreApi.getRevokedContacts();
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}

	/**
	 * Get the list of blocked contacts
	 * 
	 * @return List of contacts
	 * @throws ClientApiException
	 */
	public List<String> getBlockedContacts() throws ClientApiException {
		if (coreApi != null) {
			try {
				// Read XDM server
				return coreApi.getBlockedContacts();
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
	
	/**
	 * Request capabilities for a given contact (i.e. anonymous fetch)
	 * 
	 * @param contact Contact
	 * @throws ClientApiException
	 */
	public void requestCapabilities(String contact) throws ClientApiException {
		if (coreApi != null) {
			try {
				// Request capabilities
				coreApi.requestCapabilities(contact);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
}
