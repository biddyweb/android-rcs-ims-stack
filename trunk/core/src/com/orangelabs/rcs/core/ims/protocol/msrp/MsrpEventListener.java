package com.orangelabs.rcs.core.ims.protocol.msrp;

/**
 * MSRP event listener
 * 
 * @author jexa7410
 */
public interface MsrpEventListener {
	/**
	 * Data has been transfered
	 */
	public void msrpDataTransfered();
	
	/**
	 * Data has been received
	 * 
	 * @param data Received data
	 * @param mimeType Data mime-type 
	 */
	public void msrpDataReceived(byte[] data, String mimeType);
	
	/**
	 * MSRP transfer indicator event
	 * 
	 * @param currentSize Current transfered size in bytes
	 * @param totalSize Total size in bytes
	 */
	public void msrpTransferProgress(long currentSize, long totalSize);	
	
	/**
	 * MSRP transfer aborted
	 */
	public void msrpTransferAborted();
	
	/**
	 * MSRP transfer error
	 * 
	 * @param error Error
	 */
	public void msrpTransferError(String error);
}
