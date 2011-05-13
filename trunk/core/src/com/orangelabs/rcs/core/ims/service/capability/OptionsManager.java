package com.orangelabs.rcs.core.ims.service.capability;

import java.util.ArrayList;
import java.util.Vector;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.network.sip.SipMessageFactory;
import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sip.SipMessage;
import com.orangelabs.rcs.core.ims.protocol.sip.SipRequest;
import com.orangelabs.rcs.core.ims.protocol.sip.SipResponse;
import com.orangelabs.rcs.provider.eab.ContactsManager;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.capability.Capabilities;
import com.orangelabs.rcs.utils.MimeManager;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Capability discovery manager using options procedure
 *  
 * @author jexa7410
 */
public class OptionsManager implements DiscoveryManager {
    /**
     * IMS module
     */
    private ImsModule imsModule;
    
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param parent IMS module
     */
    public OptionsManager(ImsModule parent) {
        this.imsModule = parent;
    }
    
	/**
     * Request contact capabilities
     * 
     * @param contact Remote contact
     */
    public void requestCapabilities(String contact) {
    	if (logger.isActivated()) {
    		logger.debug("Request capabilities in background for " + contact);
    	}
    	
    	// Update capability timestamp
    	ContactsManager.getInstance().setContactCapabilitiesTimestamp(contact, System.currentTimeMillis());
    	
		// Check if we are in call with the contact
		boolean inCall = imsModule.isRichcallServiceActivated() && imsModule.getCallManager().isConnectedWith(contact);
		
    	// Start request in background
    	OptionsRequestTask task = new OptionsRequestTask(imsModule, contact, SipUtils.getSupportedFeatureTags(inCall));
    	task.start();
    }
    
    /**
     * Extract features tags
     * 
     * @param msg Message
     * @return Capabilities
     */
    public static Capabilities extractCapabilities(SipMessage msg) {
    	// Analyze feature tags
    	Capabilities capabilities = new Capabilities(); 
    	ArrayList<String> tags = msg.getFeatureTags();
    	if (tags.contains(SipUtils.FEATURE_RCSE_VIDEO_SHARE)) {
    		capabilities.setVideoSharingSupport(true);
    	}
    	if (tags.contains(SipUtils.FEATURE_RCSE_IMAGE_SHARE)) {
    		capabilities.setImageSharingSupport(true);
    	}
    	if (tags.contains(SipUtils.FEATURE_RCSE_CHAT)) {
    		capabilities.setImSessionSupport(true);
    	}
    	if (tags.contains(SipUtils.FEATURE_RCSE_FT)) {
    		capabilities.setFileTransferSupport(true);
    	}
    	if (tags.contains(SipUtils.FEATURE_RCSE_PRESENCE_DISCOVERY)) {
    		capabilities.setPresenceDiscoverySupport(true);
    	}
    	if (tags.contains(SipUtils.FEATURE_RCSE_SOCIAL_PRESENCE)) {
    		capabilities.setSocialPresenceSupport(true);
    	}
    	for(int i=0; i < tags.size(); i++) {
    		String tag = tags.get(i);
    		if (tag.startsWith(SipUtils.FEATURE_RCSE_EXTENSION)) {
    			capabilities.addSupportedExtension(tag);
    		}
    	}
    	
		// Store & forward support
        if (RcsSettings.getInstance().isImAlwaysOn()) {
			capabilities.setImSessionSupport(true);
        }		    		
		
    	// Analyze SDP part
    	byte[] content = msg.getContentBytes();
		if (content != null) {
	    	SdpParser parser = new SdpParser(content);

	    	// Get supported video codecs
	    	Vector<MediaDescription> mediaVideo = parser.getMediaDescriptions("video");
	    	Vector<String> videoCodecs = new Vector<String>();
			for (int i=0; i < mediaVideo.size(); i++) {
				MediaDescription desc = mediaVideo.get(i);
	    		MediaAttribute attr = desc.getMediaAttribute("rtpmap");
				if (attr !=  null) {
    	            String rtpmap = attr.getValue();
    	            String encoding = rtpmap.substring(rtpmap.indexOf(desc.payload)+desc.payload.length()+1);
    	            String codec = encoding.toLowerCase().trim();
    	            int index = encoding.indexOf("/");
    				if (index != -1) {
    					codec = encoding.substring(0, index);
    				}
    	    		if (MediaRegistry.isCodecSupported(codec)) {
    	    			videoCodecs.add(codec);
    	    		}    			
    			}
			}
			if (videoCodecs.size() == 0) {
				// No video codec supported between me and the remote contact
				capabilities.setVideoSharingSupport(false);
			}
    	
	    	// Check supported image formats
	    	Vector<MediaDescription> mediaImage = parser.getMediaDescriptions("message");
	    	Vector<String> imgFormats = new Vector<String>();
			for (int i=0; i < mediaImage.size(); i++) {
				MediaDescription desc = mediaImage.get(i);
	    		MediaAttribute attr = desc.getMediaAttribute("accept-types");
				if (attr != null) {
    	            String[] types = attr.getValue().split(" ");
    	            for(int j = 0; j < types.length; j++) {
    	            	String fmt = types[j];
    	            	if (MimeManager.isMimeTypeSupported(fmt)) {
    	            		imgFormats.addElement(fmt);
    	            	}
    	    		}
				}
			}
			if (imgFormats.size() == 0) {
				// No image format supported between me and the remote contact
				capabilities.setImageSharingSupport(false);
			}
		}
        
    	return capabilities;
    }
    
    /**
     * Receive a capability request (options procedure)
     * 
     * @param options Received options message
     */
    public void receiveCapabilityRequest(SipRequest options) {
    	String contact = SipUtils.getAssertedIdentity(options);

    	if (logger.isActivated()) {
			logger.debug("OPTIONS request received from " + contact);
		}
    	
	    try {
	    	// Create 200 OK response
	        SipResponse resp = SipMessageFactory.create200OkOptionsResponse(options,
	        		imsModule.getSipManager().getSipStack().getContactHeader(),
	        		SipUtils.getSupportedFeatureTags(false), null);

	        // Send 200 OK response
	        imsModule.getSipManager().sendSipResponse(resp);
	    } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Can't send 200 OK for OPTIONS", e);
        	}
	    }    	

    	// Read features tag in the request
    	Capabilities capabilities = OptionsManager.extractCapabilities(options);

    	// Update capabilities in database
    	if (capabilities.isImSessionSupported()) {
    		// RCS-e contact
    		ContactsManager.getInstance().setContactCapabilities(contact, capabilities, true, ContactsManager.REGISTRATION_STATUS_ONLINE);
    	} else {
    		// Not a RCS-e contact
    		ContactsManager.getInstance().setContactCapabilities(contact, capabilities, true, ContactsManager.REGISTRATION_STATUS_UNKNOWN);
    	}
    	
    	// Notify listener
    	imsModule.getCore().getListener().handleCapabilitiesNotification(contact, capabilities);    	
    }
}