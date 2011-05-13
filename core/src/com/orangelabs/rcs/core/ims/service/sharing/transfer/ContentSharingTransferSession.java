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

package com.orangelabs.rcs.core.ims.service.sharing.transfer;

import com.orangelabs.rcs.core.content.MmContent;
import com.orangelabs.rcs.core.ims.service.ImsService;
import com.orangelabs.rcs.core.ims.service.sharing.ContentSharingSession;

/**
 * Content sharing transfer session
 * 
 * @author jexa7410
 */
public abstract class ContentSharingTransferSession extends ContentSharingSession {
    /**
	 * Max content sharing size (in bytes)
	 */
	public final static int MAX_CONTENT_SIZE = 512 * 1024;
	
	/**
	 * Default SO_TIMEOUT value (in seconds)
	 */
	public final static int DEFAULT_SO_TIMEOUT = 30;
	
    /**
	 * Constructor
	 * 
	 * @param parent IMS service
	 * @param content Content to be shared
	 * @param contact Remote contact
	 */
	public ContentSharingTransferSession(ImsService parent, MmContent content, String contact) {
		super(parent, content, contact);
	}
	
	/**
	 * Returns the event listener
	 * 
	 * @return Listener
	 */
	public ContentSharingTransferSessionListener getListener() {
		return (ContentSharingTransferSessionListener)super.getListener();
	}
}
