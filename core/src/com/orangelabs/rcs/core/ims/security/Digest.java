package com.orangelabs.rcs.core.ims.security;

/**
 * Interface that a message digest conforms to
 */
public interface Digest {
	/**
	 * return the algorithm name
	 * 
	 * @return the algorithm name
	 */
	public String getAlgorithmName();

	/**
	 * return the size, in bytes, of the digest produced by this message digest
	 * 
	 * @return the size, in bytes, of the digest produced by this message digest
	 */
	public int getDigestSize();

	/**
	 * Update the message digest with a single byte.
	 * 
	 * @param in Input byte to be entered.
	 */
	public void update(byte in);

	/**
	 * Update the message digest with a block of bytes.
	 * 
	 * @param in Byte array containing the data
	 * @param inOff Offset into the byte array where the data starts
	 * @param len Length of the data
	 */
	public void update(byte[] in, int inOff, int len);

	/**
	 * Close the digest, producing the final digest value. The doFinal call
	 * leaves the digest reset.
	 * 
	 * @param out Array the digest is to be copied into
	 * @param outOff Offset into the out array the digest is to start at
	 */
	public int doFinal(byte[] out, int outOff);

	/**
	 * reset the digest back to it's initial state.
	 */
	public void reset();
}
