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
package com.orangelabs.rcs.core.media;


/**
 * Media renderer (e.g. screen, headset) that permits to show media
 * 
 * @author jexa7410
 */
public abstract class MediaRenderer {
	/**
	 * Constructor
	 */
	public MediaRenderer() {
    }	
	
	/**
	 * Add a listener
	 * 
	 * @param listener Media listener
	 */
	public abstract void addListener(MediaListener listener);
	
	/**
	 * Open the renderer
	 * 
	 * @throws MediaException
	 */
	public abstract void open() throws MediaException;
	
	/**
	 * Close the renderer
	 */
	public abstract void close();

	/**
	 * Start the renderer
	 * 
	 * @throws MediaException
	 */
	public abstract void start() throws MediaException;
	
	/**
	 * Stop the renderer
	 */
	public abstract void stop();

	/**
	 * Write a media sample
	 * 
	 * @param sample Media sample
	 * @throws MediaException
	 */
	public abstract void writeSample(MediaSample sample) throws MediaException;
}
