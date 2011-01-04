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
import com.orangelabs.rcs.service.api.client.IMediaEventListener;
import com.orangelabs.rcs.service.api.client.MediaSample;
import com.orangelabs.rcs.service.api.client.IMediaRenderer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Video renderer (only H.263 176x144 frames are supported)
 * 
 * @author jexa7410
 */
public class VideoRenderer extends IMediaRenderer.Stub {	
	/**
	 * Video surface
	 */
	private static VideoSurfaceView surface;
	
	/**
	 * Video properties
	 */
	private final static int videoWidth = 176;
	private final static int videoHeight = 144;
	
	/**
	 * Video frames
	 */
	private int decoded[];
	private Bitmap converted ;
	
	/**
	 * Renderer event listeners
	 */
	private Vector<IMediaEventListener> listeners = new Vector<IMediaEventListener>();
	
	/**
	 * Player status
	 */
	private boolean started = false; 

	/**
	 * Recording start time 
	 */
    private long recordingStartTime = 0L;


    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 */
	public VideoRenderer() {
		decoded = new int[videoWidth*videoHeight];
		converted = Bitmap.createBitmap(videoWidth, videoHeight, Bitmap.Config.RGB_565);
		if (logger.isActivated()) {
			logger.debug("Video renderer has been created");
		}
	}
	
	/**
	 * Set the surface to render video
	 * @param renderer
	 */
	public void setRenderer(VideoSurfaceView renderer) {
		surface = renderer;
	}
	
	/**
	 * Is renderer started
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
		return recordingStartTime;
	}

	/**
	 * Open the renderer
	 */
	public void open() {
		try {
	        // Init the video decoder: 176x144 by default
        	int result = NativeH263Decoder.InitDecoder(videoWidth,videoHeight); 
        	if (result == 0) {
        		if (logger.isActivated()) {
        			logger.error("Init video decoder has failed: error code " + result);
        		}
        	} else {
	    		if (logger.isActivated()) {
	    			logger.debug("Video decoder initialized");
	    		}
        	}
        } catch (UnsatisfiedLinkError e) {
    		if (logger.isActivated()) {
    			logger.error("Video encoder initialization has failed");
    		}
		}
	}
	  
	/**
	 * Close the renderer
	 */
	public void close() {
    	try {
            // Deallocate the video decoder
    		if (NativeH263Decoder.DeinitDecoder()==0){
    			if (logger.isActivated()) {
        			logger.error("Video decoder could not be deallocated");
        		}
    		}
		} catch(UnsatisfiedLinkError e) {
    		if (logger.isActivated()) {
    			logger.error("Can't deallocate correctly the video decoder");
    		}
		}        
	}

	/**
	 * Start the player
	 */
	public void start() {
		started = true; 
		recordingStartTime = SystemClock.uptimeMillis();
	}
	  
	/**
	 * Stop the renderer
	 */
	public void stop() {
		started = false; 
		recordingStartTime = 0L;
	}
	
	/**
	 * Write a media sample
	 * 
	 * @param sample Sample
	 */
	public void writeSample(MediaSample sample) {
		if (!started) return;	
		if (NativeH263Decoder.DecodeAndConvert(sample.getData(),decoded,sample.getTimestamp())== 1) {
			converted.setPixels(decoded, 0, videoWidth, 0, 0, videoWidth, videoHeight);
			if (converted != null && surface != null) {
				surface.setImage(converted);		
			}
		}
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
}
