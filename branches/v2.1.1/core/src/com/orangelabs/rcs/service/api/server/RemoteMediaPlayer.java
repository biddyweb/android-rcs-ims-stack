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
package com.orangelabs.rcs.service.api.server;

import android.os.RemoteException;

import com.orangelabs.rcs.core.media.MediaException;
import com.orangelabs.rcs.core.media.MediaListener;
import com.orangelabs.rcs.core.media.MediaPlayer;
import com.orangelabs.rcs.core.media.MediaSample;
import com.orangelabs.rcs.service.api.client.IMediaPlayer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Remote media player
 * 
 * @author jexa7410
 */
public class RemoteMediaPlayer extends MediaPlayer {
	/**
	 * Remote media player
	 */
	private IMediaPlayer remote;
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param remote Remote player
	 */
	public RemoteMediaPlayer(IMediaPlayer remote) {
		this.remote = remote;
	}
	
	/**
	 * Open the player
	 * 
	 * @throws MediaException
	 */
	public void open() throws MediaException {
		try {
			remote.open();
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't open the media player", e);
			}
			throw new MediaException("Can't open the remote media player");
		}
	}
	
	/**
	 * Close the player
	 */
	public void close() {
		try {
			remote.close();
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't close the media player", e);
			}
		}
	}

	/**
	 * Start the player
	 * 
	 * @throws MediaException
	 */
	public void start() throws MediaException {
		try {
			remote.start();
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't start the media player", e);
			}
			throw new MediaException("Can't start the media player");
		}
	}
	
	/**
	 * Stop the player
	 */
	public void stop() {
		try {
			remote.stop();
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't stop the media player", e);
			}
		}
	}

	/**
	 * Read a media sample (blocking method)
	 * 
	 * @return Media sample
	 * @throws MediaException
	 */
	public MediaSample readSample() throws MediaException {
		try {
			com.orangelabs.rcs.service.api.client.MediaSample sample = remote.readSample();
			return new MediaSample(sample.getData(), sample.getTimestamp());
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't read media sample", e);
			}
			throw new MediaException("Can't read media sample");
		}
	}

	/**
	 * Add a media listener
	 * 
	 * @param listener Media listener
	 */
	public void addListener(MediaListener listener) {
		try {
			remote.addListener(new RemoteMediaListener(listener));
		} catch (RemoteException e) {
			if (logger.isActivated()) {
				logger.error("Can't add listener", e);
			}
		}
	}
}
