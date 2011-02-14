/*******************************************************************************
 * Conditions Of Use
 * 
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 ******************************************************************************/
package gov.nist.core.sip.parser;


import gov.nist.core.ParseException;
import gov.nist.core.sip.header.ContentLengthHeader;
import gov.nist.core.sip.message.Message;

import java.io.IOException;
import java.io.InputStream;


/**
 * This implements a pipelined message parser suitable for use with a stream -
 * oriented input such as TCP. The client uses this class by instatiating with
 * an input stream from which input is read and fed to a message parser. It
 * keeps reading from the input stream and process messages in a never ending
 * interpreter loop. The message listener interface gets called for processing
 * messages or for processing errors. The payload specified by the
 * content-length header is read directly from the input stream. This can be
 * accessed from the Message using the getContent and getContentBytes methods
 * provided by the Message class.
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author <A href=mailto:mranga@nist.gov > M. Ranganathan </A> <a href="{@docRoot}/uncopyright.html">This
 *         code is in the public domain.</a>
 * 
 * Lamine Brahimi and Yann Duponchel (IBM Zurich) noticed that the parser was
 * blocking so I threw out some cool pipelining which ran fast but only worked
 * when the phase of the full moon matched its mood. Now things are serialized
 * and life goes slower but more reliably.
 * 
 * @see SIPMessageListener
 */

public final class PipelinedMsgParser implements Runnable {

	/**
	 * The message listener that is registered with this parser. (The message
	 * listener has methods that can process correct and erroneous messages.)
	 */
	protected SIPMessageListener sipMessageListener;

	private Thread mythread; // Preprocessor thread

	private InputStream rawInputStream;

	/**
	 * default constructor.
	 */
	protected PipelinedMsgParser() {
		super();

	}

	/**
	 * Constructor when we are given a message listener and an input stream
	 * (could be a TCP connection or a file)
	 * 
	 * @param sipMessageListener
	 *            Message listener which has methods that get called back from
	 *            the parser when a parse is complete
	 * @param in
	 *            Input stream from which to read the input.
	 * @param debug
	 *            Enable/disable tracing or lexical analyser switch.
	 */
	public PipelinedMsgParser(SIPMessageListener sipMessageListener,
			InputStream in, boolean debug) {
		this();
		this.sipMessageListener = sipMessageListener;
		rawInputStream = in;
		mythread = new Thread(this);

	}

	/**
	 * This is the constructor for the pipelined parser.
	 * 
	 * @param mhandler
	 *            a MessageListener implementation that provides the message
	 *            handlers to handle correctly and incorrectly parsed messages.
	 * @param in
	 *            An input stream to read messages from.
	 */

	public PipelinedMsgParser(SIPMessageListener mhandler, InputStream in) {
		this(mhandler, in, false);
	}

	/**
	 * This is the constructor for the pipelined parser.
	 * 
	 * @param in -
	 *            An input stream to read messages from.
	 */

	public PipelinedMsgParser(InputStream in) {
		this(null, in, false);
	}

	/**
	 * Start reading and processing input.
	 */
	public void processInput() {
		mythread.start();
	}

	/**
	 * Create a new pipelined parser from an existing one.
	 * 
	 * @return A new pipelined parser that reads from the same input stream.
	 */
	protected Object clone() {
		PipelinedMsgParser p = new PipelinedMsgParser();

		p.rawInputStream = this.rawInputStream;
		p.sipMessageListener = this.sipMessageListener;
		return p;
	}

	/**
	 * Add a class that implements a MessageListener interface whose methods get
	 * called * on successful parse and error conditons.
	 * 
	 * @param mlistener
	 *            a MessageListener implementation that can react to correct and
	 *            incorrect pars.
	 */

	public void setMessageListener(SIPMessageListener mlistener) {
		sipMessageListener = mlistener;
	}

	/**
	 * read a line of input (I cannot use buffered reader because we may need to
	 * switch encodings mid-stream!
	 */
	private String readLine(InputStream inputStream) throws IOException {
		StringBuffer retval = new StringBuffer("");
		while (true) {
			try {
				char ch;
				int i = inputStream.read();
				if (i == -1) {
					throw new IOException("End of stream");
				} else
					ch = (char) i;
				if (ch != '\r')
					retval.append(ch);
				if (ch == '\n') {
					break;
				}
			} catch (IOException ex) {
				throw ex;
			}
		}
		return retval.toString();
	}

	/**
	 * This is input reading thread for the pipelined parser. You feed it input
	 * through the input stream (see the constructor) and it calls back an event
	 * listener interface for message processing or error. It cleans up the
	 * input - dealing with things like line continuation
	 * 
	 */

	public void run() {

		InputStream inputStream = null;
		inputStream = this.rawInputStream;
		// I cannot use buffered reader here because we may need to switch
		// encodings to read the message body.
		try {
			while (true) {
				StringBuffer inputBuffer = new StringBuffer();
				String line1;
				String line2 = null;

				// ignore blank lines.
				while (true) {
					try {
						line1 = readLine(inputStream);
						if (line1.equals("\n")) {
							continue;
						} else
							break;
					} catch (IOException ex) {
						return;

					}
				}
				inputBuffer.append(line1);

				while (true) {
					try {
						line2 = readLine(inputStream);
						inputBuffer.append(line2);
						if (line2.trim().equals(""))
							break;
					} catch (IOException ex) {
						return;

					}
				}
				inputBuffer.append(line2);
				StringMsgParser smp = new StringMsgParser(sipMessageListener);
				smp.readBody = false;
				Message sipMessage = null;
				try {
					sipMessage = smp.parseSIPMessage(inputBuffer.toString());
					if (sipMessage == null)
						continue;
				} catch (ParseException ex) {
					// Just ignore the parse exception.
					continue;
				}
				ContentLengthHeader cl = sipMessage.getContentLengthHeader();
				int contentLength = 0;
				if (cl != null) {
					contentLength = cl.getContentLength();
				} else {
					contentLength = 0;
				}

				if (contentLength == 0) {
					sipMessage.removeContent();
				} else { // deal with the message body.
					contentLength = cl.getContentLength();
					byte[] message_body = new byte[contentLength];
					int nread = 0;
					while (nread < contentLength) {
						try {
							int readlength = inputStream.read(message_body,
									nread, contentLength - nread);
							if (readlength > 0) {
								nread += readlength;
							} else {
								break;
							}
						} catch (IOException ex) {
							break;
						}
					}
					sipMessage.setMessageContent(message_body);
				}
				if (sipMessageListener != null) {
					sipMessageListener.processMessage(sipMessage);
				}

			}
		} finally {
			try {
				inputStream.close();
			} catch (IOException ioe) {
			}
		}
	}

}
