package com.orangelabs.rcs.utils.logger;

/**
 * Console appender 
 * 
 * @author jexa7410
 */
public class ConsoleAppender extends Appender {
	/**
	 * Constructor
	 */
	public ConsoleAppender() {
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
		System.out.println("[" + LoggerUtils.formatDate() + "][" +
				LoggerUtils.formatClassname(classname) + "][" + LoggerUtils.formatLevel(level) + "]" + trace);
	 }
}
