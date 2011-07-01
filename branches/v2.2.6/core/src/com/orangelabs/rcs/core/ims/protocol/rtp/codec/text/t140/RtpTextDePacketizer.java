/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright © 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.core.ims.protocol.rtp.codec.text.t140;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Extracts data from incoming RTP-Text packets. <br>
 * Also handles missing packets. <br>
 * <br>
 *
 * @author Erik Zetterstrom, Omnitor AB
 * @author Andreas Piirimets, Omnitor AB
 */
public class RtpTextDePacketizer {

    //Masks for depacketizer

    /**
     * Masks out the upper part of the block length from the header.
     */
    public static final int RTP_DEPACK_BLOCKLEN_UPPER_MASK = 0x3  << 0;

    /**
     * Masks out the lower part of the block lenght from the header.
     */
    public static final int RTP_DEPACK_BLOCKLEN_LOWER_MASK = 0xff << 0;

    /**
     * Can be used to sign an unsigned value etc.
     */
    public static final int SIGNED_MASK                    = 0x80;

    //The sequence number of the last received packet.
    private long lastSequenceNumber    = 0;

    //The sequence number of the last output packet
    private long lastOutput            = 0;

    private Hashtable<Long, Long> missingPackets  = null;
    private Hashtable<Long, byte[]> receivedPackets = null;

    private int redundantGenerations = 0;
    private boolean redFlagIncoming = false;

    private int t140PayloadType;
    private byte signedT140PayloadType;

    private Timer timer = null;

    private boolean firstPacket = true;

    private Logger logger;

    private long ssrc;


    /**
     * Initializes the depacketizer and all formats.
     *
     * @param t140PayloadType Payload type to use.
     * @param redFlagIncoming Indicates if redundancy is on or off.
     */
    public RtpTextDePacketizer(int t140PayloadType, int redPt,
			       boolean redFlagIncoming) {

	logger = Logger.getLogger("se.omnitor.protocol.rtp.text");

        this.t140PayloadType = t140PayloadType;
        this.redFlagIncoming = redFlagIncoming;

        timer = new Timer();

        signedT140PayloadType = (byte)((byte)t140PayloadType | (byte)0x80);

        missingPackets  = new Hashtable<Long, Long>(10);
        receivedPackets = new Hashtable<Long, byte[]>(30);

    }

    /**
     * Destructor. Preforms cleanup.
     *
     */
    protected void finalize() {
        timer.cancel();
    }


    /**
     * Removes excess zeros that are received in the input buffer.
     *
     * @param in The received data
     *
     * @return Received data without excess zeros.
     */
    private byte[] filterZeros(byte[] in) {
        byte[] filtered = null;
        int start=0;
        int end=in.length;
        boolean lastData=true;

        //Find start of data
        int i=0;
	boolean foundData = false;
	for (i=0;i<in.length;i++) {
            if (in[i]!=0) {
                start=i;
		foundData = true;
                break;
            }
        }

        //No data found
	// Changed by Andreas Piirimets 2004-02-12
        // if (i==(in.length-1)) {
	if (!foundData) {
            return new byte[0];
        }

        //Find end of data
        for (int j=start;j<in.length;j++) {
            if (in[j]==0) {
                for (int k=(j+1);k<in.length;k++) {
                    if (in[k]!=0) {
                        lastData=false;
                        break;
                    }
                }
                if (lastData) {
		    // Changed by Andreas Piirimets 2004-02-22
		    // end=j-1;
                    end=j;
                    break;
                }
                lastData=true;
            }
        }

	// Changed by Andreas Piirimets 2004-02-11
        // int length = end-start+1;
        int length = end-start;
        filtered = new byte[length];

	java.lang.System.arraycopy(in,start,filtered,0,length);

        return filtered;
    }

    /**
     * Extracts data from received packets. Handles missing packets.
     *
     * @param inputBuffer  The received packet
     * @param outputBuffer The extracted data
     *
     * @return 1 if success
     * @return 0 if parse failure
     * @return -1 packet received out of order.
     */
    public synchronized int decode(RtpTextBuffer inputBuffer,
				   RtpTextBuffer outputBuffer) {

        long currentSequenceNumber = inputBuffer.getSequenceNumber();
        long currentTimeStamp      = inputBuffer.getTimeStamp();

        byte[] outData    = new byte[0];
        byte[] newData    = null;
        byte[] oldOutData = null;
        byte[] bufferData = new byte[inputBuffer.getLength()];
	byte[] rawData    =(byte[])inputBuffer.getData();


	//Get the data from the buffer
	System.arraycopy((byte[])inputBuffer.getData(),
			 inputBuffer.getOffset(),
			 bufferData,
			 0,
			 inputBuffer.getLength());


	//Get rid of any zeros
	byte[] data = filterZeros(bufferData);

	if (redFlagIncoming) {
	    redundantGenerations=getRedundantGenerations(data);
	} else {
	    redundantGenerations=0;
	}


        //First packet received
        if (firstPacket) {
            firstPacket = false;
            lastSequenceNumber = currentSequenceNumber - 1;
            lastOutput = lastSequenceNumber;
	    ssrc = inputBuffer.getSsrc();
        }

        //Check for sequencenumber wraparound
        else if (lastSequenceNumber >
                 T140Constants.MAX_SEQUENCE_NUMBER-
                 T140Constants.WRAP_AROUND_MARGIN &&
                 currentSequenceNumber <
                 T140Constants.WRAP_AROUND_MARGIN) {

            //No packets lost
            if (lastSequenceNumber == T140Constants.MAX_SEQUENCE_NUMBER &&
                currentSequenceNumber==0) {
                lastSequenceNumber=currentSequenceNumber-1;
            }

            //Lost packets
            else {
                lastSequenceNumber=lastSequenceNumber-
                    T140Constants.MAX_SEQUENCE_NUMBER;
            }
        }

	// If wrong SSRC, ignore
	if (inputBuffer.getSsrc() != ssrc) {
	    outputBuffer.setData(new byte[0]);
	    return 1;
	}

        //Packet received in order.
        if (currentSequenceNumber == (lastSequenceNumber+1)) {

            byte[] d = getData(0, data);
            receivedPackets.put(Long.valueOf(currentSequenceNumber),(byte [])d);
            d=(byte[])receivedPackets.get(Long.valueOf(currentSequenceNumber));
            lastSequenceNumber = currentSequenceNumber;

        }

        //New packet(s) missing.
        else if ((currentSequenceNumber-lastSequenceNumber)>0) {
            receivedPackets.put(Long.valueOf(currentSequenceNumber),
                                getData(0,data));
            for (int i=(int)(currentSequenceNumber - lastSequenceNumber)-1;
                 i>0;
                 i--) {
                if (!(missingPackets.containsKey(
                      Long.valueOf(currentSequenceNumber-i)))) {

                    LossTimerTask ltt =
			new LossTimerTask(currentSequenceNumber-i, this);
                    if (redFlagIncoming) {
                        timer.schedule((TimerTask)ltt,
                              T140Constants.WAIT_FOR_MISSING_PACKET_RED);
                    } else {
                        timer.schedule((TimerTask)ltt,
                                       T140Constants.WAIT_FOR_MISSING_PACKET);
                    }
                    missingPackets.put(Long.valueOf(currentSequenceNumber-i),
                                       Long.valueOf(currentTimeStamp));
                }
            }
            lastSequenceNumber = currentSequenceNumber;
        }

        //Packet received out of order
        else {

        }

        //Check if received packet is missing.
        //Check if the redundant data in the received packet can be used to
        //restore missing packets.
        for (int i=0;i<=redundantGenerations;i++) {
            receivedMissingPacket(currentSequenceNumber-i,i,data);
        }

        //Output data if possible.
        //Get packets in order from last output.
        boolean rKey=receivedPackets.containsKey(Long.valueOf(lastOutput+1));
	byte[] lastData = null;
        while (rKey) {// || !mKey) {

            //Get packets that are ready
            if (rKey) {
                oldOutData=outData;
                newData   =(byte[])receivedPackets.get(Long.valueOf(lastOutput+1));

		if (!(lastData == T140Constants.LOSS_CHAR &&
		      newData == T140Constants.LOSS_CHAR)) {

		    outData   =new byte[oldOutData.length + newData.length];
		    System.arraycopy(oldOutData,
				     0,
				     outData,
				     0,
				     oldOutData.length);
		    System.arraycopy(newData,
				     0,
				     outData,
				     oldOutData.length,
				     newData.length);

		    lastData = newData;
		}

		lastOutput++;
		rKey=receivedPackets.containsKey(Long.valueOf(lastOutput+1));

            }
        }



        //Receives the T.140 defined character 0xFEFF
        //ZERO_WIDTH_BREAK_SPACE
        /*TextConstants.printDebug("TextDePacketizer out: ",4);
        for (int k=0;k<outData.length;k++) {
            if(outData[k]==TextConstants.ZERO_WIDTH_NO_BREAK_SPACE[0] &&
               k<(outData.length-1) &&
               outData[k+1]==TextConstants.ZERO_WIDTH_NO_BREAK_SPACE[1]) {
                byte[] bTemp = outData;
                outData = new byte[bTemp.length-2];
                int outInd=0;
                if((bTemp.length-2)>0) {
                    for(int l=0;l<bTemp.length;l++) {
		    if(bTemp[l]!=TextConstants.ZERO_WIDTH_NO_BREAK_SPACE[0] &&
		    bTemp[l]!=Te //filterZeros((byte[])inputBuffer.getData());	xtConstants.ZERO_WIDTH_NO_BREAK_SPACE[1]) {
		    outData[outInd] = bTemp[l];
		    outInd++;
                        }
                    }
                }
                break;
            }

            TextConstants.printDebug(""+outData[k],4);
            }*/

        outputBuffer.setData(outData);
        data=null;

	//Make sure the buffer is cleared!
	for (int k=0;k<rawData.length;k++) {
	    rawData[k]=0;
	}
        inputBuffer.setData(rawData);

        return 1;
    }

    /**
     * Find out how many redundantGenerations there are in the received packet.
     *
     * @param data The packet.
     *
     * @return The number of redundant generations in this packet.
     */
    public int getRedundantGenerations(byte[] data) {

	int walker = 0;
	int redGens = 0;

	while (data[walker] == signedT140PayloadType) {
	    redGens++;
	    walker += T140Constants.REDUNDANT_HEADER_SIZE;
	}

	if (data[walker] != t140PayloadType) {
	    logger.warning("Malformed redundancy in RTP text packet, could " +
			   "not fint primary data!");

	    redGens = 0;
	}

	return redGens;

    }

    /**
     * Utility function that extracts the data associated with a certain
     * redundant generation. Generation 0 extracts the primary data.
     *
     * @param generation The generation of data to be extracted.
     * @param data       The packet.
     *
     * @return The extracted data. null if invalid generation.
     */
    private byte[] getData(int generation,byte[] data) {
        byte blockLengthByteHigh = 0x00;
        byte blockLengthByteLow  = 0x00;
        long startLength         = 0;
        long blockLength         = 0;

        byte[] extractedData     = null;

        long[] blockLengths      = new long[redundantGenerations];


        //No redundancy

        //Parse the length of the individual blocks.
        for (int i=0;i<redundantGenerations;i++) {
	    blockLengthByteHigh =
		data[T140Constants.REDUNDANT_HEADER_SIZE*i+2];
            blockLengthByteLow =
		data[T140Constants.REDUNDANT_HEADER_SIZE*i+3];
            blockLength = (long)(((blockLengthByteHigh &
				   RTP_DEPACK_BLOCKLEN_UPPER_MASK) << 8)
				 | (blockLengthByteLow &
				    RTP_DEPACK_BLOCKLEN_LOWER_MASK));
            blockLengths[i] = blockLength;

        }

        //Each generation takes 4 bytes header space + end header 1 byte.
        if (redundantGenerations>0) {
            startLength = redundantGenerations*
                T140Constants.REDUNDANT_HEADER_SIZE+
                T140Constants.PRIMARY_HEADER_SIZE;
        }

	//EZ 041114: Removed below, easier to use the stored
	// blocklengths above.
        //Get the correct startpoint for data.
        //tempGeneration=redundantGenerations-generation;

        //Find start index for primary data.
        /*for (int i=0;i<tempGeneration;i++) {
            blockLengthByteHigh=data[TextConstants.REDUNDANT_HEADER_SIZE*(i)+2];
            blockLengthByteLow =data[TextConstants.REDUNDANT_HEADER_SIZE*(i)+3];
            startLength += (long)(((blockLengthByteHigh &
                                    RTP_DEPACK_BLOCKLEN_UPPER_MASK)<<8 ) |
                                  (blockLengthByteLow &
                                   RTP_DEPACK_BLOCKLEN_LOWER_MASK)) ;
				   }*/

	//Extract primary data.
        if (generation==0) {
	    long primaryStart = startLength;

	    for (int i=0;i<blockLengths.length;i++) {
		primaryStart+=blockLengths[i];
	    }

            extractedData = new byte[(int)(data.length-primaryStart)];
            System.arraycopy(data,
                             (int)primaryStart,
                             extractedData,
                             0,
                             (data.length-(int)primaryStart));

            return extractedData;
        }

	//EZ: Removed below. Use the stored blocklengths.
        //Get length of wanted block.
        /*blockLengthByteHigh = data[TextConstants.REDUNDANT_HEADER_SIZE*
                                   (redundantGenerations-generation)+2];
        blockLengthByteLow  = data[TextConstants.REDUNDANT_HEADER_SIZE*
                                   (redundantGenerations-generation)+3];
        blockLength         = (long)(((blockLengthByteHigh &
                                       RTP_DEPACK_BLOCKLEN_UPPER_MASK) << 8) |
                                     (blockLengthByteLow &
				     RTP_DEPACK_BLOCKLEN_LOWER_MASK));*/

	//EZ 041114: Get the blocklength of the redundant generation
	blockLength = blockLengths[redundantGenerations-generation];
	for (int i=0;i<(redundantGenerations-generation);i++)
	    startLength +=blockLengths[i];

        //Extract wanted block.
        extractedData = new byte[(int)blockLength]; //POSSIBLE LOSS
        System.arraycopy(data,
                         (int)startLength,
                         extractedData,
                         0,
                         (int)blockLength);

        return extractedData;
    }


    /**
     * Converts received UTF-8 text to the desired format.
     *
     * @param utf8Bytes The received text in byte format.
     * @param encoding The desired format.
     *
     * @return Text in the desired format.
     */
    /*
    private byte[] fromUTF8(byte[] utf8Bytes, String encoding) {
        String utf8 = null;
        byte[] textBytes = null;

        try {
            utf8 = new String(utf8Bytes,"UTF-8");
            textBytes = utf8.getBytes(encoding);
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }

        return textBytes;
    }
    */


    /**
     * Function to handle the reception of missingpackets.
     *
     * @param sequenceNumber The sequenceNumber of the recieved packet.
     * @param i Redundant generation of the received packet that contains the
     * desired data.
     * @param data The data of the received packet.
     */
    public void receivedMissingPacket(long sequenceNumber, int i,byte[] data) {
	if (missingPackets.containsKey(Long.valueOf(sequenceNumber))) {

	    missingPackets.remove(Long.valueOf(sequenceNumber));
            if (!receivedPackets.containsKey(Long.valueOf(sequenceNumber))) {
                receivedPackets.put(new Long(sequenceNumber),
                                    (byte[])getData((i),
                                                    data));
            }
        }
    }

    //EZ: 041114
    /**
     * Function to handle lost packets. Adds the LOSS CHAR to output.
     *
     * @param sequenceNumber The sequence number of the lost packet.
     */
    public void lostPacket(long sequenceNumber) {
	missingPackets.remove(Long.valueOf(sequenceNumber));
	if (!receivedPackets.containsKey(Long.valueOf(sequenceNumber))) {
	    byte[] dataToAdd = T140Constants.LOSS_CHAR;

	    if (receivedPackets.containsKey(Long.valueOf(sequenceNumber+1)) &&
		(receivedPackets.get(Long.valueOf(sequenceNumber+1)) ==
		 T140Constants.LOSS_CHAR)) {

		dataToAdd = new byte[0];

	    }
	    else if (receivedPackets.containsKey(Long.valueOf(sequenceNumber-1)) &&
		     (receivedPackets.get(Long.valueOf(sequenceNumber-1)) ==
		      T140Constants.LOSS_CHAR)) {

		dataToAdd = new byte[0];

	    }

	    receivedPackets.put(Long.valueOf(sequenceNumber), dataToAdd);

	}
    }


    /**
     * Inner class that defines what to do when a packet is lost.
     *
     * @author Andreas Piirimets, Omnitor AB
     */
    private class LossTimerTask extends TimerTask {

        private long sequenceNumber = 0;
        private RtpTextDePacketizer parent = null;

        /**
         * Create a new LossTimerTask.
         *
         * @param seq The sequence number of the missingpacket.
         * @param parent The creator of this object.
         */
        public LossTimerTask(long seq, RtpTextDePacketizer parent) {
            sequenceNumber=seq;
            this.parent=parent;
        }


        /**
         * Preform the work.
	 *
         */
        public void run() {

	    //EZ: Removed below. Unessecary to make parseable data.
	    //    Just add LOSS CHAR to output.

	    // Changed by Andreas Piirimets 2004-03-11
	    // The LOSS_CHAR has to be sent in UTF-8 format
            //parent.receivedMissingPacket(sequenceNumber,
            //                             0,
            //                             TextConstants.LOSS_CHAR);
	    //try {

	    //parent.receivedMissingPacket
	    //    (sequenceNumber,
	    ///     0,
	    //     TextConstants.LOSS_CHAR);
		     //(new String(""+TextConstants.LOSS_CHAR_CHAR)).
		     //getBytes("UTF-8"));
		//}
		//catch (java.io.UnsupportedEncodingException uee) {
		//parent.receivedMissingPacket
		//    (sequenceNumber,
		//     0,
		//     new byte[0]);
		//}
	    // End of change

	    //EZ 041114: Add LOSS CHAR to output.
	    parent.lostPacket(sequenceNumber);

            this.cancel();
        }

    }

}