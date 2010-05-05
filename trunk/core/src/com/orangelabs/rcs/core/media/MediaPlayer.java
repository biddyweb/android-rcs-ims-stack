package com.orangelabs.rcs.core.media;

/**
 * Media player (e.g. camera, microphone) that permits to generate media
 * 
 * @author jexa7410
 */
public abstract class MediaPlayer {
	/**
	 * Player listener
	 */
	private MediaListener listener = null;

	/**
	 * Constructor
	 */
	public MediaPlayer() {
    }	

	/**
	 * Returns the player listener
	 * 
	 * @return Player listener
	 */
	public MediaListener getListener() {
		return listener;
	}
	
	/**
	 * Add a listener
	 * 
	 * @param listener Player listener
	 */
	public void addListener(MediaListener listener) {
		this.listener = listener;
	}
	
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
