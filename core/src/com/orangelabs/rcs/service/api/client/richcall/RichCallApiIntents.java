package com.orangelabs.rcs.service.api.client.richcall;

/**
 * Rich call API intents
 * 
 * @author jexa7410
 */
public class RichCallApiIntents {
	/**
     * Intent broadcasted when content sharing capabilities have been exchanged
     */
	public final static String SHARING_CAPABILITIES = "com.orangelabs.rcs.richcall.CONTENT_SHARING_CAPABILITIES";

	/**
     * Intent broadcasted when a new image sharing invitation has been received
     */
	public final static String IMAGE_SHARING_INVITATION = "com.orangelabs.rcs.richcall.IMAGE_SHARING_INVITATION";

    /**
     * Intent broadcasted when a new video sharing invitation has been received
     */
	public final static String VIDEO_SHARING_INVITATION = "com.orangelabs.rcs.richcall.VIDEO_SHARING_INVITATION";
}
