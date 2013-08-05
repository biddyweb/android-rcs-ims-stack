package com.orangelabs.rcs.service.api.client.media.audio;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.RemoteException;
import android.util.Log;

import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRtpSender;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.audio.AudioFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaException;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaInput;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaSample;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.RtpStreamListener;
import com.orangelabs.rcs.platform.network.DatagramConnection;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.service.api.client.media.IAudioEventListener;
import com.orangelabs.rcs.service.api.client.media.IAudioPlayer;
import com.orangelabs.rcs.service.api.client.media.IMediaEventListener;
import com.orangelabs.rcs.service.api.client.media.MediaCodec;
import com.orangelabs.rcs.utils.CodecsUtils;
import com.orangelabs.rcs.utils.FifoBuffer;
import com.orangelabs.rcs.utils.NetworkRessourceManager;
import com.orangelabs.rcs.utils.logger.Logger;

public class LiveAudioPlayer extends IAudioPlayer.Stub implements RtpStreamListener {
	
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
     * Local MediaRecorder object to capture mic and encode it
     */
    private MediaRecorder mediaRecorder;    
    
	/**
     * Local RTP port
     */
    private int localRtpPort;
    
	/**
     * Remote RTP host
     */
    private String remoteRtpHost;
    
	/**
     * Remote RTP port
     */
    private int remoteRtpPort;
    
    /**
     * RTP sender session
     */
    private MediaRtpSender rtpSender = null;

    /**
     * RTP audio input
     */
    private AudioRtpInput rtpInput = null;
    
    /**
     * Is player opened
     */
    private boolean opened = false;

    /**
     * Is player started
     */
    private boolean started = false;
    
    /**
     * Media event listeners
     */
    private Vector<IMediaEventListener> listeners = new Vector<IMediaEventListener>();
    
    /**
     * Temporary connection to reserve the port
     */
    private DatagramConnection temporaryConnection = null; 
    
    /**
     * Timestamp increment
     */
    private int timestampInc = 100; //TODO calculate it
    
    /***
     * Current time stamp
     */
    private long timeStamp = 0;
    
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
    private static final String LOCAL_SOCKET = "com.orangelabs.rcs.service.api.client.media.audio.socket.player";
    
    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
     * Constructor
     */
	public LiveAudioPlayer() { 	
		
    	// Set the local RTP port
        localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
        reservePort(localRtpPort);
        
        // Init codecs
        supportedAudioCodecs = CodecsUtils.getSupportedAudioCodecList();   

        // Set the default media codec
        if (supportedAudioCodecs.length > 0) {
            setAudioCodec(supportedAudioCodecs[0]);
        }
        
        logger.info("LiveAudioPlayer constructor : reserve local RTP port("+localRtpPort+"), init codec and set audiocodec("+supportedAudioCodecs[0].getCodecName()+")");

    }
	
	@Override
	public void rtpStreamAborted() {
		
	}

	 /**
     * Reserve a port.
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

	@Override
	public void open(String remoteHost, int remotePort) throws RemoteException {
		
		if (remoteHost!=null) remoteRtpHost = remoteHost;
		remoteRtpPort = remotePort;

        if (opened) {
            // Already opened
        	//logger.info("audioplayer open : already opened");
            return;
        }		
        
        // Init the socket listener thread
        new LiveAudioPlayerSocketListener().start();
        
        // Init the RTP layer
        try {
            releasePort();
            rtpSender = new MediaRtpSender(audioFormat, localRtpPort);
            rtpInput = new AudioRtpInput();
            rtpInput.open();
            rtpSender.prepareSession(rtpInput, remoteHost, remotePort, this);
        } catch (Exception e) {
            notifyPlayerEventError(e.getMessage());
            return;
        }
        
        // Player is opened
        opened = true;
        notifyPlayerEventOpened();       
	}

	@Override
	public void close() throws RemoteException {		
        if (!opened) {
            // Already closed
        	//logger.info("audioplayer close : already closed");
            return;
        }
        // Close the RTP layer
        rtpInput.close();
        rtpSender.stopSession();

        // Player is closed
        opened = false;
        notifyPlayerEventClosed();
        listeners.clear();		
	}

	@Override
	public void start() throws RemoteException {    		
        if (!opened) {
            // Player not opened
        	//logger.info("audioplayer start : not opened");        	
            return;
        }

        if (started) {
            // Already started
        	//logger.info("audioplayer start : already started");
            return;
        }
		// Start audio recorder
		// Feed the Socket ("audioBuffer") here with audio stream from recorder (in place of onPreview callback)
		// Read the Socket ("audioBuffer") here with audio stream from recorder (in place of onPreview callback)

		// Create media recorder
		mediaRecorder = new MediaRecorder();
		//logger.info("create MediaRecorder");
		
		// Set media recorder listener 
		mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {

			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				logger.error("mediaRecorder error : what=" + what);
			}
		});
		
		// Set media recorder audio source, output format and audio encoder
    	mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);    	
    	mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    	mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
    	//logger.info("set mediaRecorder source=MIC outputformat=3GPP audioencoder=AMR_WB");
		
		// Set output in a socket
		localSocketSender = new LocalSocket(); 
		//logger.info("new localSenderSocket");

		try {
			 localSocketSender.connect(new LocalSocketAddress(LOCAL_SOCKET));
			 logger.warn("localSenderSocket connect locally to the thread");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		mediaRecorder.setOutputFile(localSocketSender.getFileDescriptor());
		//logger.info("mediaRecorder local socket sender endpoint = " + LOCAL_SOCKET);
    	
    	// Prepare the media recorder
    	try {
			mediaRecorder.prepare();
			//logger.info("prepare mediaRecorder");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// Start the RTP sender
    	rtpSender.startSession();
    	
    	// Start the media recorder
		mediaRecorder.start();
		//logger.info("start MediaRecorder");
		//logger.info("---");
		
        // Player is started
        started = true;
        notifyPlayerEventStarted();
	}

	@Override
	public void stop() throws RemoteException {		
        if (!opened) {
            // Player not opened
        	//logger.info("audioplayer stop : not opened");
            return;
        }

        if (!started) {
            // Already stopped
        	//logger.info("audioplayer stop : already started");
            return;
        }
        
		// Stop audio recorder
   	 	mediaRecorder.stop();
   	 	//logger.info("stop MediaRecorder");
		try {
			localSocketSender.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
   	 	//logger.info("close localSocketSender");
   	 	mediaRecorder.reset();
   	 	// Release the recorder
   	 	mediaRecorder.release();
   	 	//logger.info("release MediaRecorder");
   	 	
        notifyPlayerEventStopped();		
	}

	@Override
	public int getLocalRtpPort() throws RemoteException {
		return localRtpPort;
	}

	@Override
	public void addListener(IAudioEventListener listener)
			throws RemoteException {		
	}

	@Override
	public void removeAllListeners() throws RemoteException {
		
	}

    /**
     * Get supported media codecs
     *
     * @return media Codecs list
     */
    public MediaCodec[] getSupportedAudioCodecs() {
        return supportedAudioCodecs;
    }

	@Override
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
     * Media RTP input
     */
    private static class AudioRtpInput implements MediaInput {
        /**
         * Received frames
         */
        private FifoBuffer fifo = null;

        /**
         * Constructor
         */
        public AudioRtpInput() {
        }

        /**
         * Add a new audio sample
         *
         * @param data Data
         * @param timestamp Timestamp
         * @param marker Marker bit 
         */
        public void addSample(byte[] data, long timestamp) {
            if (fifo != null) {
                MediaSample sample = new MediaSample(data, timestamp);
                fifo.addObject(sample);
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
         * Read an media sample (blocking method)
         *
         * @return Media sample
         * @throws MediaException
         */
        public MediaSample readSample() throws MediaException {
            try {
                if (fifo != null) {
                    return (MediaSample)fifo.getObject();
                } else {
                    throw new MediaException("Media audio input not opened");
                }
            } catch (Exception e) {
                throw new MediaException("Can't read media audio sample");
            }
        }    
    }
    
    /**
     * Thread that listen from local socket connection and read bytes from it
     *
     */
    class LiveAudioPlayerSocketListener extends Thread {
        
        public LiveAudioPlayerSocketListener() {
        	//logger.info("create SocketListener"); 
        }
        
        @Override
        public void run() {

            try {
                localServerSocket = new LocalServerSocket(LOCAL_SOCKET);
                //logger.info("create localServerSocket");
                //logger.info("---");
                while (true) {
                    localSocketReceiver = localServerSocket.accept();
                    //logger.info("accept localSocketReceiver");               
                    if (localSocketReceiver != null) { 
                        
                        // Reading bytes from the socket
                    	//logger.info("reading inputstream");
            	 		InputStream in = localSocketReceiver.getInputStream();

//            	    	String externalStorageRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//            	    	String uniqueOutFile = externalStorageRootPath + "/TEST" + System.currentTimeMillis() + ".3gp";
//            	    	FileOutputStream fop = new FileOutputStream(uniqueOutFile);
//            	    	logger.info("writing to file : "+uniqueOutFile);

            	        int len = 0; 
            	        byte[] buffer = new byte[1024];
            	        
            	        while ((len = in.read(buffer)) >= 0) {
            	        	//logger.info("writing in rtpinput : "+len+" bytes to "+remoteRtpHost+":"+remoteRtpPort);
            	        	//logger.info("buffer content : "+buffer);            	        	
            	        	//logger.info("in file");
            	        	//fop.write(buffer, 0, len);
            	            rtpInput.addSample(buffer, timeStamp);            	             
            	            timeStamp += timestampInc; // needed ?
            	            
            	        }
            	        //logger.info("stop reading inputstream");
                    }
                }
            } catch (IOException e) {
                Log.e(getClass().getName(), e.getMessage());
            }
        }
    }

}
