package com.orangelabs.rcs.core.ims.service.ipcall;

import android.os.RemoteException;

import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpUtils;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoSdpBuilder;
import com.orangelabs.rcs.utils.logger.Logger;


/**
 * Super class for IP Call Hold Manager 
 * 
 * @author O. Magnon
 */
public abstract class  IPCall_Hold {
	/**
	 * Constant values for IPCall Hold states
	 */
	static final int IDLE = 0; 
	static final int HOLD_INPROGRESS = 1;
	static final int HOLD = 2;
	static final int UNHOLD_INPROGRESS = 3;
	
	// Hold state
	int state;
	
	// session handled by Hold manager
	IPCallStreamingSession session ; 
	
	/**
	 * The logger
	 */
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	
	public IPCall_Hold(IPCallStreamingSession session){
		if (logger.isActivated()){
			logger.info("IPCall_Hold()");
		}
		this.state = IPCall_Hold.IDLE;
		this.session = session;
	}
	
	public abstract void setCallHold(boolean callHoldAction);
	
	
	public abstract void setCallHold(boolean callHoldAction, SipRequest reInvite);
		
	
	public abstract void prepareSession();
	

}
