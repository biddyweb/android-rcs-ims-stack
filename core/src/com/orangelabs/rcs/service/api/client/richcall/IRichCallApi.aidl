package com.orangelabs.rcs.service.api.client.richcall;

import com.orangelabs.rcs.service.api.client.richcall.IVideoSharingSession;
import com.orangelabs.rcs.service.api.client.richcall.IImageSharingSession;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;

/**
 * Rich call API
 */
interface IRichCallApi {

	// Get the remote phone number involved in the current call
	String getRemotePhoneNumber();

	// Initiate a live video sharing session
	IVideoSharingSession initiateLiveVideoSharing(in String contact, in IMediaPlayer player);

	// Initiate a pre-recorded video sharing session
	IVideoSharingSession initiateVideoSharing(in String contact, in String file, in IMediaPlayer player);

	// Get current video sharing session from its session ID
	IVideoSharingSession getVideoSharingSession(in String id);

	// Initiate an image sharing session
	IImageSharingSession initiateImageSharing(in String contact, in String file);

	// Get current image sharing session from its session ID
	IImageSharingSession getImageSharingSession(in String id);

	// Set multiparty call
	void setMultiPartyCall(in boolean flag);

	// Set call hold
	void setCallHold(in boolean flag);
}


