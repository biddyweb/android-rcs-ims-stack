package com.orangelabs.rcs.core.ims.network.sip;

/**
 * Feature tags
 * 
 * @author jexa7410
 */
public class FeatureTags {
	/**
	 * OMA IM feature tag
	 */
	public final static String FEATURE_OMA_IM = "+g.oma.sip-im";

	/**
	 * 3GPP video share feature tag
	 */
	public final static String FEATURE_3GPP_VIDEO_SHARE = "+g.3gpp.cs-voice";

	/**
     * 3GPP image share feature tag
     */
    public final static String FEATURE_3GPP_IMAGE_SHARE = "+g.3gpp.app_ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-is\"";	
	
	/**
	 * RCS-e feature tag prefix
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
	public final static String FEATURE_RCSE_EXTENSION = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcse";
}
