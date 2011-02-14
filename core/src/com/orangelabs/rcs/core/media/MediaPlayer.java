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
 * Media player (e.g. camera, microphone) that permits to generate media
 * 
 * @author jexa7410
 */
public abstract class MediaPlayer {
	
	/**
	 * Constructor
	 */
	public MediaPlayer() {
    }	

	/**
	 * Add a listener
	 * 
	 * @param listener Media listener
	 */
	public abstract void addListener(MediaListener listener);
	
	/**
	 * Open the player
	 * 
	 * @throws MediaException
	 */
	public abstract void open() throws MediaException;
	
	/**
	 * Close the player
	 */
	public abstract void close();

	/**
	 * Start the player
	 * 
	 * @throws MediaException
	 */
	public abstract void start() throws MediaException;
	
	/**
	 * Stop the player
	 */
	public abstract void stop();

	/**
	 * Read a media sample (blocking method)
	 * 
	 * @return Media sample
	 * @throws MediaException
	 */
	public abstract MediaSample readSample() throws MediaException;
}
