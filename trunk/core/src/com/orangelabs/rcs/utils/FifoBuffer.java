package com.orangelabs.rcs.utils;

import java.util.Vector;

/**
 * FIFO buffer
 * 
 * @author JM. Auffret
 */
public class FifoBuffer {
	/**
	 * Number of objects in the buffer
	 */
	private int nbObjects = 0;

	/**
	 * Buffer of objects
	 */
	private Vector<Object> fifo = new Vector<Object>();

	/**
	 * Add an object in the buffer
	 *
	 * @param obj Message
	 */
	public synchronized void addObject(Object obj) {
		fifo.addElement(obj);
		nbObjects++;
		notifyAll();
	}

	/**
	 * Read an object in the buffer. This is a blocking method until an object is read.
	 * 
	 * @return Object
	 */
	public synchronized Object getObject() {
		Object obj = null;
		if (nbObjects == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				// Nothing to do
			}
		}
		if (nbObjects != 0) {
			obj = fifo.elementAt(0);
			fifo.removeElementAt(0);
			nbObjects--;
			notifyAll();
		}
		return obj;
	}

	/**
	 * Read an object in the buffer. This is a blocking method until a timeout
	 * occurs or an object is read.
	 * 
	 * @param timeout Timeout
	 * @return Message
	 */
	public synchronized Object getObject(int timeout) {
		Object obj = null;
		if (nbObjects == 0) {
			try {
				wait(timeout);
			} catch (InterruptedException e) {
				// Nothing to do
			}
		}
		if (nbObjects != 0) {
			obj = fifo.elementAt(0);
			fifo.removeElementAt(0);
			nbObjects--;
			notifyAll();
		}
		return obj;
	}

	/**
	 * Close the buffer
	 */
	public void close() {
		synchronized(this) {
			// Free the semaphore
			this.notifyAll();
		}
	}
}