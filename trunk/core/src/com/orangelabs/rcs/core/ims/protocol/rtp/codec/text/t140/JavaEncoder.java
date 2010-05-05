package com.orangelabs.rcs.core.ims.protocol.rtp.codec.text.t140;

import java.util.Vector;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.Codec;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;

/**
 * Constructs an RTP-Text packet according to "RFC 4103 - RTP Payload for text conversation",
 * transmitted text must be in UTF-8 form
 * 
 * @author Erik Zetterstrom, Omnitor AB
 * @author Andreas Piirimets, Omnitor AB
 */
public class JavaEncoder extends Codec {

	// Redundancy header bits for packetizer

	/**
	 * Set red bit mask
	 */
	public static final int RTP_RED_SET_BIT = 0x1 << 07;

	/**
	 * Clear red bit
	 */
	public static final int RTP_RED_CLEAR_BIT = 0x0 << 07;

	/**
	 * Set the red F bit
	 */
	public static final int RTP_RED_F_BIT = 0x1 << 07;

	// Redundancy header masks for packetizer

	/**
	 * Time offset upper mask
	 */
	public static final int RTP_PACK_TIMEOFFSET_UPPER_MASK = 0xff << 6;

	/**
	 * Time offset lower mask
	 */
	public static final int RTP_PACK_TIMEOFFSET_LOWER_MASK = 0x3f << 0;

	/**
	 * Blocklength upper mask
	 */
	public static final int RTP_PACK_BLOCKLEN_UPPER_MASK = 0x3 << 8;

	/**
	 * BLock length lower mask
	 */
	public static final int RTP_PACK_BLOCKLEN_LOWER_MASK = 0xff << 0;

	private int t140Pt;
	
	private int redGen;

	/**
	 * Previous generations that are to be transmitted redundantely
	 */
	private Vector<TextRedData> redundantBuffer = null;

	private long sequenceNumber = 1;

	private boolean useRed = false; // TODO: to be set via config file
	
	public JavaEncoder() {
		this.t140Pt = 98; // TODO: to be read from the format or to be removed ?
		this.redGen = useRed ? 2 : 0;
		redundantBuffer = new Vector<TextRedData>(0, 1);
	}

	public int process(Buffer input, Buffer output) {		
		int i = 0; // Packet index
		long timestamp = System.currentTimeMillis();
		byte[] inData = (byte[])input.getData();
		int inDataLength = input.getLength();
		byte[] outData = null;
		byte[] tempOutData = null;
		int outDataSize = 0;

		// Allocate memory for redundant headers
		outData = new byte[redGen * T140Constants.REDUNDANT_HEADER_SIZE];

		if (redundantBuffer == null) {
			redundantBuffer = new Vector<TextRedData>(redGen, 0);
		}

		// Redundant data will be sent.
		if (redGen > 0) {
			int gen = 0;
			int timestampOffset = 0;
			int dataLength = 0;

			// Compensate for insufficient redundant data.

			int border = redGen - redundantBuffer.size();
			for (int g = 0; g < border; g++) {
				// Timestamp 14 bits long
				timestampOffset = 0;
				dataLength = 0;

				// Add redundant header to packet
				outData[i++] = (byte)(RTP_RED_SET_BIT | t140Pt);
				outData[i++] = (byte)((RTP_PACK_TIMEOFFSET_UPPER_MASK & timestampOffset) >>> 6);
				outData[i++] = (byte)(((RTP_PACK_TIMEOFFSET_LOWER_MASK & timestampOffset) << 2) | ((RTP_PACK_BLOCKLEN_UPPER_MASK & dataLength) >>> 8));
				outData[i++] = (byte)((RTP_PACK_BLOCKLEN_LOWER_MASK & dataLength));
			}

			// Add headers for all redundant data, latest data LAST.
			for (gen = 0; gen < redGen; gen++) {

				// Check that enough redundant generations have been stored.
				if (gen < redundantBuffer.size()) {
					TextRedData dataObj = (TextRedData) (redundantBuffer.elementAt(gen));

					// Build the extra header info
					// Timestamp 14 bits long
					timestampOffset = (int)((timestamp - dataObj.getTimeStamp()) & 0x3FFF);
					if (dataObj.getData() == null) {
						dataLength = 0;
					} else {
						dataLength = dataObj.getData().length;
					}

					// Add redundant header to packet
					outData[i++] = (byte)(RTP_RED_SET_BIT | t140Pt);
					outData[i++] = (byte)((RTP_PACK_TIMEOFFSET_UPPER_MASK & timestampOffset) >>> 6);
					outData[i++] = (byte)(((RTP_PACK_TIMEOFFSET_LOWER_MASK & timestampOffset) << 2) | ((RTP_PACK_BLOCKLEN_UPPER_MASK & dataLength) >>> 8));
					outData[i++] = (byte)((RTP_PACK_BLOCKLEN_LOWER_MASK & dataLength));
				}

			}

			// Allocate memory for primary header
			tempOutData = outData;
			outDataSize = outData.length;
			outData = new byte[outDataSize + T140Constants.PRIMARY_HEADER_SIZE];
			System.arraycopy(tempOutData, 0, outData, 0, outDataSize);

			// Add final header
			outData[i++] = (byte)(RTP_RED_CLEAR_BIT | t140Pt);

			// Add redundant data, latest data LAST.
			for (gen = 0; gen < redGen; gen++) {

				if (gen < redundantBuffer.size()) {
					TextRedData dataObjData = (TextRedData) (redundantBuffer
							.elementAt(gen));

					// Add redundant data to packet
					byte[] dataArr = dataObjData.getData();

					if (dataArr != null) {

						// Allocate memory for redundant data
						tempOutData = outData;
						outDataSize = outData.length;
						outData = new byte[outDataSize + dataArr.length];
						System.arraycopy(tempOutData, 0, outData, 0, outDataSize);
						System.arraycopy(dataArr, 0, outData, i, dataArr.length);
						i += dataArr.length;
						dataArr = null;
					}

					dataObjData = null;
				}
			}

			// Remove first redundant element in vector.
			if (redundantBuffer.size() >= redGen) {
				redundantBuffer.removeElementAt(0);
			}

			// Add a new redundant element to vector.
			redundantBuffer.addElement(new TextRedData(timestamp, inData, 0, inDataLength));
		}

		if (inDataLength > 0) {

			// Allocate memory for primary data
			tempOutData = outData;
			outDataSize = outData.length;
			outData = new byte[outDataSize + inDataLength];
			System.arraycopy(tempOutData, 0, outData, 0, outDataSize);

			// Add primary data to packet.
			System.arraycopy(inData, 0, outData, i, inData.length);
			i += inData.length;
		}

		output.setData(outData);
		output.setLength(outData.length);
		output.setOffset(0);
		output.setTimeStamp(timestamp);
		output.setSequenceNumber(sequenceNumber);
		sequenceNumber++;

		return BUFFER_PROCESSED_OK;
	}

	/**
	 * Carries redundant data
	 * 
	 * @author Erik Zetterstrom, Omnitor AB
	 */
	public class TextRedData {
		private long myTimestamp = 0;
		private int mySeqNum = 0;
		private byte[] myDataArr = null;

		/**
		 * Constructor
		 * 
		 * @param timestamp The timestamp of the data
		 * @param dataArr The data byte array
		 * @param dataOffset The offset in bytes for the data
		 * @param dataLength The number of data bytes in the array
		 */
		public TextRedData(long timestamp, byte[] dataArr, int dataOffset, int dataLength) {

			myTimestamp = timestamp;

			if (dataArr == null) {
				myDataArr = null;
			} else {
				myDataArr = new byte[dataLength];
				System.arraycopy(dataArr, dataOffset, myDataArr, 0, dataLength);
			}
		}

		/**
		 * Constructor
		 * 
		 * @param timestamp The timestamp of the data
		 * @param dataArr The data byte array
		 * @param dataOffset The offset in bytes for the data
		 * @param dataLength The number of data bytes in the array
		 * @param theSeqNum The sequnece number
		 */
		public TextRedData(long timestamp, byte[] dataArr, int dataOffset,
				int dataLength, int theSeqNum) {
			myDataArr = new byte[dataLength];
			myTimestamp = timestamp;
			mySeqNum = theSeqNum;

			System.arraycopy(dataArr, dataOffset, myDataArr, 0, dataLength);
		}

		/**
		 * Gets the timestamp
		 * 
		 * @return the timestamp
		 */
		public long getTimeStamp() {
			return myTimestamp;
		}

		/**
		 * Gets the sequence number
		 * 
		 * @return the sequence number
		 */
		public int getSeqNum() {
			return mySeqNum;
		}

		/**
		 * Gets the data
		 * 
		 * @return the data byte array
		 */
		public byte[] getData() {
			return myDataArr;
		}
	}

	public int dropOneRtpTextSeqNo() {
		sequenceNumber++;
		return (int) (sequenceNumber - 1);
	}
}