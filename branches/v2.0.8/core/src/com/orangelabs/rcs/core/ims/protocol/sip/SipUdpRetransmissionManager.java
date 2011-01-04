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
package com.orangelabs.rcs.core.ims.protocol.sip;

import gov.nist.core.sip.message.Message;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * SIP UDP retransmission manager
 * 
 * @author jexa7410
 */
public class SipUdpRetransmissionManager {
	/**
	 * T1 period (in milliseconds)
	 */
	public final static int T1_PERIOD = 500;
	
	/**
	 * T2 period (in milliseconds)
	 */
	public final static int T2_PERIOD = T1_PERIOD * 64;
	
	/**
	 * Timer
	 */
	private Timer timer = new Timer();

	/**
	 * SIP UDP manager
	 */
	private SipUdpManager udpManager;
	
	/**
	 * Current contexts
	 */
	private Hashtable<String, SipUdpRetransmissionContext> contexts = new Hashtable<String, SipUdpRetransmissionContext>();

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public SipUdpRetransmissionManager(SipUdpManager udpManager) {
		super();
		
		this.udpManager = udpManager;
	}
	
	/**
	 * Terminate the manager
	 */
	public void terminate() {
		try {
			timer.cancel();
			contexts.clear();
		} catch(Exception e) {}
	}
	
	/**
	 * Get a retransmission context
	 * 
	 * @param contextId Context ID
	 * @return Context
	 */
	public SipUdpRetransmissionContext getRetransmissionContext(String contextId) {
		return (SipUdpRetransmissionContext)contexts.get(contextId);
	}
	
	/**
	 * Add a new context for incoming message
	 * 
	 * @param msg Incoming SIP message
	 */
	public void addIncomingContext(Message msg) {
		synchronized(contexts) {
			String contextId = SipUdpRetransmissionContext.generateUdpContextId(msg);
			if (logger.isActivated()) {
				logger.debug("Add a new incoming context " + contextId);
			}
	
			// Add new entry in the list of contexts
			SipUdpRetransmissionContext context = new SipUdpRetransmissionContext(contextId, msg, null);
			contexts.put(contextId, context);
	
			// Start the expiration timer
			ExpirationTimer timerTask = new ExpirationTimer(contextId); 
			context.addExpirationTimer(timerTask);
			timer.schedule(timerTask, T2_PERIOD);			
			if (logger.isActivated()) {
				logger.debug("Start timer T2 (" + T2_PERIOD + "ms) for context " + contextId);
			}
		}
	}

	/**
	 * Add a new context for outgoing message
	 * 
	 * @param msg SIP message received
	 */
	public void addOutgoingContext(Message msg) {
		synchronized(contexts) {
			String contextId = SipUdpRetransmissionContext.generateUdpContextId(msg);
			SipUdpRetransmissionContext ctx = getRetransmissionContext(contextId);
			if (ctx != null)  {
				// A context already exist: update it
				if (logger.isActivated()) {
					logger.debug("Update the outgoing context " + contextId);
				}
				ctx.setOutgoingMessage(msg);
			} else {	
				if (logger.isActivated()) {
					logger.debug("Add a new outgoing context " + contextId);
				}
		
				// Add new entry in the list of contexts
				SipUdpRetransmissionContext context = new SipUdpRetransmissionContext(contextId, null, msg);
				contexts.put(contextId, context);
				
				// Add a retransmission timer
				RetransmissionTimer timerTask1 = new RetransmissionTimer(contextId, T1_PERIOD);
		        timer.schedule(timerTask1, T1_PERIOD);
				if (logger.isActivated()) {
					logger.debug("Start timer T1 (" + T1_PERIOD + "ms) for context " + contextId);
				}
				context.addRetransmissionTimer(timerTask1);
	
				// Start the expiration timer
				ExpirationTimer timerTask2 = new ExpirationTimer(contextId); 
				context.addExpirationTimer(timerTask2);
				timer.schedule(timerTask2, T2_PERIOD);			
				if (logger.isActivated()) {
					logger.debug("Start timer T2 (" + T2_PERIOD + "ms) for context " + contextId);
				}
			}
		}
	}
	
    /**
     * Retransmit a message
     * 
     * @param contextId Context id
     * @param msg Message
     */
    public void retransmitMessage(String contextId, Message msg) {
    	try {
        	// Retransmit the message
			if (logger.isActivated()) {
				logger.debug("Retransmit context " + contextId);
			}
			udpManager.retransmitMessage(msg);
    	} catch(Exception e) {
			if (logger.isActivated()) {
				logger.debug("Retransmission of context " + contextId + " has failed, remove it");
			}
    	}
    }

    /**
	 * Retransmission timer
	 */
    class RetransmissionTimer extends TimerTask {
    	private String contextId;
    	private int delay;
    	private long startedAt;
    	
        public RetransmissionTimer(String contextId, int delay) {
            super();
            
            this.contextId = contextId;
            this.delay = delay;
            this.startedAt = System.currentTimeMillis();
        }

        public RetransmissionTimer(String contextId, int delay, long startedAt) {
            super();
            
            this.contextId = contextId;
            this.delay = delay;
            this.startedAt = startedAt;
        }
        
        public void run() {
    		synchronized(contexts) {
	    		// Check if the context is existing
	    		SipUdpRetransmissionContext ctx = (SipUdpRetransmissionContext)contexts.get(contextId);
	            if (ctx == null) {
	            	// End of retransmission: context has been removed
		    		if (logger.isActivated()) {
		    			logger.debug("Timer T1: no more context " + contextId);
		    		}
	            	return;
	            }
	            
	            // Check if the response has been received before timer event
	            if (ctx.getIncomingMessage() != null) {
	            	// End of retransmission: response already received
		    		if (logger.isActivated()) {
		    			logger.debug("Timer T1: response already received for context " + contextId);
		    		}
	            	return;
	            }
	    		
	    		// Check if the retransmission period is passed
	            if ((System.currentTimeMillis() - startedAt) > T2_PERIOD) {
	            	// End of retransmission: another timeout will occur at a upper level
		    		if (logger.isActivated()) {
		    			logger.debug("Timer T1: end of retransmission for context " + contextId);
		    		}
	            	return;
	    		}
	    		
	        	// Retransmit the message
	    		if (logger.isActivated()) {
	    			logger.debug("Timer T1: start retransmission for context " + contextId);
	    		}
	            retransmitMessage(contextId, ctx.getOutgoingMessage());
	    		
	    		// Restart the timer for the next retransmission of the message
	            int newDelay = delay*2;
	    		RetransmissionTimer timerTask = new RetransmissionTimer(contextId, newDelay, startedAt);
	    		timer.schedule(timerTask, newDelay);
	    		if (logger.isActivated()) {
	    			logger.debug("Timer T1: restart timer (" + newDelay + "ms) for context " + contextId);
	    		}
    		}
        }
    }
    
	/**
	 * Expiration timer
	 */
    class ExpirationTimer extends TimerTask {
    	private String contextId;

    	public ExpirationTimer(String contextId) {
            super();

            this.contextId = contextId;
        }
        
        public void run() {
    		synchronized(contexts) {
	    		// This context has expired, remove it
				if (logger.isActivated()) {
					logger.debug("Timer T2: period has expired, remove context " + contextId);
				}
				contexts.remove(contextId);
				if (logger.isActivated()) {
					logger.debug("Number of contexts: " + contexts.size());
				}
    		}
        }
    }	
}
