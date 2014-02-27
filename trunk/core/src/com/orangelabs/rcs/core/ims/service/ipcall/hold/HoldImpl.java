package com.orangelabs.rcs.core.ims.service.ipcall.hold;

import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.UpdateSessionManagerListener;
import com.orangelabs.rcs.core.ims.service.ipcall.IPCallStreamingSession;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Interface for Hold implementation classes 
 * 
 * @author O. Magnon
 */
public abstract class HoldImpl {

	/**
	 * session handled by Hold Manager
	 */
	protected IPCallStreamingSession session ; 	
	
	/**
	 * Hold manager
	 */
	protected UpdateSessionManagerListener holdMngr;
	
	/**
	 * The logger
	 */
	protected Logger logger = Logger.getLogger(this.getClass().getName());	
	
	/**
	 * constructor
	 */
	public HoldImpl(IPCallStreamingSession session, HoldManager holdMngr){
//		if (logger.isActivated()){
//			logger.info("AddVideoImpl()");
//		}

		this.session = session;
		this.holdMngr = holdMngr;
	}	
	/**
	 * Set Call Hold for Local
	 * 
	 * @param callHoldAction  call hold action (true : call hold - false: call resume)
	 * @throws Exception 
	 */
	public abstract void setCallHold(boolean callHoldAction) throws Exception ;

	
	/**
	 * Set Call Hold for Remote
	 * 
	 * @param callHoldAction call hold action (true : call hold - false: call resume)
	 * @param reInvite  reInvite Request received
	 */
	public abstract void setCallHold(boolean callHoldAction, SipRequest reInvite); 

	
	/**
	 * Hold Media session
	 */
	public abstract void holdMediaSession();

	
	/**
	 * Resume Media session 
	 */
	public abstract void  resumeMediaSession();
	
}
