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
package com.orangelabs.rcs.core.ims.service.capability;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.protocol.sip.SipMessage;
import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.capability.Capabilities;
import com.orangelabs.rcs.service.api.client.capability.CapabilityApiIntents;
import com.orangelabs.rcs.utils.MimeManager;

/**
 * Capability utility functions
 * 
 * @author jexa7410
 */
public class CapabilityUtils {
	/**
	 * RCS-e video share feature tag
	 */
	public final static String FEATURE_RCSE_VIDEO_SHARE = "+g.3gpp.cs-voice";

	/**
	 * RCS-e feature tag
	 */
	public final static String FEATURE_RCSE = "+g.3gpp.iari-ref";
	
	/**
	 * RCS-e image share feature tag
	 */
	public final static String FEATURE_RCSE_IMAGE_SHARE = "urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-is";

	/**
	 * RCS-e chat feature tag
	 */
	public final static String FEATURE_RCSE_CHAT = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.im";

	/**
	 * RCS-e file transfer feature tag
	 */
	public final static String FEATURE_RCSE_FT = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.ft";

	/**
	 * RCS-e presence discovery feature tag
	 */
	public final static String FEATURE_RCSE_PRESENCE_DISCOVERY = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.dp";

	/**
	 * RCS-e social presence feature tag
	 */
	public final static String FEATURE_RCSE_SOCIAL_PRESENCE = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.sp";

	/**
	 * RCS-e extension feature tag prefix
	 */
	public final static String FEATURE_RCSE_EXTENSION = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.orange";
	
	/**
	 * Get all supported feature tags
	 *
	 * @return List of tags
	 */
	public static List<String> getAllSupportedFeatureTags() {
		return getSupportedFeatureTags(true);
	}
	
	/**
	 * Get supported feature tags based on the in call state
	 *
	 * @param inCall In call flag
	 * @return List of tags
	 */
	public static List<String> getSupportedFeatureTags(boolean inCall) {
		List<String> tags = new ArrayList<String>();

		// Add RCS tags
		if (RcsSettings.getInstance().isVideoSharingSupported()&& inCall) {
			tags.add(FEATURE_RCSE_VIDEO_SHARE);
		}

		// Add RCS-e tags
		String supported = "";
		if (RcsSettings.getInstance().isImSessionSupported()) {
			supported += FEATURE_RCSE_CHAT + ",";
		}
		if (RcsSettings.getInstance().isImageSharingSupported()&& inCall) {
			supported += FEATURE_RCSE_IMAGE_SHARE + ",";
		}
		if (RcsSettings.getInstance().isFileTransferSupported()) {
			supported += FEATURE_RCSE_FT + ",";
		}
		if (RcsSettings.getInstance().isPresenceDiscoverySupported()) {
			supported += FEATURE_RCSE_PRESENCE_DISCOVERY + ",";
		}
		if (RcsSettings.getInstance().isSocialPresenceSupported()) {
			supported += FEATURE_RCSE_SOCIAL_PRESENCE + ",";
		}

		// Add RCS extensions
		String extensions = RcsSettings.getInstance().getSupportedRcsExtensions();
		if ((extensions != null) && (extensions.length() > 0)) {
			String[] extensionList = extensions.split(";");
			for (int i=0;i<extensionList.length;i++) {
				supported += extensionList[i] + ",";
			}
		}

		// Add prefixes
		if (supported.length() != 0) {
			supported = FEATURE_RCSE + "=\"" + supported.substring(0, supported.length()-1) + "\"";
			tags.add(supported);
		}
		
		return tags;
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
    	for(int i=0; i < tags.size(); i++) {
    		String tag = tags.get(i);
        	if (tag.equals(FEATURE_RCSE_VIDEO_SHARE)) {
        		capabilities.setVideoSharingSupport(true);
        	}
        	if (tag.equals(FEATURE_RCSE_IMAGE_SHARE)) {
        		capabilities.setImageSharingSupport(true);
        	}
        	if (tag.equals(FEATURE_RCSE_CHAT)) {
        		capabilities.setImSessionSupport(true);
        	}
        	if (tag.equals(FEATURE_RCSE_FT)) {
        		capabilities.setFileTransferSupport(true);
        	}
        	if (tag.equals(FEATURE_RCSE_PRESENCE_DISCOVERY)) {
        		capabilities.setPresenceDiscoverySupport(true);
        	}
        	if (tag.equals(FEATURE_RCSE_SOCIAL_PRESENCE)) {
        		capabilities.setSocialPresenceSupport(true);
        	}
    		if (tag.equals(FEATURE_RCSE_EXTENSION)) {
    			capabilities.addSupportedExtension(tag);
    		}
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
	 * Get external supported features
	 * 
	 * @return List of tags
	 */
	public static List<String> getExternalSupportedFeatures() {
		PackageManager packageManager = AndroidFactory.getApplicationContext().getPackageManager();
		Intent intent = new Intent(CapabilityApiIntents.RCS_EXTENSIONS);
		intent.setType(FEATURE_RCSE_EXTENSION + "/*");
		
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0; i < list.size(); i++) {
			ResolveInfo info = list.get(i);
			result.add(info.activityInfo.name);
		}
		return result;
	}	    
}
