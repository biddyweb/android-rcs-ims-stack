package com.orangelabs.rcs.platform.logger;

import android.util.Log;

import com.orangelabs.rcs.utils.logger.Appender;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Android appender 
 * 
 * @author jexa7410
 */
public class AndroidAppender extends Appender {
	/**
	 * Constructor
	 */
	public AndroidAppender() {
		super();
	}

	/**
	 * Print a trace
	 *
	 * @param classname Classname
	 * @param level Trace level
	 * @param trace Trace
	 */
	public synchronized void printTrace(String classname, int level, String trace) {
		classname = "[RCS][" + classname + "]";
		
		if (level == Logger.INFO_LEVEL) {
			Log.i(classname, trace);
		} else
		if (level == Logger.WARN_LEVEL) {
			Log.w(classname, trace);
		} else
		if (level == Logger.ERROR_LEVEL) {
			Log.e(classname, trace);
		} else
		if (level == Logger.FATAL_LEVEL) {
			Log.e(classname, trace);
		} else {
			Log.v(classname, trace);
		}
	 }
}