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
package com.orangelabs.rcs.samples.sharing;

import java.util.Vector;

import android.graphics.Bitmap;
import android.os.SystemClock;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.decoder.NativeH263Decoder;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.decoder.VideoSample;
import com.orangelabs.rcs.service.api.client.IMediaEventListener;
import com.orangelabs.rcs.service.api.client.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.MediaSample;
import com.orangelabs.rcs.utils.FifoBuffer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Pre-recorded video player (only H.263 176x144 frames are supported)
 * 
 * @author jexa7410
 */
public class PrerecordedVideoPlayer extends IMediaPlayer.Stub {
	/**
	 * List of received frames
	 */
	private FifoBuffer fifo = new FifoBuffer();
	
	/**
	 * Player event listeners
	 */
	private Vector<IMediaEventListener> listeners = new Vector<IMediaEventListener>();
	
	/**
	 * Player status
	 */
	private boolean started = false; 

	/**
	 * Play start time 
	 */
    private long playStartTime = 0L;
    
    /**
     * File to play
     */
    private String filename;
    
    /**
     * Surface renderer
     */
    private VideoSurfaceView surface;
    
    /**
	 * Video preview event listener
	 */
	private PrerecordedVideoPreviewListener listener;
    
	/**
	 * Video properties
	 */
	private long duration;
	private int width;
	private int height;
	
	/**
	 * Stop playing thread
	 */
	private boolean stopPlaying = false;
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
	/**
	 * Constructor
	 */
	public PrerecordedVideoPlayer(String _filename, VideoSurfaceView _surface, PrerecordedVideoPreviewListener _listener){
		filename = _filename;
		surface = _surface;
		listener = _listener;
		if (logger.isActivated()) {
			logger.debug("Pre-recorded video player has been created");
		}
	}

	/**
	 * Get video duration
	 * 
	 * @return Milliseconds
	 */
	public long getVideoDuration() {
		return duration;
	}
	
	/**
	 * Is player started
	 * 
	 * @return Boolean
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * Return the recorded start time
	 * 
	 * @return Milliseconds
	 */
	public long getRecordingStartTime() {
		return playStartTime;
	}

	/**
	 * Add a new video sample
	 * 
	 * @param data Data
	 * @param timestamp Timestamp
	 */
	public void addSample(byte[] data, long timestamp) {
		MediaSample sample = new MediaSample(data, timestamp);
		fifo.addObject(sample);
	}

	/**
	 * Open the player
	 */
	public void open() {
		try {
            // Init file reader
        	int result = NativeH263Decoder.InitParser(filename);
        	if (result == 0) {
        		if (logger.isActivated()) {
        			logger.error("Init 3GPP file reader has failed: error code " + result);
        		}
        	} else {
	    		if (logger.isActivated()) {
	    			logger.info("3GPP file reader initialized");
	    		}
        	}
        	
        	// Get video properties
        	duration = NativeH263Decoder.getVideoLength();
        	width = NativeH263Decoder.getVideoWidth();
        	height = NativeH263Decoder.getVideoHeight();
    		if (logger.isActivated()) {
    			logger.debug("Video properties | filename: "+filename+" | duration: "+duration+ " | "+width+"x"+height);
    		}
        } catch(UnsatisfiedLinkError e) {
    		if (logger.isActivated()) {
    			logger.error("3GPP file reader initialization has failed");
    		}
		}
	}
	  
	/**
	 * Close the player
	 */
	public void close() {
		try {
            // Deallocate the 3GPP file reader
    		NativeH263Decoder.DeinitParser();
		} catch(UnsatisfiedLinkError e) {
    		if (logger.isActivated()) {
    			logger.error("Can't deallocate correctly the 3GPP file reader");
    		}
		}       
	}
	  
	/**
	 * Start the player
	 */
	public void start() {
		started = true;
		t.start();
		playStartTime = SystemClock.uptimeMillis();
	}
	  
	/**
	 * Stop the player
	 */
	public void stop() {
		stopPlaying = true;
		boolean retry = true;
		while (retry){
			try {
				t.join();
				retry = false;
			} catch (InterruptedException e) {}
		}
		started = false; 
		playStartTime = 0L;
	}
	
	/**
	 * Read a media sample
	 * 
	 * @return Sample
	 */
	public MediaSample readSample() {
		MediaSample sample = (MediaSample)fifo.getObject();
		return sample;
	}

	/**
	 * Add a media event listener
	 * 
	 * @param listener Media event listener
	 */
	public void addListener(IMediaEventListener listener) {
		listeners.addElement(listener);
	}

	/**
	 * Remove all listeners
	 */
	public void removeAllListeners() {
		listeners.removeAllElements();
	}

	/**
	 * Background thread
	 */
	private Thread t = new Thread() {
		public void run() {
			if (logger.isActivated()) {
				logger.debug("Background thread started");
			}
			long totalDuration = 0;
			boolean endOfMedia = false;

			Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			int[] Decoded = new int[width * height];
			VideoSample sample;
			long time, delta;

			time = System.currentTimeMillis();
			while (!endOfMedia && !stopPlaying) {
				sample = NativeH263Decoder.getVideoSample(Decoded);
				if (sample != null) {
					image.setPixels(Decoded, 0, width, 0, 0, width, height);
					totalDuration = totalDuration + sample.timestamp;
					addSample(sample.data, time);
					listener.updateDuration(totalDuration);
					// Wait before next frame
					try {
						delta = System.currentTimeMillis() - time;
						if (delta < sample.timestamp)
							sleep(sample.timestamp - delta);
					} catch (InterruptedException e) {
						endOfMedia = true;
					} finally {
						surface.setImage(image);
						time = System.currentTimeMillis();
					}
				} else {
					if (logger.isActivated()) {
						logger.debug("End of media");
					}
					// Notify listener
					listener.endOfStream();
					endOfMedia = true;
				}
			}
			stopPlaying = false;
		}
	};
}
