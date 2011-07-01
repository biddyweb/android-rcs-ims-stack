/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright Â© 2010 France Telecom S.A.
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
package com.orangelabs.rcs.provisioning;

import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Provisioning parser
 * 
 * @author jexa7410
 */
public class ProvisioningParser extends DefaultHandler {
	/* DOCUMENT SAMPLE:
		<?xml version="1.0"?>
		<wap-provisioningdoc version="1.1">
			<characteristic type=”IMS”>
				<characteristic type=”LBO_P-CSCF_Address”>
					<parm name=”Address” value=”X”/>
					<parm name=”AddressType” value=”X”/>
				</characteristic>
				<parm name=”NatUrlFmt” value=”X”/>
				<parm name=”IntUrlFmt” value=”X”/>
				<parm name=”Q-Value” value=”X”/>
				<characteristic type=”ServCapPresentity”>
					<parm name=”VoiceCall” value=”X”/>
					<parm name=”Chat” value=”X”/>
					<parm name=”SendSms” value=”X”/>
					<parm name=”FileTranfer” value=”X”/>
					<parm name=” VideoShare” value=”X”/>
					<parm name=”ImageShare” value=”X”/>
				</characteristic>
				<parm name=”MaxSizeImageShare” value=”X”/>
				<parm name=”MaxSizeImageShare” value=”X”/>
			</characteristic>
		</wap-provisioningdoc>	 
	 */
	
	/**
	 * Parameter type
	 */
	private String type;

	/**
	 * Parameter path
	 */
	private String path = "";

	/**
	 * List of parameters
	 */
	private Hashtable<String, Parameter> params = new Hashtable<String, Parameter>(); 
	
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
	public ProvisioningParser(InputSource inputSource) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser parser = factory.newSAXParser();
	    parser.parse(inputSource, this);
	}

	/**
	 * Get list of parameters
	 * 
	 * @return Hashtable
	 */
	public Hashtable<String, Parameter> getParams() {
		return params;
	}
	
	public void startDocument() {
		if (logger.isActivated()) {
			logger.debug("Start document");
		}
	}
	
	public void startElement(String namespaceURL, String localName,	String qname, Attributes attr) {
		if (localName.equals("characteristic")) {
			type = attr.getValue("type").trim().toUpperCase();
			path = path + "/" + type;
		} else
		if (localName.equals("parm")) {
			String name = attr.getValue("name").trim().toLowerCase();
			String value = attr.getValue("value").trim();
			String keyPath = path + "/" + name;
			Parameter param = new Parameter(name, value, type, keyPath);
			params.put(keyPath, param);
		}
	}
	
	public void endElement(String namespaceURL, String localName, String qname) {
		if (localName.equals("characteristic")) {
			int index  = path.lastIndexOf("/");
			if (index != -1) {
				path = path.substring(0, index);
			} else {
				path = "";
			}
		}
	}
	
	public void endDocument() {
		if (logger.isActivated()) {
			logger.debug("End document");
		}
	}
}