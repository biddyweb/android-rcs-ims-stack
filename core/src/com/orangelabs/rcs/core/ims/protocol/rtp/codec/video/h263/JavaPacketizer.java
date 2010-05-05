package com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.VideoCodec;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;

/**
 * Reassembles H263+ RTP packets into H263+ frames, as per RFC 4629
 */
public class JavaPacketizer extends VideoCodec {
	/**
	 * Because packets can come out of order, it is possible that some packets for a newer frame
	 * may arrive while an older frame is still incomplete.  However, in the case where we get nothing
	 * but incomplete frames, we don't want to keep all of them around forever.
	 */
	public JavaPacketizer(){
	}
	
	public int process(Buffer input, Buffer output){		
		if (!input.isDiscard())	{			
			// Add H263+ RTP header
			/*	      0                   1
				      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
				     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
				     |   RR    |P|V|   PLEN    |PEBIT|		+ 2 null bytes
				     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
					----------------------------------------
					|0000 0100|0000 0000|0000 0000|0000 0000|
					----------------------------------------
					Only bit set is P = 1
			*/		
			byte h263header[] = new byte[2];
			h263header[0]= 0x04;
			h263header[1]= 0x00;
			
			byte[] bufferData = (byte[])input.getData();
			byte data[] = new byte[bufferData.length];
			// write h263 payload
			for (int i=0;i<bufferData.length;i++){
					data[i]=bufferData[i];
			}
			// Trick : we overwrite the first two bytes of payload (null bytes) by h263 header 
			// Write h263 header
			for (int i=0;i<h263header.length;i++){
					data[i]=h263header[i];
			}
						
			if (data.length > 0){
				// Copy to buffer
				output.setFormat(input.getFormat());
				output.setData(data);
				output.setLength(data.length);
				output.setOffset(0);
				output.setTimeStamp(input.getTimeStamp());
				output.setFlags(Buffer.FLAG_RTP_MARKER | Buffer.FLAG_RTP_TIME);
			}
			return BUFFER_PROCESSED_OK;
		}else{
			output.setDiscard(true);
			return OUTPUT_BUFFER_NOT_FILLED;
		}		
	}
	
}

	