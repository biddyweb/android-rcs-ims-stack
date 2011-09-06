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

package com.orangelabs.rcs.service.api.client.media.video;

import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRtpSender;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.H263Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.encoder.NativeH263Encoder;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.encoder.NativeH263EncoderParams;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.H264Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.encoder.NativeH264Encoder;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.H263VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.H264VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaException;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaInput;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaSample;
import com.orangelabs.rcs.platform.network.DatagramConnection;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.media.IMediaEventListener;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.MediaCodec;
import com.orangelabs.rcs.utils.FifoBuffer;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.logger.Logger;

import android.hardware.Camera;
import android.os.RemoteException;
import android.os.SystemClock;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 * Live RTP video player. Supports only H.263 and H264 QCIF formats.
 */
public class LiveVideoPlayer extends IMediaPlayer.Stub implements Camera.PreviewCallback {

    /**
     * Video codec
     */
    private VideoCodec mediaVideoCodec;

    /**
     * Supported video formats
     */
    private enum SupportedVideoFormats {
        H263, H264
    };

    /**
     * Local video format
     */
    private SupportedVideoFormats localVideoFormat;

    /**
     * Video format
     */
    private VideoFormat videoFormat;

    /**
     * Local RTP port
     */
    private int localRtpPort;

    /**
     * RTP sender session
     */
    private MediaRtpSender rtpSender = null;

    /**
     * RTP media input
     */
    private MediaRtpInput rtpInput = null;

    /**
     * Last video frame
     */
    private CameraBuffer frameBuffer;

    /**
     * H263 Video encoder parameters
     */
    private NativeH263EncoderParams params = new NativeH263EncoderParams();

    /**
     * Is player opened
     */
    private boolean opened = false;

    /**
     * Is player started
     */
    private boolean started = false;

    /**
     * Video start time
     */
    private long videoStartTime = 0L;

    /**
     * Media event listeners
     */
    private Vector<IMediaEventListener> listeners = new Vector<IMediaEventListener>();

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
    public LiveVideoPlayer() {
    	// Set video codec
    	setVideoCodec(RcsSettings.getInstance().getCShVideoFormat());

        // Set the local RTP port
        localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
        reservePort(localRtpPort);

        // Initialize frame buffer
        frameBuffer = new CameraBuffer();
    }

    /**
     * Constructor.
     * Force a video codec.
     *
     * @param codec Video codec
     */
    public LiveVideoPlayer(VideoCodec codec) {
        this.mediaVideoCodec = codec;

    	// Set video codec
    	setVideoCodec(codec.getCodecName());

        // Set the local RTP port
        localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
        reservePort(localRtpPort);

        // Initialize frame buffer
        frameBuffer = new CameraBuffer();
    }

    /**
     * Constructor. Force a video codec.
     * 
     * @param codec Video codec name
     */
    public LiveVideoPlayer(String codec) {
    	// Set video codec
    	setVideoCodec(codec);

        // Set the local RTP port
        localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
        reservePort(localRtpPort);

        // Initialize frame buffer
        frameBuffer = new CameraBuffer();
    }

    /**
     * Set video codec
     * 
     * @param codecName Codec name
     */
    private void setVideoCodec(String codecName) {
        if (codecName.equalsIgnoreCase(H264VideoFormat.ENCODING)) {
        	// H264
            localVideoFormat = SupportedVideoFormats.H264;
            videoFormat = (VideoFormat) MediaRegistry.generateFormat(H264VideoFormat.ENCODING);
            mediaVideoCodec = new VideoCodec(H264Config.CODEC_NAME, H264Config.CLOCK_RATE,
                    H264Config.CODEC_PARAMS, H264Config.FRAME_RATE, H264Config.BIT_RATE,
                    H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT);
        } else {
        	// Default H263
            localVideoFormat = SupportedVideoFormats.H263;
            videoFormat = (VideoFormat) MediaRegistry.generateFormat(H263VideoFormat.ENCODING);
            mediaVideoCodec = new VideoCodec(H263Config.CODEC_NAME, H263Config.CLOCK_RATE,
                    H263Config.CODEC_PARAMS, H263Config.FRAME_RATE, H263Config.BIT_RATE,
                    H263Config.VIDEO_WIDTH, H263Config.VIDEO_HEIGHT);

            // Set native video parameters
            params.setEncFrameRate(H263Config.FRAME_RATE);
            params.setBitRate(H263Config.BIT_RATE);
            params.setTickPerSrc(params.getTimeIncRes() / H263Config.FRAME_RATE);
            params.setIntraPeriod(-1);
            params.setNoFrameSkipped(false);
        }
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
     * Reserve a port.
     *
     * @param port the port to reserve
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
     * Return the video start time
     *
     * @return Milliseconds
     */
    public long getVideoStartTime() {
        return videoStartTime;
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
     * Open the player
     *
     * @param remoteHost Remote host
     * @param remotePort Remote port
     */
    public void open(String remoteHost, int remotePort) {
        if (opened) {
            // Already opened
            return;
        }

        // Init video encoder
        try {
            if (localVideoFormat == SupportedVideoFormats.H264) {
            	// H264
                NativeH264Encoder.InitEncoder(mediaVideoCodec.getWidth(),
                        mediaVideoCodec.getHeight(),
                        mediaVideoCodec.getFramerate());
                // TODO: To be analyzed: exit with 0 but it works...
                // if (result == 0) {
                //   notifyPlayerEventError("Encoder init failed with error code " + result);
                //   return;
                // }
            } else {
            	// Default H263
                int result = NativeH263Encoder.InitEncoder(params);
                if (result != 1) {
                    notifyPlayerEventError("Encoder init failed with error code " + result);
                    return;
                }
            }

        } catch (UnsatisfiedLinkError e) {
            notifyPlayerEventError(e.getMessage());
            return;
        }

        // Init the RTP layer
        try {
            releasePort();
            rtpSender = new MediaRtpSender(videoFormat, localRtpPort);
            rtpInput = new MediaRtpInput();
            rtpInput.open();
            rtpSender.prepareSession(rtpInput, remoteHost, remotePort);
        } catch (Exception e) {
            notifyPlayerEventError(e.getMessage());
            return;
        }

        // Player is opened
        opened = true;
        notifyPlayerEventOpened();
    }

    /**
     * Close the player
     */
    public void close() {
        if (!opened) {
            // Already closed
            return;
        }
        // Close the RTP layer
        rtpInput.close();
        rtpSender.stopSession();

        try {
            // Close the video encoder
            if (localVideoFormat == SupportedVideoFormats.H264) {
                NativeH264Encoder.DeinitEncoder();
            } else { // default H263
                NativeH263Encoder.DeinitEncoder();
            }
        } catch (UnsatisfiedLinkError e) {
            if (logger.isActivated()) {
                logger.error("Can't close correctly the video encoder", e);
            }
        }

        // Player is closed
        opened = false;
        notifyPlayerEventClosed();
    }

    /**
     * Start the player
     */
    public void start() {
        if (!opened) {
            // Player not opened
            return;
        }

        if (started) {
            // Already started
            return;
        }

        // Start RTP layer
        rtpSender.startSession();

        // Start capture
        captureThread.start();

        // Player is started
        videoStartTime = SystemClock.uptimeMillis();
        started = true;
        notifyPlayerEventStarted();
    }

    /**
     * Stop the player
     */
    public void stop() {
        if (!opened) {
            // Player not opened
            return;
        }

        if (!started) {
            // Already stopped
            return;
        }

        // Stop capture
        try {
            captureThread.interrupt();
        } catch (Exception e) {
        }

        // Player is stopped
        videoStartTime = 0L;
        started = false;
        notifyPlayerEventStopped();
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
     * Remove all media event listeners
     */
    public void removeAllListeners() {
        listeners.removeAllElements();
    }

    /**
     * Get media Codec
     * 
     * @return media Codec
     */
    public MediaCodec getMediaCodec() throws RemoteException {
        return mediaVideoCodec.getMediaCodec();
    }

    /**
     * Notify player event started
     */
    private void notifyPlayerEventStarted() {
        if (logger.isActivated()) {
            logger.debug("Player is started");
        }
        Iterator<IMediaEventListener> ite = listeners.iterator();
        while (ite.hasNext()) {
            try {
                ((IMediaEventListener)ite.next()).mediaStarted();
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
        Iterator<IMediaEventListener> ite = listeners.iterator();
        while (ite.hasNext()) {
            try {
                ((IMediaEventListener)ite.next()).mediaStopped();
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
        Iterator<IMediaEventListener> ite = listeners.iterator();
        while (ite.hasNext()) {
            try {
                ((IMediaEventListener)ite.next()).mediaOpened();
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
        Iterator<IMediaEventListener> ite = listeners.iterator();
        while (ite.hasNext()) {
            try {
                ((IMediaEventListener)ite.next()).mediaClosed();
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
            logger.debug("Player error: " + error);
        }

        Iterator<IMediaEventListener> ite = listeners.iterator();
        while (ite.hasNext()) {
            try {
                ((IMediaEventListener)ite.next()).mediaError(error);
            } catch (RemoteException e) {
                if (logger.isActivated()) {
                    logger.error("Can't notify listener", e);
                }
            }
        }
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
     * Camera buffer
     */
    private class CameraBuffer {
        /**
         * YUV frame where frame size is always (videoWidth*videoHeight*3)/2
         */
        private byte frame[] = new byte[(mediaVideoCodec.getWidth() * mediaVideoCodec.getHeight() * 3) / 2];

        /**
         * Set the last captured frame
         *
         * @param frame Frame
         */
        public void setFrame(byte[] frame) {
            this.frame = frame;
        }

        /**
         * Return the last captured frame
         *
         * @return Frame
         */
        public byte[] getFrame() {
            return frame;
        }
    }

    /**
     * Video capture thread
     */
    private Thread captureThread = new Thread() {
        /**
         * Timestamp
         */
        private long timeStamp = 0;

        /**
         * Processing
         */
        public void run() {
            if (rtpInput == null) {
                return;
            }

            int timeToSleep = 1000 / mediaVideoCodec.getFramerate();
            int timestampInc = 90000 / mediaVideoCodec.getFramerate();
            byte[] frameData;
            byte[] encodedFrame;
            long encoderTs = 0;
            long oldTs = System.currentTimeMillis();

            while (started) {
                // Set timestamp
                long time = System.currentTimeMillis();
                encoderTs = encoderTs + (time - oldTs);

                // Get data to encode
                frameData = frameBuffer.getFrame();

                // Encode frame
                if (localVideoFormat == SupportedVideoFormats.H264) {
                    encodedFrame = NativeH264Encoder.EncodeFrame(frameData, encoderTs);
                } else { // default H263
                    encodedFrame = NativeH263Encoder.EncodeFrame(frameData, encoderTs);
                }
                if (encodedFrame.length > 0) {
                    // Send encoded frame
                    rtpInput.addFrame(encodedFrame, timeStamp += timestampInc);
                }

                // Sleep between frames if necessary
                long delta = System.currentTimeMillis() - time;
                if (delta < timeToSleep) {
                    try {
                        Thread.sleep((timeToSleep - delta) - (((timeToSleep - delta) * 10) / 100));
                    } catch (InterruptedException e) {
                    }
                }

                // Update old timestamp
                oldTs = time;
            }
        }
    };

    /**
     * Media RTP input
     */
    private class MediaRtpInput implements MediaInput {
        /**
         * Received frames
         */
        private FifoBuffer fifo = null;

        /**
         * Constructor
         */
        public MediaRtpInput() {
        }

        /**
         * Add a new video frame
         *
         * @param data Data
         * @param timestamp Timestamp
         */
        public void addFrame(byte[] data, long timestamp) {
            if (fifo != null) {
                fifo.addObject(new MediaSample(data, timestamp));
            }
        }

        /**
         * Open the player
         */
        public void open() {
            fifo = new FifoBuffer();
        }

        /**
         * Close the player
         */
        public void close() {
            if (fifo != null) {
                fifo.close();
                fifo = null;
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
                if (fifo != null) {
                    return (MediaSample)fifo.getObject();
                } else {
                    throw new MediaException("Media input not opened");
                }
            } catch (Exception e) {
                throw new MediaException("Can't read media sample");
            }
        }
    }
}
