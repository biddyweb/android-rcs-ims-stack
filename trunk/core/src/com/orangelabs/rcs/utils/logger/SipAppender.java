package com.orangelabs.rcs.utils.logger;

/**
 * SIP callflow appender 
 * 
 * @author jexa7410
 */
public class SipAppender extends Appender {
	
	/**
	 * Constructor
	 */
	public SipAppender() {
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
				(classname.equals("SipUdpManager")) &&
					trace.startsWith(">>>>>>>>>> SIP message sent")
						|| trace.startsWith(">>>>>>>>>> SIP message received")) {
			System.out.println(trace);
		}
	 }
}
