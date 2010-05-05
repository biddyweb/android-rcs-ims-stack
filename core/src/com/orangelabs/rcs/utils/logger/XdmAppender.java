package com.orangelabs.rcs.utils.logger;

/**
 * XDM callflow appender 
 * 
 * @author jexa7410
 */
public class XdmAppender extends Appender {
	
	/**
	 * Constructor
	 */
	public XdmAppender() {
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
		if ((classname != null) &&
				(classname.equals("XdmManager")) &&
					(trace.startsWith("Send HTTP request")
							|| trace.startsWith("Receive HTTP response"))) {
			System.out.println(trace);
		}
	 }
}