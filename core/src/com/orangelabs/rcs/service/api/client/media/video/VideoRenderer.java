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

package com.orangelabs.rcs.service.api.client.media.video;

import android.graphics.Bitmap;
import android.os.RemoteException;
import android.os.SystemClock;
import com.orangelabs.rcs.core.ims.protocol.rtp.DummyPacketGenerator;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRtpReceiver;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.H263Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.decoder.NativeH263Decoder;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.H264Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.decoder.NativeH264Decoder;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.H263VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.H264VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaOutput;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaSample;
import com.orangelabs.rcs.platform.network.DatagramConnection;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.media.IMediaEventListener;
import com.orangelabs.rcs.service.api.client.media.IMediaRenderer;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.logger.Logger;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 * Video RTP renderer
 * supports H.263 176x144 frames and H264
 *
 * @author jexa7410
 */
public class VideoRenderer extends IMediaRenderer.Stub {

    /**
     * Enum Video format
     */
    private enum LocalVideoFormat {
        H263, H264
    };

    /**
     * Local video format
     */
    private LocalVideoFormat localVideoFormat;

    /**
     * Video format
     */
    private VideoFormat videoFormat;

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
     * Video surface
     */
    private VideoSurfaceView surface = null;

    /**
     * Media event listeners
     */
    private Vector<IMediaEventListener> listeners = new Vector<IMediaEventListener>();

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Temporary connection to reserve the port
     */
    private DatagramConnection temporaryConnection = null;

    /**
     * video width
     */
    public int video_width;

    /**
     * video height
     */
    public int video_height;

    /**
     * Constructor
     * use setting video codec.
     */
    public VideoRenderer() {
        if (RcsSettings.getInstance().getCShVideoFormat().equals(H264VideoFormat.ENCODING)) {
            localVideoFormat = LocalVideoFormat.H264;
            videoFormat = (VideoFormat)MediaRegistry.generateFormat(H264VideoFormat.ENCODING);
            video_width = H264Config.VIDEO_WIDTH;
            video_height = H264Config.VIDEO_HEIGHT;
        } else { // default H263
            localVideoFormat = LocalVideoFormat.H263;
            videoFormat = (VideoFormat)MediaRegistry.generateFormat(H263VideoFormat.ENCODING);
            video_width = H263Config.VIDEO_WIDTH;
            video_height = H263Config.VIDEO_HEIGHT;
        }

        // Set the local RTP port
        localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
        reservePort(localRtpPort);
    }

    /**
     * Constructor
     * Force a video codec.
     *
     * @param codec Video codec
     */
    public VideoRenderer(String codec) {
        if (codec.equals(H264VideoFormat.ENCODING)) {
            localVideoFormat = LocalVideoFormat.H264;
            videoFormat = (VideoFormat)MediaRegistry.generateFormat(H264VideoFormat.ENCODING);
            video_width = H264Config.VIDEO_WIDTH;
            video_height = H264Config.VIDEO_HEIGHT;
        } else { // default H263
            localVideoFormat = LocalVideoFormat.H263;
            videoFormat = (VideoFormat)MediaRegistry.generateFormat(H263VideoFormat.ENCODING);
            video_width = H263Config.VIDEO_WIDTH;
            video_height = H263Config.VIDEO_HEIGHT;
        }

        // Set the local RTP port
        localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
        reservePort(localRtpPort);
    }

    /**
     * Set the surface to render video
     *
     * @param surface Video surface
     */
    public void setVideoSurface(VideoSurfaceView surface) {
        this.surface = surface;
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
        if (opened) {
            // Already opened
            return;
        }

        try {
            // Init the video decoder
            int result;
            if (localVideoFormat == LocalVideoFormat.H264) {
                result = NativeH264Decoder.InitDecoder();
            } else { // default H263
                result = NativeH263Decoder.InitDecoder(video_width, video_height);
            }
            if (result == 0) {
                notifyPlayerEventError("Decoder init failed with error code " + result);
                return;
            }
        } catch (UnsatisfiedLinkError e) {
            notifyPlayerEventError(e.getMessage());
            return;
        }

        try {
            // Init the RTP layer
            releasePort();
            rtpReceiver = new MediaRtpReceiver(localRtpPort);
            rtpDummySender = new DummyPacketGenerator();
            rtpOutput = new MediaRtpOutput();
            rtpOutput.open();
            rtpReceiver.prepareSession(rtpOutput, videoFormat);
            rtpDummySender.prepareSession(remoteHost, remotePort);
        } catch (Exception e) {
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
        if (!opened) {
            // Already closed
            return;
        }

        // Close the RTP layer
        rtpOutput.close();
        rtpReceiver.stopSession();
        rtpDummySender.stopSession();

        try {
            // Close the video decoder
            if (localVideoFormat == LocalVideoFormat.H264) {
                NativeH264Decoder.DeinitDecoder();
            } else { // default H263
                NativeH263Decoder.DeinitDecoder();
            }
        } catch (UnsatisfiedLinkError e) {
            if (logger.isActivated()) {
                logger.error("Can't close correctly the video decoder", e);
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
        rtpReceiver.startSession();
        rtpDummySender.startSession();

        // Renderer is started
        videoStartTime = SystemClock.uptimeMillis();
        started = true;
        notifyPlayerEventStarted();
    }

    /**
     * Stop the renderer
     */
    public void stop() {
        if (!started) {
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

        // Stop decoder
        // TODO

        // Renderer is stopped
        started = false;
        videoStartTime = 0L;
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
     * Media RTP output
     */
    private class MediaRtpOutput implements MediaOutput {
        /**
         * Video frame
         */
        private int decodedFrame[];

        /**
         * Bitmap frame
         */
        private Bitmap rgbFrame;

        /**
         * Constructor
         */
        public MediaRtpOutput() {
            decodedFrame = new int[video_width * video_height];
            rgbFrame = Bitmap.createBitmap(video_width, video_height, Bitmap.Config.RGB_565);
        }

        /**
         * Open the renderer
         */
        public void open() {
            // Nothing to do
        }

        /**
         * Close the renderer
         */
        public void close() {
            // Force black screen
        	surface.clearImage();
        }

        /**
         * Write a media sample
         *
         * @param sample Sample
         */
        public void writeSample(MediaSample sample) {
            if (localVideoFormat == LocalVideoFormat.H264) {
                if (NativeH264Decoder.DecodeAndConvert(sample.getData(), decodedFrame) == 1) {
                    rgbFrame.setPixels(decodedFrame, 0, video_width, 0, 0, video_width,
                            video_height);
                    if (surface != null) {
                        surface.setImage(rgbFrame);
                    }
                }
            } else { // default H263
                if (NativeH263Decoder.DecodeAndConvert(sample.getData(), decodedFrame,
                        sample.getTimeStamp()) == 1) {
                    rgbFrame.setPixels(decodedFrame, 0, video_width, 0, 0, video_width,
                            video_height);
                    if (surface != null) {
                        surface.setImage(rgbFrame);
                    }
                }
            }
        }
    }
}

