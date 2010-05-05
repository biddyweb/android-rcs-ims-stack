package com.orangelabs.rcs.utils.logger;

/**
 * Appender
 * 
 * @author jexa7410
 */
public abstract class Appender {
	/**
	 * Constructor
	 */
	public Appender() {
	}

	/**
	 * Print a trace
	 *
	 * @param classname Classname
	 * @param level Trace level
	 * @param trace Trace
	 */
	public abstract void printTrace(String classname, int level, String trace);
}
