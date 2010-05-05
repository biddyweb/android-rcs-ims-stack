package com.orangelabs.rcs.core.ims.protocol.msrp;

import java.util.Vector;

/**
 * Fifo buffer
 * 
 * @author JM. Auffret
 */
public class FifoBuffer {
	/**
	 * Number of messages in the buffer
	 */
	private int numMessage = 0;

	/**
	 * Buffer of messages
	 */
	private Vector<Object> fifo = new Vector<Object>();

	/**
	 * Add a message in the buffer
	 *
	 * @param obj Message
	 */
	public synchronized void putMessage(Object obj) {
		fifo.addElement(obj);
		numMessage++;
		notifyAll();
	}

	/**
	 * Read a message in the buffer. This is a blocking method until a
	 * message is received in the buffer.
	 * 
	 * @return Message
	 */
	public synchronized Object getMessage() {
		Object message = null;
		if (numMessage == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				// Nothing to do
			}
		}
		if (numMessage != 0) {
			message = fifo.elementAt(0);
			fifo.removeElementAt(0);
			numMessage--;
			notifyAll();
		}
		return message;
	}

	/**
	 * Read a message in the buffer. This is a blocking method until a timeout
	 * occurs or a message is received in the buffer.
	 * 
	 * @param timeout Timeout
	 * @return Message
	 */
	public synchronized Object getMessage(int timeout) {
		Object message = null;
		if (numMessage == 0) {
			try {
				wait(timeout);
			} catch (InterruptedException e) {
				// Nothing to do
			}
		}
		if (numMessage != 0) {
			message = fifo.elementAt(0);
			fifo.removeElementAt(0);
			numMessage--;
			notifyAll();
		}
		return message;
	}

	/**
	 * Unblock the reading
	 */
	public void unblockRead() {
		synchronized (this) {
			this.notifyAll();
		}
	}
}