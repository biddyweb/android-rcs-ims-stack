package com.orangelabs.rcs.service.api.client.ipcall;

/**
 * IP call event listener interface
 *
 * @author owom5460
 */
interface IIPCallEventListener {

	// Session is started
	void handleSessionStarted();

    // The incoming media video size has changed
    void handleMediaResized(in int width, in int height);

	// Session has been aborted
	void handleSessionAborted(in int reason);
    
	// Session has been terminated by remote
	void handleSessionTerminatedByRemote();
	
	// Add video invitation
	void handleAddVideoInvitation(in String videoEncoding, in int videoWidth, in int videoHeight);
	
	// Remove video invitation
	void handleRemoveVideoInvitation();
	
	// Add video has been accepted by user 
	void handleAddVideoAccepted();
	
	// Remove video has been accepted (200 OK response) 
	void handleRemoveVideoAccepted();
	
	// Add video has been declined by user or not answered
	void handleAddVideoAborted(in int errorCode);

	// Remove video aborted (No 200 OK response) 
	void handleRemoveVideoAborted(in int errorCode);

	// IP Call error
	void handleCallError(in int error);
	
	// Called user is Busy
	void handle486Busy();
}
