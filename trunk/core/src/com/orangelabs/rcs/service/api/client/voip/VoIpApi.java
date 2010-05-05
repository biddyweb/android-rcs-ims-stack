package com.orangelabs.rcs.service.api.client.voip;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.service.api.client.ClientApiException;
import com.orangelabs.rcs.service.api.client.CoreServiceNotAvailableException;
import com.orangelabs.rcs.service.api.client.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.IMediaRenderer;

/**
 * VoIP API
 * 
 * @author jexa7410
 */
public class VoIpApi extends ClientApi {
	/**
	 * Application context
	 */
	private Context ctx;
	
	/**
	 * Core service API
	 */
	private IVoIpApi coreApi = null;

	/**
     * Constructor
     * 
     * @param ctx Application context
     */
    public VoIpApi(Context ctx) {
    	this.ctx = ctx;
    }
    
    /**
     * Connect API
     */
    public void connectApi() {
		ctx.bindService(new Intent(IVoIpApi.class.getName()), apiConnection, 0);
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
	public IVoIpApi getCoreServiceApi() {
		return coreApi;
	}
	
	/**
	 * Core service API connection
	 */
	private ServiceConnection apiConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	coreApi = IVoIpApi.Stub.asInterface(service);

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
	 * Initiate a VoIP session
	 * 
	 * @param contact Contact
	 * @param player Media player
	 * @param renderer Media renderer
	 * @return VoIP session
	 * @throws ClientApiException
	 */
	public IVoIpSession initiateVoIpCall(String contact, IMediaPlayer player, IMediaRenderer renderer) throws ClientApiException {
    	if (coreApi != null) {
			try {
		    	return coreApi.initiateVoIpCall(contact, player, renderer);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
	
	/**
	 * Get a VoIP session from its session ID
	 * 
	 * @param id Session ID
	 * @return Session
	 * @throws ClientApiException
	 */
	public IVoIpSession getVoIpSession(String id) throws ClientApiException {
    	if (coreApi != null) {
			try {
		    	return coreApi.getVoIpSession(id);
			} catch(Exception e) {
				throw new ClientApiException(e.getMessage());
			}
		} else {
			throw new CoreServiceNotAvailableException();
		}
	}
}
