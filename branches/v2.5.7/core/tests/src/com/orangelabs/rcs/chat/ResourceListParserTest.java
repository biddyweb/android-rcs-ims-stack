package com.orangelabs.rcs.chat;

import java.io.ByteArrayInputStream;

import org.xml.sax.InputSource;

import android.test.AndroidTestCase;

import com.orangelabs.rcs.core.ims.service.im.chat.resourcelist.ResourceListDocument;
import com.orangelabs.rcs.core.ims.service.im.chat.resourcelist.ResourceListParser;
import com.orangelabs.rcs.utils.logger.Logger;

public class ResourceListParserTest extends AndroidTestCase {
	private static final String CRLF = "\r\n";
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetResourceListDocument() {
		/*
		 * Resource-List SAMPLE: <?xml version="1.0" encoding="UTF-8"?>
		 * <resource-lists xmlns="urn:ietf:params:xml:ns:resource-lists"
		 * xmlns:cp="urn:ietf:params:xml:ns:copycontrol"> <list> <entry
		 * uri="sip:bill@example.com" cp:copyControl="to" /> <entry
		 * uri="sip:joe@example.org" cp:copyControl="cc" /> <entry
		 * uri="sip:ted@example.net" cp:copyControl="bcc" /> </list>
		 * </resource-lists>
		 */
		StringBuffer sb = new StringBuffer("<?xml version=\"1.08\" encoding=\"UTF-8\"?>");
		sb.append(CRLF);
		sb.append("<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\" xmlns:cp=\"urn:ietf:params:xml:ns:copycontrol\">");
		sb.append(CRLF);
		sb.append("<list>");
		sb.append(CRLF);
		sb.append("<entry uri=\"sip:bill@example.com\" cp:copyControl=\"to\"  />");
		sb.append(CRLF);
		sb.append("<entry uri=\"sip:joe@example.org\" cp:copyControl=\"cc\" />");
		sb.append(CRLF);
		sb.append("<entry uri=\"sip:ted@example.net\" cp:copyControl=\"bcc\" />");
		sb.append(CRLF);
		sb.append("</list>");
		sb.append(CRLF);
		sb.append("</resource-lists>");
		sb.append(CRLF);
		String xml = sb.toString();
		try {
			InputSource inputso = new InputSource(new ByteArrayInputStream(xml.getBytes()));
			ResourceListParser parser = new ResourceListParser(inputso);
			ResourceListDocument rlistDoc = parser.getResourceList();
			if (logger.isActivated()) {
				if (rlistDoc.getEntries() != null) {
					logger.info("resources number = " + rlistDoc.getEntries().size());
				} else {
					logger.info("resources list is null");
				}
			}
			assertEquals(rlistDoc.getEntries().firstElement(), "sip:bill@example.com");
			assertEquals(rlistDoc.getEntries().elementAt(1), "sip:joe@example.org");
			assertEquals(rlistDoc.getEntries().lastElement(), "sip:ted@example.net");
		} catch (Exception e) {
			fail("no resourceslist");
			e.printStackTrace();
		}
	}
}
