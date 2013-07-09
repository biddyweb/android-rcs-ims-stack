/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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

package com.orangelabs.rcs.service.api.client.media.audio;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.orangelabs.rcs.core.ims.protocol.rtp.DummyPacketGenerator;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRtpReceiver;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.audio.AudioFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaException;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaOutput;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaSample;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.RtpStreamListener;
import com.orangelabs.rcs.platform.network.DatagramConnection;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.utils.CodecsUtils;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.logger.Logger;

import com.orangelabs.rcs.service.api.client.media.IAudioEventListener;
import com.orangelabs.rcs.service.api.client.media.IAudioRenderer;
import com.orangelabs.rcs.service.api.client.media.MediaCodec;

/**
 * Audio RTP renderer based on AMR WB format
 *
 * @author jexa7410
 */
public class AudioRenderer extends IAudioRenderer.Stub implements RtpStreamListener {

    /**
     * List of supported audio codecs
     */
    private MediaCodec[] supportedAudioCodecs = null;

    /**
     * Selected audio codec
     */
    private AudioCodec selectedAudioCodec = null;
    
    /**
     * Audio format
     */
    private AudioFormat audioFormat;

    /**
     * Local RTP port
     */
    private int localRtpPort;

    /**
     * RTP receiver session
     */
    private MediaRtpReceiver rtpReceiver = null;

    /**
     * RTP dummy packet generator
     */
    private DummyPacketGenerator rtpDummySender = null;

    /**
     * RTP media output
     */
    private MediaRtpOutput rtpOutput = null;
    
    /**
     * Local socket sender
     */
    private LocalSocket localSocketSender;
    
    /**
     * Local socket receiver
     */
    private LocalSocket localSocketReceiver;
    
    /**
     * Local socket
     */
    private LocalServerSocket localServerSocket;
    
    /**
     * Local socket endpoint
     */    
    private static final String LOCAL_SOCKET = "com.orangelabs.rcs.service.api.client.media.audio.socket.renderer";    

    /**
     * Is player opened
     */
    private boolean opened = false;

    /**
     * Is player started
     */
    private boolean started = false;

    /**
     * Audio start time
     */
    private long audioStartTime = 0L;

    /**
     * Audio event listeners
     */
    private Vector<IAudioEventListener> listeners = new Vector<IAudioEventListener>();

    /**
     * Temporary connection to reserve the port
     */
    private DatagramConnection temporaryConnection = null;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     */
    public AudioRenderer() {
        // Set the local RTP port
        localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
        reservePort(localRtpPort);

        // Init codecs
        supportedAudioCodecs = CodecsUtils.getSupportedAudioCodecList();   

        // Set the default audio codec
        if (supportedAudioCodecs.length > 0) {
            setAudioCodec(supportedAudioCodecs[0]);
        }
        
        logger.info("AudioRenderer constructor : reserve local RTP port("+localRtpPort+"), init codec and set audiocodec("+supportedAudioCodecs[0].getCodecName()+")");
    }

    /**
     * Constructor with a list of video codecs
     *
     * @param codecs Ordered list of codecs (preferred codec in first)
     */
    public AudioRenderer(MediaCodec[] codecs) {
        // Set the local RTP port
        localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
        reservePort(localRtpPort);

        // Init codecs
        supportedAudioCodecs = codecs;

        // Set the default media codec
        if (supportedAudioCodecs.length > 0) {
            setAudioCodec(supportedAudioCodecs[0]);
        }
    }

    /**
     * Return the audio start time
     *
     * @return Milliseconds
     */
    public long getAudioStartTime() {
        return audioStartTime;
    }

    /**
     * Returns the local RTP port
     *
     * @return Port
     */
    public int getLocalRtpPort() {
        return localRtpPort;
    }

    /**
     * Reserve a port
     *
     * @param port Port to reserve
     */
    private void reservePort(int port) {
        if (temporaryConnection == null) {
            try {
                temporaryConnection = NetworkFactory.getFactory().createDatagramConnection();
                temporaryConnection.open(port);
            } catch (IOException e) {
                temporaryConnection = null;
            }
        }
    }

    /**
     * Release the reserved port.
     */
    private void releasePort() {
        if (temporaryConnection != null) {
            try {
                temporaryConnection.close();
            } catch (IOException e) {
                temporaryConnection = null;
            }
        }
    }

    /**
     * Is player opened
     *
     * @return Boolean
     */
    public boolean isOpened() {
        return opened;
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
     * Open the renderer
     *
     * @param remoteHost Remote host
     * @param remotePort Remote port
     */
    public void open(String remoteHost, int remotePort) {
    	
    	if (logger.isActivated()) {
    		logger.info("AudioRenderer open : check codec and init RTP layer");
    	}
    	
        if (opened) {
            // Already opened
        	if (logger.isActivated()) {
        		logger.info("AudioRenderer already opened");
        	}
            return;
        }

        // Check audio codec
        if (selectedAudioCodec == null) {
            notifyPlayerEventError("Audio Codec not selected");
            return;
        }

        if (logger.isActivated()) {
        	logger.info("AudioRenderer open : init RTP layer with host:"+remoteHost+" and port:"+remotePort);
        }
        
        try {
            // Init the RTP layer
            releasePort();
            rtpReceiver = new MediaRtpReceiver(localRtpPort);
            rtpDummySender = new DummyPacketGenerator();
            rtpOutput = new MediaRtpOutput();
            rtpOutput.open();
            // set orientation parameter to 0
            rtpReceiver.prepareSession(remoteHost, remotePort, rtpOutput, audioFormat, this);   
            rtpDummySender.prepareSession(remoteHost, remotePort, rtpReceiver.getInputStream());
            rtpDummySender.startSession();
        } catch (Exception e) {
        	if (logger.isActivated()) {
        		logger.warn("AudioRenderer init RTP layer failed");
        	}
            notifyPlayerEventError(e.getMessage());
            return;
        }

        // Player is opened
        opened = true;
        notifyPlayerEventOpened();
    }

    /**
     * Close the renderer
     */
    public void close() {
    	
    	if (logger.isActivated()) {
    		logger.info("AudioRenderer close : close RTP layer (rtpOutput close and rtpReceiver stop) and notify");
    	}
    	
        if (!opened) {
            // Already closed
        	if (logger.isActivated()) {
        		logger.info("AudioRenderer already closed");
        	}
            return;
        }

        // Close the RTP layer
        rtpOutput.close();
        rtpReceiver.stopSession();
        rtpDummySender.stopSession();

        // Player is closed
        opened = false;
        notifyPlayerEventClosed();
    }

    /**
     * Start the player
     */
    public void start() {
    	
    	if (logger.isActivated()) {
    		logger.info("AudioRenderer start : start RTP layer (rtpReceiver start)");
    	}
    	
        if (!opened) {
            // Player not opened
        	if (logger.isActivated()) {
        		logger.info("AudioRenderer not opened");
        	}
            return;
        }

        if (started) {
            // Already started
        	if (logger.isActivated()) {
        		logger.info("AudioRenderer already started");
        	}
            return;
        }

        // Start RTP layer
        rtpReceiver.startSession();

        // Renderer is started
        audioStartTime = SystemClock.uptimeMillis();
        started = true;
        notifyPlayerEventStarted();
    }

    /**
     * Stop the renderer
     */
    public void stop() {
    	
    	if (logger.isActivated()) {
    		logger.info("AudioRenderer stop : stop RTP layer (rtpReceiver stop and rtpOutput close)");
    	}
    	
        if (!started) {
        	if (logger.isActivated()) {
        		logger.info("not started");
        	}
            return;
        }

        // Stop RTP layer
        if (rtpReceiver != null) {
            rtpReceiver.stopSession();
        }
        if (rtpDummySender != null) {
            rtpDummySender.stopSession();
        }
        if (rtpOutput != null) {
            rtpOutput.close();
        }

        // Renderer is stopped
        started = false;
        audioStartTime = 0L;
        notifyPlayerEventStopped();
    }

    /**
     * Add a audio event listener
     *
     * @param listener Audio event listener
     */
    public void addListener(IAudioEventListener listener) {
        listeners.addElement(listener);
    }

    /**
     * Remove all audio event listeners
     */
    public void removeAllListeners() {
        listeners.removeAllElements(); 
    }

    /**
     * Get supported audio codecs
     *
     * @return audio Codecs list
     */
    public MediaCodec[] getSupportedAudioCodecs() {
        return supportedAudioCodecs;
    }

    /**
     * Get audio codec
     *
     * @return Audio codec
     */
    public MediaCodec getAudioCodec() {
        if (selectedAudioCodec == null)
            return null;
        else
            return selectedAudioCodec.getMediaCodec();
    }

    /**
     * Set audio codec
     *
     * @param mediaCodec Audio codec
     */
    public void setAudioCodec(MediaCodec mediaCodec) {
        if (AudioCodec.checkAudioCodec(supportedAudioCodecs, new AudioCodec(mediaCodec))) {
            selectedAudioCodec = new AudioCodec(mediaCodec);
            audioFormat = (AudioFormat) MediaRegistry.generateFormat(mediaCodec.getCodecName());
        } else {
            notifyPlayerEventError("Codec not supported");
        }
    }

    /**
     * Notify RTP aborted
     */
    public void rtpStreamAborted() {
        notifyPlayerEventError("RTP session aborted");
    }

    /**
     * Notify player event started
     */
    private void notifyPlayerEventStarted() {
        if (logger.isActivated()) {
            logger.debug("Player is started");
        }
        Iterator<IAudioEventListener> ite = listeners.iterator();
        while (ite.hasNext()) {
            try {
                ((IAudioEventListener)ite.next()).audioStarted();
            } catch (RemoteException e) {
                if (logger.isActivated()) {
                    logger.error("Can't notify listener", e);
                }
            }
        }
    }

    /**
     * Notify player event stopped
     */
    private void notifyPlayerEventStopped() {
        if (logger.isActivated()) {
            logger.debug("Player is stopped");
        }
        Iterator<IAudioEventListener> ite = listeners.iterator();
        while (ite.hasNext()) {
            try {
                ((IAudioEventListener)ite.next()).audioStopped();
            } catch (RemoteException e) {
                if (logger.isActivated()) {
                    logger.error("Can't notify listener", e);
                }
            }
        }
    }

    /**
     * Notify player event opened
     */
    private void notifyPlayerEventOpened() {
        if (logger.isActivated()) {
            logger.debug("Player is opened");
        }
        Iterator<IAudioEventListener> ite = listeners.iterator();
        while (ite.hasNext()) {
            try {
                ((IAudioEventListener)ite.next()).audioOpened();
            } catch (RemoteException e) {
                if (logger.isActivated()) {
                    logger.error("Can't notify listener", e);
                }
            }
        }
    }

    /**
     * Notify player event closed
     */
    private void notifyPlayerEventClosed() {
        if (logger.isActivated()) {
            logger.debug("Player is closed");
        }
        Iterator<IAudioEventListener> ite = listeners.iterator();
        while (ite.hasNext()) {
            try {
                ((IAudioEventListener)ite.next()).audioClosed();
            } catch (RemoteException e) {
                if (logger.isActivated()) {
                    logger.error("Can't notify listener", e);
                }
            }
        }
    }

    /**
     * Notify player event error
     */
    private void notifyPlayerEventError(String error) {
        if (logger.isActivated()) {
            logger.debug("Renderer error: " + error);
        }

        Iterator<IAudioEventListener> ite = listeners.iterator();
        while (ite.hasNext()) {
            try {
                ((IAudioEventListener)ite.next()).audioError(error);
            } catch (RemoteException e) {
                if (logger.isActivated()) {
                    logger.error("Can't notify listener", e);
                }
            }
        }
    }

    /**
     * Media RTP output
     */
    private class MediaRtpOutput implements MediaOutput {

        /**f
         * Constructor
         */
        public MediaRtpOutput() {
        	if (logger.isActivated()) {
        		logger.info("Create the rtp output stream");
        	}
        }

        /**
         * Open the renderer
         */
        public void open() {
            // Nothing to do
        	if (logger.isActivated()) {
        		logger.info("Open the rtp output stream");
        	}
        }

        /**
         * Close the renderer
         */
        public void close() {
        	if (logger.isActivated()) {
        		logger.info("close the rtp output stream");
        	}
        }

		@Override
		public void writeSample(MediaSample sample) throws MediaException {
			rtpDummySender.incomingStarted();

            // Get the audio sample here from the RTP => feed the local server socket
        	if (logger.isActivated()) {
        		logger.info("writeSample - get bytes : " + sample.getData().length);
        	}
			
		}
    }
    
    /**
     * Thread that listen from local socket connection and read bytes from it
     *
     */
    class AudioRendererSocketListener extends Thread {
        
        public AudioRendererSocketListener() {
        	//logger.warn("create SocketListener"); 
        }
        
        @Override
        public void run() {

            try {
                localServerSocket = new LocalServerSocket(LOCAL_SOCKET);
                //logger.warn("create localServerSocket");
                //logger.warn("---");
                while (true) {
                    localSocketReceiver = localServerSocket.accept();
                    //logger.warn("accept localSocketReceiver");               
                    if (localSocketReceiver != null) {
                        
                        // Reading bytes from the socket
                    	//logger.warn("reading inputstream");
            	 		InputStream in = localSocketReceiver.getInputStream();

//            	    	String externalStorageRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//            	    	String uniqueOutFile = externalStorageRootPath + "/TEST" + System.currentTimeMillis() + ".3gp";
//            	    	FileOutputStream fop = new FileOutputStream(uniqueOutFile);
//            	    	logger.warn("writing to file : "+uniqueOutFile);

            	        int len = 0; 
            	        byte[] buffer = new byte[1024];
            	        
            	        while ((len = in.read(buffer)) >= 0) {
            	        	//logger.warn("writing in rtpinput : "+len+" bytes");
            	        	//logger.warn("buffer content : "+buffer);            	        	
            	        	//logger.warn("in file");
            	        	//fop.write(buffer, 0, len);            	            
            	        }
            	        //logger.warn("stop reading inputstream");
                    }
                }
            } catch (IOException e) {
                Log.e(getClass().getName(), e.getMessage());
            }
        }
    }

}

