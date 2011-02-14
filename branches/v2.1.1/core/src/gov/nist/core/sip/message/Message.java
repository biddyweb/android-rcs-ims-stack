/*******************************************************************************
 * Conditions Of Use
 * 
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 ******************************************************************************/
package gov.nist.core.sip.message;

import gov.nist.core.GenericObject;
import gov.nist.core.ParseException;
import gov.nist.core.Separators;
import gov.nist.core.Utils;
import gov.nist.core.sip.header.CSeqHeader;
import gov.nist.core.sip.header.CallIdHeader;
import gov.nist.core.sip.header.ContactList;
import gov.nist.core.sip.header.ContentLengthHeader;
import gov.nist.core.sip.header.ContentTypeHeader;
import gov.nist.core.sip.header.FromHeader;
import gov.nist.core.sip.header.Header;
import gov.nist.core.sip.header.HeaderList;
import gov.nist.core.sip.header.RecordRouteList;
import gov.nist.core.sip.header.RequestLine;
import gov.nist.core.sip.header.RouteList;
import gov.nist.core.sip.header.StatusLine;
import gov.nist.core.sip.header.ToHeader;
import gov.nist.core.sip.header.ViaHeader;
import gov.nist.core.sip.header.ViaList;
import gov.nist.core.sip.parser.HeaderParser;
import gov.nist.core.sip.parser.ParserFactory;
import gov.nist.core.sip.parser.PipelinedMsgParser;
import gov.nist.core.sip.parser.StringMsgParser;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * This is the main SIP Message structure.
 * 
 * @see StringMsgParser
 * @see PipelinedMsgParser
 * 
 * 
 * @version JAIN-SIP-1.1
 * 
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * 
 */

public abstract class Message extends GenericObject {
	
	public static final int REQUEST = 0;
	public static final int RESPONSE = 1;

	private int type;

	private long timestamp = System.currentTimeMillis();

	private static HeaderList sipHeaderList;

	protected static final String DEFAULT_ENCODING = "UTF-8";
	
	/**
	 * unparsed headers
	 */
	protected Vector unrecognizedHeaders;

	/**
	 * List of parsed headers (in the order they were added)
	 */
	protected Vector headers;

	/** Direct accessors for frequently accessed headers * */
	protected FromHeader fromHeader;

	protected ToHeader toHeader;

	protected CSeqHeader cSeqHeader;

	protected CallIdHeader callIdHeader;

	protected ContentLengthHeader contentLengthHeader;

	// protected MaxForwards maxForwardsHeader;

	// Payload
	protected String messageContent;

	protected byte[] messageContentBytes;

	protected Object messageContentObject;

	static {
		sipHeaderList = new HeaderList();
	}

	// Table of headers indexed by name.
	private Hashtable nameTable;

	/**
	 * Return true if the header belongs only in a Request.
	 * 
	 * @param sipHeader
	 *            is the header to test.
	 */
	public static boolean isRequestHeader(Header sipHeader) {
		return sipHeader.getHeaderName().equals(Header.ALERT_INFO)
				|| sipHeader.getHeaderName().equals(Header.IN_REPLY_TO)
				|| sipHeader.getHeaderName().equals(Header.AUTHORIZATION)
				|| sipHeader.getHeaderName().equals(Header.MAX_FORWARDS)
				|| sipHeader.getHeaderName().equals(Header.PRIORITY)
				|| sipHeader.getHeaderName().equals(Header.PROXY_AUTHORIZATION)
				|| sipHeader.getHeaderName().equals(Header.PROXY_REQUIRE)
				|| sipHeader.getHeaderName().equals(Header.ROUTE)
				|| sipHeader.getHeaderName().equals(Header.SUBJECT);

	}

	/**
	 * Return true if the header belongs only in a response.
	 * 
	 * @param sipHeader
	 *            is the header to test.
	 */
	public static boolean isResponseHeader(Header sipHeader) {
		return sipHeader.getHeaderName().equals(Header.ERROR_INFO)
				|| sipHeader.getHeaderName().equals(Header.PROXY_AUTHENTICATE)
				|| sipHeader.getHeaderName().equals(Header.SERVER)
				|| sipHeader.getHeaderName().equals(Header.UNSUPPORTED)
				|| sipHeader.getHeaderName().equals(Header.RETRY_AFTER)
				|| sipHeader.getHeaderName().equals(Header.WARNING)
				|| sipHeader.getHeaderName().equals(Header.WWW_AUTHENTICATE);

	}

	/**
	 * Get A dialog identifier constructed from this messsage. This is an id
	 * that can be used to identify dialogs.
	 * 
	 * @param isServerTransaction
	 *            is a flag that indicates whether this is a server transaction.
	 */
	public abstract String getDialogId(boolean isServerTransaction);
	
	/**
	 * Encode this message as a string. This is more efficient when the payload
	 * is a string (rather than a binary array of bytes). If the payload cannot
	 * be encoded as a UTF-8 string then it is simply ignored (will not appear
	 * in the encoded message).
	 * 
	 * @return The Canonical String representation of the message (including the
	 *         canonical string representation of the SDP payload if it exists).
	 */
	public String encode() {
		StringBuffer encoding = new StringBuffer();
		// Synchronization added because of concurrent modification exception
		// noticed by Lamine Brahimi.
		synchronized (this.headers) {
			Enumeration it = this.headers.elements();

			while (it.hasMoreElements()) {
				Header siphdr = (Header) it.nextElement();
				if (!(siphdr instanceof ContentLengthHeader))
					encoding.append(siphdr.encode());
			}
		}

		// Add the content-length header
		if (contentLengthHeader != null)
			encoding.append(contentLengthHeader.encode()).append(
					Separators.NEWLINE);

		if (this.messageContentObject != null) {
			String mbody = this.getContent().toString();
			encoding.append(mbody);
		} else if (this.messageContent != null
				|| this.messageContentBytes != null) {
			String content = null;
			try {
				if (messageContent != null)
					content = messageContent;
				else
					content = new String(messageContentBytes, DEFAULT_ENCODING);
			} catch (Exception ex) {
				content = "";
			}
			encoding.append(content);
		}
		return encoding.toString();
	}

	/**
	 * Encode the message as a byte array. Use this when the message payload is
	 * a binary byte array.
	 * 
	 * @return The Canonical byte array representation of the message (including
	 *         the canonical byte array representation of the SDP payload if it
	 *         exists all in one contiguous byte array).
	 * 
	 */
	public byte[] encodeAsBytes() {
		StringBuffer encoding = new StringBuffer();
		Enumeration it = this.headers.elements();

		while (it.hasMoreElements()) {
			Header siphdr = (Header) it.nextElement();
			if (!(siphdr instanceof ContentLengthHeader))
				encoding.append(siphdr.encode());

		}
		byte[] retval = null;
		byte[] content = this.getRawContent();
		if (content != null) {
			encoding.append(Header.CONTENT_LENGTH + Separators.COLON
					+ Separators.SP + content.length + Separators.NEWLINE);
			encoding.append(Separators.NEWLINE);
			// Append the content
			byte[] msgarray = null;
			try {
				msgarray = encoding.toString().getBytes("UTF-8");
			} catch (Exception ex) {
			}

			retval = new byte[msgarray.length + content.length];
			System.arraycopy(msgarray, 0, retval, 0, msgarray.length);
			System.arraycopy(content, 0, retval, msgarray.length,
					content.length);
		} else {
			// Message content does not exist.
			encoding.append(Header.CONTENT_LENGTH + Separators.COLON
					+ Separators.SP + '0' + Separators.NEWLINE);
			encoding.append(Separators.NEWLINE);
			try {
				retval = encoding.toString().getBytes("UTF-8");
			} catch (Exception ex) {
			}
		}
		return retval;
	}

	/**
	 * clone this message (create a new deep physical copy). All headers in the
	 * message are cloned. You can modify the cloned copy without affecting the
	 * original.
	 * 
	 * @return A cloned copy of this object.
	 */
	public Object clone() {
		Message retval = null;
		try {
			retval = (Message) this.getClass().newInstance();
		} catch (IllegalAccessException ex) {
		} catch (InstantiationException ex) {
		}
		Enumeration li = headers.elements();
		while (li.hasMoreElements()) {
			try {
				Header sipHeader = (Header) ((Header) li.nextElement()).clone();
				retval.attachHeader(sipHeader);
			} catch (ParseException ex) {
			}
		}
		if (retval instanceof Request) {
			Request thisRequest = (Request) this;
			RequestLine rl = (RequestLine) (thisRequest.getRequestLine())
					.clone();
			((Request) retval).setRequestLine(rl);
		} else {
			Response thisResponse = (Response) this;
			StatusLine sl = (StatusLine) (thisResponse.getStatusLine()).clone();
			((Response) retval).setStatusLine(sl);
		}

		if (this.getContent() != null) {
			try {
				retval.setContent(this.getContent(), this
						.getContentTypeHeaderHeader());
			} catch (ParseException ex) {
				/** Ignore * */
			}
		}

		return retval;
	}

	/**
	 * 
	 * Constructor: Initializes lists and list headers. All the headers for
	 * which there can be multiple occurances in a message are derived from the
	 * HeaderListClass. All singleton headers are derived from Header class.
	 * 
	 */
	public Message(int type) {
		this.type = type;
		this.unrecognizedHeaders = new Vector();
		this.headers = new Vector();
		nameTable = new Hashtable();
	}

	/**
	 * Is a SIP request
	 * 
	 * @return Boolean
	 */
	public boolean isRequest() {
		return (type == REQUEST);
	}
	
	/**
	 * Is a SIP response
	 * 
	 * @return Boolean
	 */
	public boolean isResponse() {
		return (type == RESPONSE);
	}

	/**
	 * Attach a header and die if you get a duplicate header exception.
	 * 
	 * @param h
	 *            Header to attach.
	 */
	private void attachHeader(Header h) throws ParseException,
			IllegalArgumentException {
		if (h == null)
			throw new IllegalArgumentException("null header!");
		if (h instanceof HeaderList) {
			HeaderList hl = (HeaderList) h;
			if (hl.isEmpty()) {
				return;
			}
		}
		attachHeader(h, false, false);
	}

	/**
	 * Attach a header (replacing the original header).
	 * 
	 * @param header
	 *            Header that replaces a header of the same type.
	 */
	public void setHeader(Header sipHeader) throws IllegalArgumentException {
		Header header = (Header) sipHeader;
		if (header == null)
			throw new IllegalArgumentException("null header!");
		if (header instanceof HeaderList) {
			HeaderList hl = (HeaderList) header;
			// Ignore empty lists.
			if (hl.isEmpty())
				return;
		}
		this.removeHeader(header.getHeaderName());

		attachHeader(header, true, false);

	}

	/**
	 * Set a header from a linked list of headers.
	 * 
	 * @param headers --
	 *            a list of headers to set.
	 */

	public void setHeaders(Vector headers) {
		Enumeration elements = headers.elements();
		while (elements.hasMoreElements()) {
			Header sipHeader = (Header) elements.nextElement();
			this.attachHeader(sipHeader, false);
		}
	}

	/**
	 * Attach a header to the end of the existing headers in this Message
	 * structure. This is equivalent to the
	 * attachHeader(Header,replaceflag,false); which is the normal way in which
	 * headers are attached. This was added in support of JAIN-SIP.
	 * 
	 * @since 1.0 (made this public)
	 * @param h
	 *            header to attach.
	 * @param replaceflag
	 *            if true then replace a header if it exists.
	 */
	public void attachHeader(Header h, boolean replaceflag) {

		this.attachHeader(h, replaceflag, false);

	}

	/**
	 * Attach the header to the SIP Message structure at a specified position in
	 * its list of headers.
	 * 
	 * @param header
	 *            Header to attach.
	 * @param replaceFlag
	 *            If true then replace the existing header.
	 * @param index
	 *            Location in the header list to insert the header.
	 */

	public void attachHeader(Header header, boolean replaceFlag, boolean top) {
		if (header == null) {
			throw new NullPointerException("null header");
		}

		Header h;

		if (ListMap.hasList(header)	&& !sipHeaderList.getClass().isAssignableFrom(header.getClass())) {
			HeaderList hdrList = ListMap.getList(header);
			hdrList.add(header);
			h = hdrList;
		} else {
			h = header;
		}

		if (replaceFlag) {
			nameTable.remove(header.getHeaderName().toLowerCase());
		} else if (nameTable.containsKey(header.getHeaderName().toLowerCase())
				&& !(h instanceof HeaderList)) {
			return; // silently ignore duplicate.
		}

// Modif IOT: multiple VIa list was not supported 
/*		Header originalHeader = (Header) getHeader(header.getHeaderName());
		// Delete the original header from our list structure.
		int index = -1;
		if (originalHeader != null) {
			Enumeration li = headers.elements();

			while (li.hasMoreElements()) {
				Header next = (Header) li.nextElement();
				index++;
				if (next.equals(originalHeader)) {
					break;
				}
			}
		}
		if (index != -1 && index < headers.size()) {
			headers.removeElementAt(index);
		}*/

		if (getHeader(header.getHeaderName()) == null) {
			nameTable.put(header.getHeaderName().toLowerCase(), h);
			headers.addElement(h);
		} else {
			if (h instanceof HeaderList) {
				HeaderList hdrlist = (HeaderList) nameTable.get(header
						.getHeaderName().toLowerCase());
				if (hdrlist != null)
					hdrlist.concatenate((HeaderList) h, top);
				else
					nameTable.put(h.getHeaderName().toLowerCase(), h);
			} else {
				nameTable.put(h.getHeaderName().toLowerCase(), h);
			}
		}

		// Direct accessor fields for frequently accessed headers.
		if (h instanceof FromHeader) {
			this.fromHeader = (FromHeader) h;
		} else if (h instanceof ContentLengthHeader) {
			this.contentLengthHeader = (ContentLengthHeader) h;
		} else if (h instanceof ToHeader) {
			this.toHeader = (ToHeader) h;
		} else if (h instanceof CSeqHeader) {
			this.cSeqHeader = (CSeqHeader) h;
		} else if (h instanceof CallIdHeader) {
			this.callIdHeader = (CallIdHeader) h;
		}
	}

	/**
	 * Remove a header given its name. If multiple headers of a given name are
	 * present then the top flag determines which end to remove headers from.
	 * 
	 * @param headerName
	 *            is the name of the header to remove.
	 * @param top --
	 *            flag that indicates which end of header list to process.
	 */
	public void removeHeader(String headerName, boolean top) {
		Header toRemove = (Header) nameTable.get(headerName.toLowerCase());
		// nothing to do then we are done.
		if (toRemove == null)
			return;
		if (toRemove instanceof HeaderList) {
			HeaderList hdrList = (HeaderList) toRemove;
			if (top)
				hdrList.removeFirst();
			else
				hdrList.removeLast();
			// Clean up empty list
			if (hdrList.isEmpty()) {
				Enumeration li = this.headers.elements();
				int index = -1;
				while (li.hasMoreElements()) {
					Header sipHeader = (Header) li.nextElement();
					index++;
					if (Utils.equalsIgnoreCase(sipHeader.getName(), headerName))
						break;
				}
				if (index != -1 && index < this.headers.size())
					headers.removeElementAt(index);
			}
		} else {
			this.nameTable.remove(headerName.toLowerCase());
			if (toRemove instanceof FromHeader) {
				this.fromHeader = null;
			} else if (toRemove instanceof ToHeader) {
				this.toHeader = null;
			} else if (toRemove instanceof CSeqHeader) {
				this.cSeqHeader = null;
			} else if (toRemove instanceof CallIdHeader) {
				this.callIdHeader = null;
			} else if (toRemove instanceof ContentLengthHeader) {
				this.contentLengthHeader = null;
			}
			Enumeration li = this.headers.elements();
			int index = -1;
			while (li.hasMoreElements()) {
				Header sipHeader = (Header) li.nextElement();
				index++;
				if (Utils.equalsIgnoreCase(sipHeader.getName(), headerName))
					break;
			}
			if (index != -1 && index < this.headers.size())
				this.headers.removeElementAt(index);
		}

	}

	/**
	 * Remove all headers given its name.
	 * 
	 * @param headerName
	 *            is the name of the header to remove.
	 */
	public void removeHeader(String headerName) {

		if (headerName == null)
			throw new NullPointerException("null arg");
		Header toRemove = (Header) nameTable.get(headerName.toLowerCase());
		// nothing to do then we are done.
		if (toRemove == null)
			return;
		nameTable.remove(headerName.toLowerCase());
		// Remove the fast accessor fields.
		if (toRemove instanceof FromHeader) {
			this.fromHeader = null;
		} else if (toRemove instanceof ToHeader) {
			this.toHeader = null;
		} else if (toRemove instanceof CSeqHeader) {
			this.cSeqHeader = null;
		} else if (toRemove instanceof CallIdHeader) {
			this.callIdHeader = null;
		} else if (toRemove instanceof ContentLengthHeader) {
			this.contentLengthHeader = null;
		}

		Enumeration li = this.headers.elements();
		int index = -1;
		while (li.hasMoreElements()) {
			Header sipHeader = (Header) li.nextElement();
			index++;
			if (Utils.equalsIgnoreCase(sipHeader.getName(), headerName))
				break;

		}
		if (index != -1 && index < headers.size())
			headers.removeElementAt(index);
	}

	/**
	 * Return true if this message has a body.
	 */
	public boolean hasContent() {
		return messageContent != null || messageContentBytes != null;
	}

	/**
	 * Return an iterator for the list of headers in this message.
	 * 
	 * @return an Iterator for the headers of this message.
	 */
	public Enumeration getHeaders() {
		return headers.elements();
	}

	/**
	 * Get the first header of the given name.
	 * 
	 * @return header -- the first header of the given name.
	 */
	public Header getHeader(String headerName) {
		if (headerName == null)
			throw new NullPointerException("bad name");
		Header sipHeader = (Header) this.nameTable
				.get(headerName.toLowerCase());
		if (sipHeader instanceof HeaderList)
			return (Header) ((HeaderList) sipHeader).getFirst();
		else
			return (Header) sipHeader;
	}

	/**
	 * Get the contentType header (null if one does not exist).
	 * 
	 * @return contentType header
	 */
	public ContentTypeHeader getContentTypeHeaderHeader() {
		return (ContentTypeHeader) this.getHeader(Header.CONTENT_TYPE);
	}

	/**
	 * Get the from header.
	 * 
	 * @return -- the from header.
	 */
	public FromHeader getFromHeader() {
		return (FromHeader) fromHeader;
	}

	/**
	 * 
	 * /** Get the Contact list of headers (null if one does not exist).
	 * 
	 * @return List containing Contact headers.
	 */
	public ContactList getContactHeaders() {
		return (ContactList) this.getHeaderList(Header.CONTACT);
	}

	/**
	 * Get the Via list of headers (null if one does not exist).
	 * 
	 * @return List containing Via headers.
	 */
	public ViaList getViaHeaders() {
		return (ViaList) getHeaderList(Header.VIA);
	}

	/**
	 * Get an iterator to the list of vial headers.
	 * 
	 * @return a list iterator to the list of via headers. public ListIterator
	 *         getVia() { return this.viaHeaders.listIterator(); }
	 */

	/**
	 * Set A list of via headers.
	 * 
	 * @param -
	 *            a list of via headers to add.
	 */
	public void setVia(ViaList viaList) {
		this.setHeader(viaList);
	}

	/**
	 * Set a list of via headers.
	 */
	public void setVia(Vector viaList) {
		this.removeHeader(ViaHeader.NAME);
		for (int i = 0; i < viaList.size(); i++) {
			ViaHeader via = (ViaHeader) viaList.elementAt(i);
			this.addHeader(via);
		}
	}

	/**
	 * Set the header given a list of headers.
	 * 
	 * @param headerList
	 *            a headerList to set
	 */

	public void setHeader(HeaderList sipHeaderList) {
		this.setHeader((Header) sipHeaderList);
	}

	/**
	 * Get the topmost via header.
	 * 
	 * @return the top most via header if one exists or null if none exists.
	 */
	public ViaHeader getTopmostVia() {
		if (this.getViaHeaders() == null)
			return null;
		else
			return (ViaHeader) (getViaHeaders().getFirst());
	}

	/**
	 * Get the CSeqHeader list of header (null if one does not exist).
	 * 
	 * @return CSeqHeader header
	 */
	public CSeqHeader getCSeqHeader() {
		return cSeqHeader;
	}

	/**
	 * Get the sequence number.
	 * 
	 * @return the sequence number.
	 */
	public int getCSeqHeaderNumber() {
		return cSeqHeader.getSequenceNumber();
	}

	/**
	 * Get the Route List of headers (null if one does not exist).
	 * 
	 * @return List containing Route headers
	 */
	public RouteList getRouteHeaders() {
		return (RouteList) getHeaderList(Header.ROUTE);
	}

	/**
	 * Get the CallIdHeader header (null if one does not exist)
	 * 
	 * @return Call-ID header .
	 */
	public CallIdHeader getCallId() {
		return callIdHeader;
	}

	/**
	 * Set the call id header.
	 * 
	 * @param callid --
	 *            call idHeader (what else could it be?)
	 */
	public void setCallId(CallIdHeader callId) {
		this.setHeader(callId);
	}

	/**
	 * Get the CallIdHeader header (null if one does not exist)
	 * 
	 * @param callId --
	 *            the call identifier to be assigned to the call id header
	 */
	public void setCallId(String callId) throws ParseException {
		if (callIdHeader == null) {
			this.setHeader(new CallIdHeader());
		}
		callIdHeader.setCallId(callId);
	}

	/**
	 * Get the call ID string. A conveniance function that returns the stuff
	 * following the header name for the call id header.
	 * 
	 * @return the call identifier.
	 * 
	 */
	public String getCallIdentifier() {
		return callIdHeader.getCallId();
	}

	/**
	 * Get the RecordRoute header list (null if one does not exist).
	 * 
	 * @return Record-Route header
	 */
	public RecordRouteList getRecordRouteHeaders() {
		return (RecordRouteList) this.getHeaderList(Header.RECORD_ROUTE);
	}

	/**
	 * Get the To header (null if one does not exist).
	 * 
	 * @return To header
	 */
	public ToHeader getToHeader() {
		return (ToHeader) toHeader;
	}

	public void setToHeader(ToHeader to) {
		this.setHeader(to);
	}

	public void setFromHeader(FromHeader from) {
		this.setHeader(from);

	}

	/**
	 * Get the ContentLengthHeader header (null if one does not exist).
	 * 
	 * @return content-length header.
	 */
	public ContentLengthHeader getContentLengthHeader() {
		return contentLengthHeader;
	}

	/**
	 * Get the message body as a string. If the message contains a content type
	 * header with a specified charset, and if the payload has been read as a
	 * byte array, then it is returned encoded into this charset.
	 * 
	 * @return Message body (as a string)
	 * 
	 */
	public String getMessageContent() throws UnsupportedEncodingException {
		if (this.messageContent == null && this.messageContentBytes == null)
			return null;
		else if (this.messageContent == null) {
			ContentTypeHeader contentTypeHeader = (ContentTypeHeader) this.nameTable
					.get(Header.CONTENT_TYPE.toLowerCase());
			if (contentTypeHeader != null) {
				String charset = contentTypeHeader.getCharset();
				if (charset != null) {
					this.messageContent = new String(messageContentBytes,
							charset);
				} else {
					this.messageContent = new String(messageContentBytes,
							DEFAULT_ENCODING);
				}
			} else
				this.messageContent = new String(messageContentBytes,
						DEFAULT_ENCODING);
		}
		return this.messageContent;
	}

	/**
	 * Get the message content as an array of bytes. If the payload has been
	 * read as a String then it is decoded using the charset specified in the
	 * content type header if it exists. Otherwise, it is encoded using the
	 * default encoding which is UTF-8.
	 * 
	 * @return an array of bytes that is the message payload.
	 * 
	 */
	public byte[] getRawContent() {
		try {
			if (this.messageContent == null && this.messageContentBytes == null
					&& this.messageContentObject == null) {
				return null;
			} else if (this.messageContentObject != null) {
				String messageContent = this.messageContentObject.toString();
				byte[] messageContentBytes;
				ContentTypeHeader contentTypeHeader = (ContentTypeHeader) this.nameTable
						.get(Header.CONTENT_TYPE.toLowerCase());
				if (contentTypeHeader != null) {
					String charset = contentTypeHeader.getCharset();
					if (charset != null) {
						messageContentBytes = messageContent.getBytes(charset);
					} else {
						messageContentBytes = messageContent
								.getBytes(DEFAULT_ENCODING);
					}
				} else
					messageContentBytes = messageContent
							.getBytes(DEFAULT_ENCODING);
				return messageContentBytes;
			} else if (this.messageContent != null) {
				byte[] messageContentBytes;
				ContentTypeHeader contentTypeHeader = (ContentTypeHeader) this.nameTable
						.get(Header.CONTENT_TYPE.toLowerCase());
				if (contentTypeHeader != null) {
					String charset = contentTypeHeader.getCharset();
					if (charset != null) {
						messageContentBytes = this.messageContent
								.getBytes(charset);
					} else {
						messageContentBytes = this.messageContent
								.getBytes(DEFAULT_ENCODING);
					}
				} else
					messageContentBytes = this.messageContent
							.getBytes(DEFAULT_ENCODING);
				return messageContentBytes;
			} else {
				return messageContentBytes;
			}
		} catch (UnsupportedEncodingException ex) {
			return null;
		}
	}

	/**
	 * Set the message content given type and subtype.
	 * 
	 * @param type
	 *            is the message type (eg. application)
	 * @param subType
	 *            is the message sybtype (eg. sdp)
	 * @param messageContent
	 *            is the messge content as a string.
	 */

	public void setMessageContent(String type, String subType,
			String messageContent) throws IllegalArgumentException {
		if (messageContent == null)
			throw new IllegalArgumentException("messgeContent is null");
		ContentTypeHeader ct = new ContentTypeHeader(type, subType);
		this.setHeader(ct);
		this.messageContent = messageContent;
		this.messageContentBytes = null;
		this.messageContentObject = null;

		if (this.getContentLengthHeader() != null) {
			this.getContentLengthHeader().setContentLength(
					messageContent.length());
		}

	}

	/**
	 * Set the message content after converting the given object to a String.
	 * 
	 * @param content --
	 *            content to set.
	 * @param contentTypeHeader --
	 *            content type header corresponding to content.
	 */
	public void setContent(Object content, ContentTypeHeader contentTypeHeader)
			throws ParseException {
		if (content == null)
			throw new NullPointerException("null content");
		String contentString = content.toString();
		this.setMessageContent(contentString);
		this.setHeader(contentTypeHeader);
		this.removeContent();
		if (content instanceof String) {
			this.messageContent = (String) content;
		} else if (content instanceof byte[]) {
			this.messageContentBytes = (byte[]) content;
		} else
			this.messageContentObject = content;

		int length = -1;
		if (content instanceof String)
			length = ((String) content).length();
		else if (content instanceof byte[])
			length = ((byte[]) content).length;

		if (length != -1 && this.getContentLengthHeader() != null) {
			this.getContentLengthHeader().setContentLength(length);
		}

	}

	public void setContent(Object content) {
		if (content == null)
			throw new NullPointerException("null content");
		String contentString = content.toString();
		this.setMessageContent(contentString);
		this.removeContent();
		if (content instanceof String) {
			this.messageContent = (String) content;
		} else if (content instanceof byte[]) {
			this.messageContentBytes = (byte[]) content;
		} else
			this.messageContentObject = content;

		int length = -1;
		if (content instanceof String)
			length = ((String) content).length();
		else if (content instanceof byte[])
			length = ((byte[]) content).length;

		if (length != -1 && this.getContentLengthHeader() != null) {
			this.getContentLengthHeader().setContentLength(length);
		}
	}

	/**
	 * Get the content of the header.
	 * 
	 * @return the content of the sip message.
	 */
	public Object getContent() {
		if (this.messageContentObject != null)
			return messageContentObject;
		else if (this.messageContentBytes != null)
			return this.messageContentBytes;
		else if (this.messageContent != null)
			return this.messageContent;
		else
			return null;
	}

	/**
	 * Set the message content for a given type and subtype.
	 * 
	 * @param type
	 *            is the messge type.
	 * @param subType
	 *            is the message subType.
	 * @param messageContent
	 *            is the message content as a byte array.
	 */
	public void setMessageContent(String type, String subType,
			byte[] messageContent) {
		ContentTypeHeader ct = new ContentTypeHeader(type, subType);
		this.setHeader(ct);
		this.setMessageContent(messageContent);

		if (this.getContentLengthHeader() != null) {
			this.getContentLengthHeader().setContentLength(
					messageContent.length);
		}

	}

	/**
	 * Set the message content for this message.
	 * 
	 * @param content
	 *            Message body as a string.
	 */
	public void setMessageContent(String content) {
		int clength = (content == null ? 0 : content.length());

		if (this.getContentLengthHeader() != null) {
			this.getContentLengthHeader().setContentLength(clength);
		}

		messageContent = content;
		messageContentBytes = null;
		messageContentObject = null;
	}

	/**
	 * Set the message content as an array of bytes.
	 * 
	 * @param content
	 *            is the content of the message as an array of bytes.
	 */
	public void setMessageContent(byte[] content) {

		if (this.getContentLengthHeader() != null) {
			this.getContentLengthHeader().setContentLength(content.length);
		}

		messageContentBytes = content;
		messageContent = null;
		messageContentObject = null;
	}

	/**
	 * Remove the message content if it exists.
	 * 
	 */
	public void removeContent() {
		messageContent = null;
		messageContentBytes = null;
		messageContentObject = null;
	}

	/**
	 * Get a SIP header or Header list given its name.
	 * 
	 * @param headerName
	 *            is the name of the header to get.
	 * @return a header or header list that contains the retrieved header.
	 */
	public Enumeration<Header> getHeaders(String headerName) {
		if (headerName == null)
			throw new NullPointerException("null headerName");
		
		Header sipHeader = (Header) nameTable.get(headerName.toLowerCase());
		
		if (sipHeader == null)
			return new Vector<Header>().elements();
		if (sipHeader instanceof HeaderList) {
			return ((HeaderList)sipHeader).getElements();
		} else {
			Vector<Header> v = new Vector<Header>();
			v.addElement(sipHeader);
			return v.elements();
		}
	}

	private HeaderList getHeaderList(String headerName) {
		return (HeaderList) nameTable.get(headerName.toLowerCase());
	}

	/**
	 * Return true if the Message has a header of the given name.
	 * 
	 * @param headerName
	 *            is the header name for which we are testing.
	 * @return true if the header is present in the message
	 */

	public boolean hasHeader(String headerName) {
		return nameTable.containsKey(headerName.toLowerCase());
	}

	/**
	 * Return true if the message has a FromHeader header tag.
	 * 
	 * @return true if the message has a from header and that header has a tag.
	 */
	public boolean hasFromHeaderTag() {
		return fromHeader != null && fromHeader.getTag() != null;
	}

	/**
	 * Return true if the message has a To header tag.
	 * 
	 * @return true if the message has a to header and that header has a tag.
	 */
	public boolean hasToTag() {
		return toHeader != null && toHeader.getTag() != null;
	}

	/**
	 * Return the from tag.
	 * 
	 * @return the tag from the from header.
	 * 
	 */
	public String getFromHeaderTag() {
		return fromHeader == null ? null : fromHeader.getTag();
	}

	/**
	 * Set the FromHeader Tag.
	 * 
	 * @param tag --
	 *            tag to set in the from header.
	 */
	public void setFromHeaderTag(String tag) {

		fromHeader.setTag(tag);

	}

	/**
	 * Set the to tag.
	 * 
	 * @param tag --
	 *            tag to set.
	 */
	public void setToTag(String tag) {

		toHeader.setTag(tag);

	}

	/**
	 * Return the to tag.
	 */
	public String getToTag() {
		return toHeader == null ? null : toHeader.getTag();
	}

	/**
	 * Return the encoded first line.
	 */
	public abstract String getFirstLine();

	/**
	 * Add a SIP header.
	 * 
	 * @param sipHeader --
	 *            sip header to add.
	 */
	public void addHeader(Header sipHeader) {
		Header sh = (Header) sipHeader;
		if (sipHeader instanceof ViaHeader) {
			attachHeader(sh, false, true);
		} else {
			attachHeader(sh, false, false);
		}
	}

	/**
	 * Add a header to the unparsed list of headers.
	 * 
	 * @param unparsed --
	 *            unparsed header to add to the list.
	 */
	public void addUnparsed(String unparsed) {
		this.unrecognizedHeaders.addElement(unparsed);
	}

	/**
	 * Add a SIP header.
	 * 
	 * @param sipHeader --
	 *            string version of SIP header to add.
	 */

	public void addHeader(String sipHeader) {
		String hdrString = sipHeader.trim() + "\n";
		try {
			HeaderParser parser = ParserFactory.createParser(hdrString); // Modif: pb parsing event header
			Header sh = parser.parse();
			this.attachHeader(sh, false);
		} catch (ParseException ex) {
			this.unrecognizedHeaders.addElement(hdrString);
		}
	}

	/**
	 * Get a list containing the unrecognized headers.
	 * 
	 * @return a linked list containing unrecongnized headers.
	 */
	public Enumeration getUnrecognizedHeaders() {
		return this.unrecognizedHeaders.elements();
	}

	/**
	 * Get the header names.
	 * 
	 * @return a list iterator to a list of header names. These are ordered in
	 *         the same order as are present in the message.
	 */
	public Enumeration getHeaderNames() {
		Enumeration li = this.headers.elements();
		Vector retval = new Vector();
		while (li.hasMoreElements()) {
			Header sipHeader = (Header) li.nextElement();
			String name = sipHeader.getName();
			retval.addElement(name);
		}
		return retval.elements();
	}

	/**
	 * Compare for equality.
	 * 
	 * @param other --
	 *            the other object to compare with.
	 * 
	 */

	public boolean equals(Object other) {
		if (!other.getClass().equals(this.getClass()))
			return false;
		Message otherMessage = (Message) other;
		Enumeration values = this.nameTable.elements();
		if (otherMessage.nameTable.size() != nameTable.size())
			return false;
		while (values.hasMoreElements()) {
			Header mine = (Header) values.nextElement();
			Header his = (Header) nameTable.get(mine.getHeaderName()
					.toLowerCase());
			if (his == null)
				return false;
			else if (!his.equals(mine))
				return false;
		}
		return true;
	}

	/**
	 * Set the content length header.
	 * 
	 * @param contentLength --
	 *            content length header.
	 */
	public void setContentLength(ContentLengthHeader contentLength) {
		this.setHeader(contentLength);
	}

	/**
	 * Set the CSeqHeader header.
	 * 
	 * @param cseqHeader --
	 *            CSeqHeader Header.
	 */

	public void setCSeqHeader(CSeqHeader cseqHeader) {
		this.setHeader(cseqHeader);
	}

	public abstract void setSIPVersion(String sipVersion) throws ParseException;

	public abstract String getSIPVersion();

	public long getTimestamp() {
		return this.timestamp;
	}
}
