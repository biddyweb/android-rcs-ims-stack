package com.orangelabs.rcs.service.api.client.presence;

/**
 * Presence API intents 
 * 
 * @author jexa7410
 */
public interface PresenceApiIntents {
    /**
     * Intent broadcasted when a presence sharing invitation has been received
     */
	public final static String PRESENCE_INVITATION = "com.orangelabs.rcs.presence.PRESENCE_SHARING_INVITATION";
	
    /**
     * Intent broadcasted when my presence info has changed
     */
    public final static String MY_PRESENCE_INFO_CHANGED = "com.orangelabs.rcs.presence.MY_PRESENCE_INFO_CHANGED";

    /**
     * Intent broadcasted when my presence status has changed
     */
    public final static String MY_PRESENCE_STATUS_CHANGED = "com.orangelabs.rcs.presence.MY_PRESENCE_STATUS_CHANGED";

    /**
     * Intent broadcasted when a contact info has changed
     */
    public final static String CONTACT_INFO_CHANGED = "com.orangelabs.rcs.presence.CONTACT_INFO_CHANGED";

    /**
     * Intent broadcasted when a contact photo-icon has changed
     */
    public final static String CONTACT_PHOTO_CHANGED = "com.orangelabs.rcs.presence.CONTACT_PHOTO_CHANGED";

    /**
     * Intent broadcasted when a presence sharing info has changed
     */
    public final static String PRESENCE_SHARING_CHANGED = "com.orangelabs.rcs.presence.PRESENCE_SHARING_CHANGED";
}
