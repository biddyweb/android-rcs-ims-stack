package com.orangelabs.rcs.core.ims.service.presence.rlmi;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * PDIF parser
 * 
 * @author jexa7410
 */
public class RlmiParser extends DefaultHandler {
	/* RLMI SAMPLE:
		<?xml version="1.0" encoding="UTF-8"?>
		<list xmlns="urn:ietf:params:xml:ns:rlmi" uri="sip:+33960810101@sip.ofr.com;pres-list=rcs" version="1" fullState="true"><name>rcs</name>
		  <resource uri="sip:+33960810100@sip.ofr.com">
		    <instance id="001" state="pending" reason="subscribe"/>
		  </resource>
		</list>
	*/

	private StringBuffer accumulator;
	private ResourceInstance resourceInstance = null;
	private RlmiDocument resourceInfo = null;

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
    public RlmiParser(InputSource inputSource) throws Exception {
    	SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(inputSource, this);
	}

	public RlmiDocument getResourceInfo() {
		return resourceInfo;
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

		if (localName.equals("list")) {
			String uri = attr.getValue("uri").trim();
			resourceInfo = new RlmiDocument(uri);
		} else
		if (localName.equals("resource")) {
			String uri = attr.getValue("uri").trim();
			resourceInstance = new ResourceInstance(uri);
		} else
		if (localName.equals("instance")) {
			String state = attr.getValue("state");
			if ((resourceInstance != null) && (state != null)) {
				resourceInstance.setState(state.trim());
			}
			String reason = attr.getValue("reason");
			if ((resourceInstance != null) && (reason != null)) {
				resourceInstance.setReason(reason.trim());
			}
		}
	}

	public void endElement(String namespaceURL, String localName, String qname) {
		if (localName.equals("resource")) {
			if (resourceInfo != null) {
				resourceInfo.addResource(resourceInstance);
			}
			resourceInstance = null;
		} else
		if (localName.equals("list")) {
			if (logger.isActivated()) {
				logger.debug("RLMI document is complete");
			}			
		}
	}

	public void endDocument() {
		if (logger.isActivated()) {
			logger.debug("End document");
		}
	}
}