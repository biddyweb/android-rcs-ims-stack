package com.orangelabs.rcs.core.media;


/**
 * Media renderer (e.g. screen, headset) that permits to show media
 * 
 * @author jexa7410
 */
public abstract class MediaRenderer {
	/**
	 * Renderer listener
	 */
	private MediaListener listener = null;

	/**
	 * Constructor
	 */
	public MediaRenderer() {
    }	

	/**
	 * Returns the renderer listener
	 * 
	 * @return Renderer listener
	 */
	public MediaListener getListener() {
		return listener;
	}
	
	/**
	 * Add a listener
	 * 
	 * @param listener Renderer listener
	 */
	public void addListener(MediaListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Open the renderer
	 * 
	 * @throws MediaException
	 */
	public abstract void open() throws MediaException;
	
	/**
	 * Close the renderer
	 */
	public abstract void close();

	/**
	 * Start the renderer
	 * 
	 * @throws MediaException
	 */
	public abstract void start() throws MediaException;
	
	/**
	 * Stop the renderer
	 */
	public abstract void stop();

	/**
	 * Write a media sample
	 * 
	 * @aparam sample Media sample
	 * @throws MediaException
	 */
	public abstract void writeSample(MediaSample sample) throws MediaException;
}