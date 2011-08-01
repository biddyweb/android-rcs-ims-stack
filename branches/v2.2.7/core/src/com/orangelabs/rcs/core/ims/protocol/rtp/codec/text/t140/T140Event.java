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

/**
 * This class carries a T.140 event. <br>
 * <br>
 * Signalling to and from the packetizer and depacketizer is done with this
 * event.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class T140Event {

    /**
     * Defines event type "text". Transfer text to display as text in the
     * receiving window of the peer terminal(s). <br>
     * <br>
     * Data carries a String.
     */
    public static final int TEXT = 1;

    /**
     * Defines event type "alert user in session". Intended to caues an
     * alerting indication at the receiving terminal during a session. <br>
     * <br>
     * Data carries null.
     */
    public static final int BELL = 2;

    /**
     * Defines event type "erase last character". Erase the last character
     * sent from the display at the receiving end. <br>
     * <br>
     * Data carries null.
     */
    public static final int BS = 3;

    /**
     * Defines event type "new line". Move the insertion point for text to
     * the beginning of the next line in the display window. <br>
     * <br>
     * Data carries null.
     */
    public static final int NEW_LINE = 4;

    /**
     * Defines event type "new line". A supported, but not preferred way of
     * requesting a new line. <br>
     * <br>
     * Data carries null.
     */
    public static final int CR_LF = 5;

    /**
     * Defines event type "interrupt". To initiate a request for a mode
     * change. <br>
     * <br>
     * Data carries a String.
     */
    public static final int INT = 6;

    /**
     * Defines event type "select graphic rendition". Propose display
     * attributes for the following text. <br>
     * <br>
     * Data carries a String.
     */
    public static final int SGR = 7;

    /**
     * Defines event type "application protocol function". Identified coding
     * of extensions to the protocol, so that they can be introduced
     * unilaterally without disturbing the display. <br>
     * <br>
     * Data carries a String.
     */
    public static final int SOS_ST = 8;

    /**
     * Defines event type "identify UCS subset". Announce an intention to use
     * a standardized subset of ISO 10646. <br>
     * <br>
     * Data carries a String.
     */
    public static final int identifyUcsSubset = 9;

    private int type;
    private Object data;

    /**
     * Initializes.
     *
     * @param type The event type, expressed as one of the constants in this
     * class.
     * @param data Data associated with this event.
     */
    public T140Event(int type, Object data) {

	this.type = type;
	this.data = data;
    }
   
    /**
     * Gets the event type, expressed as one of the constants in this class.
     *
     * @return The event type.
     */
    public int getType() {
	return type;
    }

    /**
     * Gets data associated to this event. The object type differs for each
     * event, see details in the description for each constant.
     *
     * @param Event data.
     */
    public Object getData() {
	return data;
    }
}








