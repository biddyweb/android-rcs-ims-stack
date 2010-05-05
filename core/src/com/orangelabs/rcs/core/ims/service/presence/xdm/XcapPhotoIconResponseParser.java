package com.orangelabs.rcs.core.ims.service.presence.xdm;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * XCAP photo-icon response parser
 * 
 * @author jexa7410
 */
public class XcapPhotoIconResponseParser extends DefaultHandler {

	private StringBuffer accumulator;
	
	private byte[] data = null;
	private String mime = null;
	private String encoding = null;
	private String desc = null;
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     * 
     * @param inputSource Input source
     * @throws Exception
     */
    public XcapPhotoIconResponseParser(InputSource inputSource) throws Exception {
    	SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(inputSource, this);
	}

	public void startDocument() {
		if (logger.isActivated()) {
			logger.debug("Start document");
		}
		accumulator = new StringBuffer();
	}

	public void characters(char buffer[], int start, int length) {
		accumulator.append(buffer, start, length);
	}

	public void startElement(String namespaceURL, String localName,	String qname, Attributes attr) {
		accumulator.setLength(0);
	}

	public void endElement(String namespaceURL, String localName, String qname) {
		if (localName.equals("data")) {
			data = accumulator.toString().getBytes();
		} else
		if (localName.equals("mime-type")) {
			mime = accumulator.toString();
		} else
		if (localName.equals("encoding")) {
			encoding = accumulator.toString();
		} else
		if (localName.equals("description")) {
			desc = accumulator.toString();
		}
	}

	public void endDocument() {
		if (logger.isActivated()) {
			logger.debug("End document");
		}
	}

	public byte[] getData() {
		return data;
	}

	public String getMime() {
		return mime;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getDesc() {
		return desc;
	}
}
