package com.orangelabs.rcs.service.api.client.toip;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.service.api.client.ClientApiException;
import com.orangelabs.rcs.service.api.client.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.CoreServiceNotAvailableException;
import com.orangelabs.rcs.service.api.client.IMediaRenderer;

/**
 * ToIP API
 * 
 * @author jexa7410
 */
public class ToIpApi extends ClientApi {
	/**
	 * Application context
	 */
	private Context ctx;
	
	/**
	 * Core service API
	 */
	private IToIpApi coreApi = null;

	/**
     * Constructor
     * 
     * @param ctx Application context
     */
    public ToIpApi(Context ctx) {
    	this.ctx = ctx;
    }
    
    /**
     * Connect API
     */
    public void connectApi() {
		ctx.bindService(new Intent(IToIpApi.class.getName()), apiConnection, 0);
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
	public IToIpApi getCoreServiceApi() {
		return coreApi;
	}
	
	/**
	 * Core service API connection
	 */
	private ServiceConnection apiConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	coreApi = IToIpApi.Stub.asInterface(service);

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
	 * Initiate a ToIP call
	 *  
 	 * @param contact Contact
	 * @param player Media player
	 * @param renderer Media renderer
	 * @return ToIP session
	 * @throws ClientApiException
 	 */
	public IToIpSession initiateToIpCall(String contact, IMediaPlayer player, IMediaRenderer renderer) throws ClientApiException {
    	if (coreApi != null) {
			try {
		    	return coreApi.initiateToIpCall(contact, player, renderer);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}

	/**
	 * Get a ToIP session from its session ID
	 *
	 * @param id Session ID
	 * @return Session
	 * @throws ClientApiException
	 */
	public IToIpSession getToIpSession(String id) throws ClientApiException {
    	if (coreApi != null) {
			try {
		    	return coreApi.getToIpSession(id);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
}
