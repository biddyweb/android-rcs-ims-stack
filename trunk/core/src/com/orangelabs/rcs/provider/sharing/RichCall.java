package com.orangelabs.rcs.provider.sharing;

import android.content.ContentResolver;
import android.content.Context;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Rich call content provider
 * 
 * @author jexa7410
 */
public class RichCall {
	/**
	 * Current instance
	 */
	private static RichCall instance = null;

	/**
	 * Content resolver
	 */
	private ContentResolver cr;
	
	/**
	 * Database URI
	 */
// TODO	private Uri databaseUri = RichCallData.CONTENT_URI;
	
	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Create instance
	 * 
	 * @param ctx Context
	 */
	public static synchronized void createInstance(Context ctx) {
		if (instance == null) {
			instance = new RichCall(ctx);
		}
	}
	
	/**
	 * Returns instance
	 * 
	 * @return Instance
	 */
	public static RichCall getInstance() {
		return instance;
	}
	
	/**
     * Constructor
     * 
     * @param ctx Application context
     */

	private RichCall(Context ctx) {
		super();
		
        this.cr = ctx.getContentResolver();
	}
	
	/**
	 * Add a new call
	 */
	public void addCall() {
		// TODO
	}
}
