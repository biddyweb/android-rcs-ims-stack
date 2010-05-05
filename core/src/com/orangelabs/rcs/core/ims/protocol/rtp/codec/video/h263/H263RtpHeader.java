package com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263;

/**
 * RFC 4629: a special header is added to each H263+ packet that
 * immediately follows the RTP header:
 *
 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |   RR    |P|V|   PLEN    |PEBIT|
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class H263RtpHeader{

	public int HEADER_SIZE = 2;	
	
	public byte RR;
	public boolean P;
	public boolean V;
	public int PLEN;
	public int PEBIT;

	/**
	 * Constructor
	 * 
	 * @param RR
	 * @param P
	 * @param V
	 * @param PLEN
	 * @param PEBIT
	 */
	public H263RtpHeader(final byte RR, final boolean P, final boolean V, final int PLEN, final int PEBIT){
		this.RR = RR;
		this.P = P;
		this.V = V;
		this.PLEN = PLEN;
		this.PEBIT = PEBIT;
	}		
	
	/**
	 * Constructor
	 * 
	 * @param data
	 */
	public H263RtpHeader(byte[] data){
		RR = (byte)(data[0]>>3);
		P = (data[0]&0x4) != 0;
		V = (data[0]&0x2) != 0;
		PLEN = ((data[0]&0x1)<<5)|(data[1]>>3);
		PEBIT = data[1]&0x7;
	}

}
