/*
 * T.140 Presentation Library
 * 
 * Copyright (C) 2004-2008 Board of Regents of the University of Wisconsin System
 * (Univ. of Wisconsin-Madison, Trace R&D Center)
 * Copyright (C) 2004-2008 Omnitor AB
 *
 * This software was developed with support from the National Institute on
 * Disability and Rehabilitation Research, US Dept of Education under Grant
 * # H133E990006 and H133E040014  
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Please send a copy of any improved versions of the library to: 
 * Gunnar Hellstrom, Omnitor AB, Renathvagen 2, SE 121 37 Johanneshov, SWEDEN
 * Gregg Vanderheiden, Trace Center, U of Wisconsin, Madison, Wi 53706
 *
 */
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








