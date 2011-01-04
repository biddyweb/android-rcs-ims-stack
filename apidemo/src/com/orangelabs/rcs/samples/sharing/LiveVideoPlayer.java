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

import java.util.Iterator;
import java.util.Vector;

import android.hardware.Camera;
import android.os.RemoteException;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.encoder.NativeH263Encoder;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.encoder.NativeH263EncoderParams;
import com.orangelabs.rcs.service.api.client.IMediaEventListener;
import com.orangelabs.rcs.service.api.client.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.MediaSample;
import com.orangelabs.rcs.utils.FifoBuffer;

/**
 * Live video player (only H.263 176x144 frames are supported)
 */
public class LiveVideoPlayer extends IMediaPlayer.Stub implements Camera.PreviewCallback {
	/**
	 * List of received frames
	 */
	private FifoBuffer fifo = new FifoBuffer();
	
	/**
	 * Last frame captured
	 */
	private CameraFrame frameBuffer = new CameraFrame();
	
	/**
	 * Encoder options
	 */
	private NativeH263EncoderParams params = new NativeH263EncoderParams();
	
	/**
	 * Video size properties
	 */
	private final static int videoWidth = 176;
	private final static int videoHeight = 144;
	private final static int frameRate = 8;
	private final static int bitrate = 128000;
	private long timeStamp = 0;
		
	/**
	 * Player event listeners
	 */
	private Vector<IMediaEventListener> listeners = new Vector<IMediaEventListener>();
	
	/**
	 * Player status
	 */
	private boolean started = false; 

	/**
	 * Constructor
	 */
	public LiveVideoPlayer() {		
		params.setEncFrameRate((float)frameRate);
		params.setIntraPeriod(-1);	
		params.setBitRate(bitrate);
		params.setTickPerSrc(params.getTimeIncRes()/frameRate);
		params.setNoFrameSkipped(false);
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
	 * Add a new video sample
	 * 
	 * @param data Data
	 * @param timestamp Timestamp
	 */
	private void addSample(byte[] data, long timestamp) {
		fifo.addObject(new MediaSample(data, timestamp));
	}

	/**
	 * Open the player
	 */
	public void open() {
		try {
			int returnCode;
        	if((returnCode=NativeH263Encoder.InitEncoder(params))!=1){
        		notifyError("Init failed with code: "+returnCode);
        	}
		} catch (UnsatisfiedLinkError e) {
        	notifyError(e.getMessage());
		}
	}
	  
	/**
	 * Close the player
	 */
	public void close() {
		try {
			NativeH263Encoder.DeinitEncoder();
		} catch(UnsatisfiedLinkError e) {
			notifyError(e.getMessage());
		}        
	}
	
	/**
	 * Start the player
	 */
	public void start() {
		started = true;
		frameThread.start();
		notifyStart();
	}
	  
	/**
	 * Stop the player
	 */
	public void stop() {
		started = false;
		try {
			frameThread.join();
		} catch(Exception e) {
			notifyError(e.getMessage());
		}
		notifyStop();
	}
	
	/**
	 * Preview frame from the camera
	 * 
	 * @param data Frame
	 * @param camera Camera
	 */
	public void onPreviewFrame(byte[] data, Camera camera) {
		frameBuffer.setFrame(data);				
	}
	
	/**
	 * Read a media sample
	 * 
	 * @return Sample
	 */
	public MediaSample readSample() {
		return (MediaSample)fifo.getObject();
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
	 * Notify start to listeners
	 */
	private void notifyStart() {
		Iterator<IMediaEventListener> ite = listeners.iterator();
		while (ite.hasNext()) {
			try {
				((IMediaEventListener)ite.next()).mediaStarted();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Notify stop to listeners
	 */
	private void notifyStop() {
		Iterator<IMediaEventListener> ite = listeners.iterator();
		while (ite.hasNext()) {
			try {
				((IMediaEventListener) ite.next()).mediaStopped();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Notify error to listeners
	 */
	private void notifyError(String error) {
		Iterator<IMediaEventListener> ite = listeners.iterator();
		while (ite.hasNext()) {
			try {
				((IMediaEventListener) ite.next()).mediaError(error);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Camera frame buffer
	 */
	private class CameraFrame {
		/**
		 * Current frame. YUV frame size is always (videoWidth*videoHeight*3)/2.
		 */
		byte frame[] = new byte[(videoWidth * videoHeight * 3) / 2];

		/**
		 * Update last captured frame
		 * 
		 * @param frame
		 *            New frame
		 */
		public void setFrame(byte[] frame) {
			this.frame = frame;
		}

		/**
		 * Return most recent frame
		 * 
		 * @return frame Last frame
		 */
		public byte[] getFrame() {
			return frame;
		}
	}

	/**
	 * Create thread to retreive frame
	 */
	private Thread frameThread = new Thread() {
		public void run() {
			int timeToSleep = 1000 / (frameRate);
			int timestampInc = 90000 / (frameRate);
			byte[] frameData;
			byte[] encodedFrame;
			long time = 0, delta = 0, encoderTS = 0;
			long oldTs = System.currentTimeMillis();

			while (started) {
				time = System.currentTimeMillis();
				encoderTS = encoderTS + (time - oldTs);
				frameData = frameBuffer.getFrame();
				encodedFrame = NativeH263Encoder.EncodeFrame(frameData,	encoderTS);
				if (encodedFrame.length > 0)
					addSample(encodedFrame, timeStamp += timestampInc);
				if ((delta = (System.currentTimeMillis() - time)) < timeToSleep) {
					try {
						sleep((timeToSleep - delta)
								- (((timeToSleep - delta) * 10) / 100));
					} catch (InterruptedException e) {
					}
				}
				oldTs = time;
			}
		}
	};
}
