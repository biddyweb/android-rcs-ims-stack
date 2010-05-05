package com.orangelabs.rcs.core.ims.protocol.rtp.util;

/**
 * Generic packet
 * 
 * @author jexa7410
 */
public class Packet {
	/**
	 * Data
	 */
	public byte[] data;
	
	/**
	 * Packet length
	 */
	public int length;

	/**
	 * Offset
	 */
	public int offset;
		
	/**
	 * Received at
	 */
	public long receiptAt;

	/**
	 * Constructor
	 */
	public Packet() {
	}

	/**
	 * Constructor
	 * 
	 * @param packet Packet
	 */
	public Packet(Packet packet) {
		data = packet.data;
		length = packet.length;
		offset = packet.offset;
		receiptAt = packet.receiptAt;
	}
}
