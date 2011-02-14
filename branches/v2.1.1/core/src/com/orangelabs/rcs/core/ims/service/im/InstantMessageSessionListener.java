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
package com.orangelabs.rcs.core.ims.service.im;

import java.util.Date;

import com.orangelabs.rcs.core.content.FileContent;
import com.orangelabs.rcs.core.content.PhotoContent;
import com.orangelabs.rcs.core.content.VideoContent;
import com.orangelabs.rcs.core.ims.service.ImsSessionListener;
import com.orangelabs.rcs.core.ims.service.im.InstantMessage;
import com.orangelabs.rcs.core.ims.service.im.InstantMessageError;

/**
 * IM session listener
 * 
 * @author JM. Auffret
 */
public interface InstantMessageSessionListener extends ImsSessionListener {
	/**
	 * New message received
	 * 
	 * @param message Message
	 */
    public void handleReceiveMessage(InstantMessage message);

    /**
     * Is composing event
     * 
     * @param contact Contact
     * @param status Status
     */
    public void handleContactIsComposing(String contact, boolean status);
    
    /**
     * New picture received
     * 
     * @param contact Remote contact
     * @param date Date of reception 
     * @param content Picture content 
     */
    public void handleReceivePicture(String contact, Date date, PhotoContent content);

    /**
     * New video received
     * 
     * @param contact Remote contact
     * @param date Date of reception
     * @param content Video content
     */
    public void handleReceiveVideo(String contact, Date date, VideoContent content);
    
    /**
     * New file received
     * 
     * @param contact Remote contact
     * @param date Date of reception
     * @param content File content
     */
    public void handleReceiveFile(String contact, Date date, FileContent content);
    
	/**
	 * Message has been transfered
	 */
    public void handleMessageTransfered();
    
    /**
     * IM error
     * 
     * @param error Error
     */
    public void handleImError(InstantMessageError error);
}
