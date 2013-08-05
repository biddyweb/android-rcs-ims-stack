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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.RemoteException;
import android.os.SystemClock;

import com.orangelabs.rcs.core.ims.protocol.rtp.DummyPacketGenerator;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRtpReceiver;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.audio.AudioFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaException;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaOutput;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaSample;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.RtpInputStream;
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
 * @author opob7414
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
     * RtpInputStream shared with the renderer
     */
    private RtpInputStream rendererRtpInputStream = null; 

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
     * RTP audio output
     */
    private AudioRtpOutput rtpOutput = null;
    
	/**
     * Local MediaPlayer object to decode the stream and play it
     */
    private MediaPlayer mediaPlayer;    

    /**
     * Local file output stream for file recording
     */    
    private FileOutputStream fop;
    
    /**
     * Local file output stream for buffer file recording
     */    
    private File outputBufferFile;
    
    /**
     * Minimum number of buffers before playing in the mediaplayer and size of read piece of audio
     */    
    private int nbMinBuffer = 25;
    
    /**
     * Current number of received buffers before playing in the mediaplayer
     */    
    private int nbBuffer = 0;
    
    /**
     * Local buffer media file input stream for the mediaplayer
     */  
    private FileInputStream fin;  

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
     * Local context of the application
     */
    private Context rendererContext;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     */
    public AudioRenderer(Context context) {
        // Set the local RTP port
        localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
        reservePort(localRtpPort);

        // Init codecs
        supportedAudioCodecs = CodecsUtils.getSupportedAudioCodecList();   

        // Set the default audio codec
        if (supportedAudioCodecs.length > 0) {
            setAudioCodec(supportedAudioCodecs[0]);
        }
        
        if (logger.isActivated()) {
        	logger.info("AudioRenderer constructor : reserve local RTP port("+localRtpPort+"), init codec and set audiocodec("+supportedAudioCodecs[0].getCodecName()+")");
        }
        
        this.rendererContext = context;
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
     * Returns the local RTP stream (set after the open)
     *
     * @return RtpInputStream
     */
    public RtpInputStream getRtpInputStream() {
        return rendererRtpInputStream;
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
            rtpOutput = new AudioRtpOutput();
            rtpOutput.open();
            rtpReceiver.prepareSession(remoteHost, remotePort, rtpOutput, audioFormat, this);  
            rendererRtpInputStream = rtpReceiver.getInputStream();
            rtpDummySender.prepareSession(remoteHost, remotePort, rtpReceiver.getInputStream());
            rtpDummySender.startSession();
        } catch (Exception e) {
        	if (logger.isActivated()) {
        		logger.warn("AudioRenderer init RTP layer failed");
        	}
            notifyPlayerEventError(e.getMessage());
            return;
        }
        
        // Init the file buffer to write sound data        
    	File outputBufferDir = rendererContext.getCacheDir();
    	try {
			outputBufferFile = File.createTempFile("RTPBUFFER", ".AMR", outputBufferDir);
			outputBufferFile.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	if (logger.isActivated()) {
    		logger.info("prepare record file : " + outputBufferFile.getAbsolutePath());
    	}
    	try {
			fop = new FileOutputStream(outputBufferFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    	// Set the audio settings
    	AudioManager audioManager;  
    	audioManager = (AudioManager)rendererContext.getSystemService(Context.AUDIO_SERVICE);  
    	audioManager.setMode(AudioManager.MODE_IN_CALL); 
    	audioManager.setSpeakerphoneOn(false); 
    	if (logger.isActivated()) {
    		logger.info("set audiomanager settings : MODE_IN_CALL and SpeakerphoneOn to false");
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
        
        // Close the file streams
        try {
	        fop.close();
	        fin.close();
		} catch (IOException e) {
			e.printStackTrace();
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

        // Start the RTP receiver
        rtpReceiver.startSession();
        
        // Create the mediaPlayer (started after nbMinBuffer packets received through writeSample())
		if (logger.isActivated()) {
			logger.info("create the mediaplayer");
		}
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        
        // Renderer is started
        audioStartTime = SystemClock.uptimeMillis();
        started = true;
        notifyPlayerEventStarted();
    }
    
    /**
     * Set up the media player
     */
    private void setupMediaPlayer() {
    	
        // Set data source to the mediaPlayer
		if (logger.isActivated()) {
			logger.info("set data source to the mediaplayer");
		}
        try {
        	fin = new FileInputStream(this.outputBufferFile);
			mediaPlayer.setDataSource(fin.getFD()); 
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        // Prepare the mediaPlayer
		if (logger.isActivated()) {
			logger.info("prepare the mediaplayer");
		}
        try {
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
        	@Override
        	public void onPrepared(MediaPlayer mp) {
        		
        		// The mediaPlayer is prepared
				if (logger.isActivated()) {
					logger.info("the mediaplayer is prepared");
				}
            }
        });
        
        mediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			@Override
			public void onSeekComplete(MediaPlayer mp) {	
				mp.start();
				// The mediaPlayer is seeked
				if (logger.isActivated()) {
					logger.info("the mediaplayer is seeked and restart");
				}
			}
        	
        });
        
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (started) {
					// Reset mediaPlayer so it use new buffered data in the file
					int timePosition = mp.getCurrentPosition();
	                mp.reset();
	                try {
		                mp.setDataSource(fin.getFD());
		                mp.prepare();
		                mp.seekTo(timePosition);
	        		} catch (IllegalArgumentException e) {
	        			e.printStackTrace();
	        		} catch (SecurityException e) {
	        			e.printStackTrace();
	        		} catch (IllegalStateException e) {
	        			e.printStackTrace();
	        		} catch (IOException e) {
	        			e.printStackTrace();
	        		}
					// The mediaPlayer stops
					if (logger.isActivated()) {
						logger.info("the mediaplayer is rebuffering");
					}
				} else {
					mp.release();
					// The mediaPlayer stops
					if (logger.isActivated()) {
						logger.info("the mediaplayer stops playing");
					}
				}
			}
        });
        
		if (logger.isActivated()) {
			logger.info("set up mediaplayer with duration (secs): " + mediaPlayer.getDuration());
		}
        
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
        
        // Stop the mediaplayer
        mediaPlayer.stop();

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
     * Audio RTP output
     */
    private class AudioRtpOutput implements MediaOutput {

        /**f
         * Constructor
         */
        public AudioRtpOutput() {
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
        	
			// Write data to record file
        	try {				
				fop.write(sample.getData(), 0, sample.getData().length);
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        	// Start playing when number of received buffers reach nbMinBuffer
        	nbBuffer++;
        	if (nbBuffer == nbMinBuffer) setupMediaPlayer();

    		if (logger.isActivated()) {		
	            StringBuilder sb = new StringBuilder();
	            for (int y = 0; y < sample.getData().length; y++) {
	            	sb.append(" "+Byte.valueOf(sample.getData()[y]).toString());            	            	
	            }
	            logger.info("RTP buffer content : " + sb.toString());	            
    		}
			
		}
    }
       
}