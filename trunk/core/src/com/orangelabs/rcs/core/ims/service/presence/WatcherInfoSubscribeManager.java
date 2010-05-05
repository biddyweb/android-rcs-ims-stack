package com.orangelabs.rcs.core.ims.service.presence;

import java.io.ByteArrayInputStream;

import org.xml.sax.InputSource;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.protocol.sip.SipDialogPath;
import com.orangelabs.rcs.core.ims.protocol.sip.SipException;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.service.presence.watcherinfo.Watcher;
import com.orangelabs.rcs.core.ims.service.presence.watcherinfo.WatcherInfoDocument;
import com.orangelabs.rcs.core.ims.service.presence.watcherinfo.WatcherInfoParser;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Subscribe manager for presence watcher info event
 * 
 * @author jexa7410
 */
public class WatcherInfoSubscribeManager extends SubscribeManager {
	/**
     * The log4j logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param parent IMS module
     * @param presentity Presentity
     * @param defaultExpirePeriod Default expiration period in seconds
     */
    public WatcherInfoSubscribeManager(ImsModule parent, String presentity, int defaultExpirePeriod) {
    	super(parent, presentity, defaultExpirePeriod);
    }
    	
	/**
     * Create a SUBSCRIBE request
     * 
	 * @param dialog SIP dialog path
	 * @param expirePeriod Expiration period
	 * @param accessInfo Access info
	 * @return SIP request
	 * @throws SipException
     */
    public SipRequest createSubscribe(SipDialogPath dialog, int expirePeriod, String accessInfo) throws SipException {
    	// Create SUBSCRIBE message
    	SipRequest subscribe = SipMessageFactory.createSubscribe(dialog, expirePeriod, accessInfo);

    	// Set the Event header
    	subscribe.addHeader("Event: presence.winfo");

    	// Set the Accept header
    	subscribe.addHeader("Accept: application/watcherinfo+xml");

    	return subscribe;
    }

    /**
     * Receive a notification
     * 
     * @param notify Received notify
     */
    public void receiveNotification(SipRequest notify) {
    	// Check notification
    	if (!isNotifyForThisSubscriber(notify)) {
    		return;
    	}    	

    	if (logger.isActivated()) {
			logger.debug("New watcher-info notification received");
		}    	
    	
	    // Parse XML part
	    String content = notify.getContent();
		if (content != null) {
	    	try {
				InputSource input = new InputSource(
						new ByteArrayInputStream(notify.getContent().getBytes()));
				WatcherInfoParser parser = new WatcherInfoParser(input);
				WatcherInfoDocument watcherinfo = parser.getWatcherInfo();
				if (watcherinfo != null) {
					for (int i=0; i < watcherinfo.getWatcherList().size(); i++) {
						Watcher w = (Watcher)watcherinfo.getWatcherList().elementAt(i);
						String contact = w.getUri();
						String status = w.getStatus();
						String event = w.getEvent();
						
						if ((contact != null) && (status != null) && (event != null)) {
							if (status.equalsIgnoreCase("pending")) {
								// It's an invitation or a new status
								getImsModule().getCore().getListener().handlePresenceSharingInvitation(contact);
							}
							
							// Notify listener
							getImsModule().getCore().getListener().handlePresenceSharingNotification(contact, status, event);
						}
					}
				}
	    	} catch(Exception e) {
	    		if (logger.isActivated()) {
	    			logger.error("Can't parse watcher-info notification", e);
	    		}
	    	}
	    }
		
		// Check subscription state
		String state = notify.getHeader("Subscription-State");
		if ((state != null) && (state.indexOf("terminated") != -1)) {
			if (logger.isActivated()) {
				logger.info("Watcher-info subscription has been terminated by server");
			}
			terminatedByServer();
		}
    }
}
