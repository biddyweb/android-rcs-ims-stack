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
package com.orangelabs.rcs.core.ims.service.sharing.streaming;

import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingSession;
import com.orangelabs.rcs.core.media.MediaRenderer;

/**
 * Content sharing streaming session
 * 
 * @author jexa7410
 */
public abstract class ContentSharingStreamingSession extends ContentSharingSession {
    /**
	 * Media renderer
	 */
	private MediaRenderer renderer = null;
	
    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param content Content to be shared
	 * @param contact Remote contact
	 */
	public ContentSharingStreamingSession(ImsService parent, MmContent content, String contact) {
		super(parent, content, contact);
	}
	
	/**
	 * Get the media renderer
	 * 
	 * @return Renderer
	 */
	public MediaRenderer getMediaRenderer() {
		return renderer;
	}
	
	/**
	 * Set the media renderer
	 * 
	 * @param renderer Renderer
	 */
	public void setMediaRenderer(MediaRenderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * Returns the event listener
	 * 
	 * @return Listener
	 */
	public ContentSharingStreamingSessionListener getListener() {
		return (ContentSharingStreamingSessionListener)super.getListener();
	}
}
