package com.orangelabs.rcs.core.ims.service.presence.xdm;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * XCAP response parser
 * 
 * @author jexa7410
 */
public class XcapResponseParser extends DefaultHandler {

	private StringBuffer accumulator;
	
	private List<String> uriList = new ArrayList<String>();
	
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
    public XcapResponseParser(InputSource inputSource) throws Exception {
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

		if (localName.equals("entry")) {
			String uri = attr.getValue("uri").trim();
			uriList.add(uri);
		}
	}

	public void endElement(String namespaceURL, String localName, String qname) {
	}

	public void endDocument() {
		if (logger.isActivated()) {
			logger.debug("End document");
		}
	}

	public void warning(SAXParseException exception) {
		if (logger.isActivated()) {
			logger.error("Warning: line " + exception.getLineNumber() + ": "
				+ exception.getMessage());
		}
	}

	public void error(SAXParseException exception) {
		if (logger.isActivated()) {
			logger.error("Error: line " + exception.getLineNumber() + ": "
				+ exception.getMessage());
		}
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		if (logger.isActivated()) {
			logger.error("Fatal: line " + exception.getLineNumber() + ": "
				+ exception.getMessage());
		}
		throw exception;
	}

	public List<String> getUris() {
		return uriList;
	}
}
