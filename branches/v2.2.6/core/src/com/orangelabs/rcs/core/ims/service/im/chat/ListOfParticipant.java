package com.orangelabs.rcs.core.ims.service.im.chat;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.xml.sax.InputSource;

import com.orangelabs.rcs.core.ims.ImsModule;
import com.orangelabs.rcs.core.ims.service.im.chat.resourcelist.ResourceListDocument;
import com.orangelabs.rcs.core.ims.service.im.chat.resourcelist.ResourceListParser;
import com.orangelabs.rcs.utils.PhoneUtils;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * List of participants
 * 
 * @author jexa7410
 */
public class ListOfParticipant {
	/**
	 * Internal list
	 */
	private List<String> list = new ArrayList<String>();
	
	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());	
	
	/**
	 * Constructor
	 */
	public ListOfParticipant() {
	}
	
	/**
	 * Constructor
	 * 
	 * @param list List
	 */
	public ListOfParticipant(List<String> list) {
		this.list = list;
	}
	
	/**
	 * Constructor
	 * 
	 * @param xml Resource-list document in XML
	 */
	public ListOfParticipant(String xml) {
		try {
	    	InputSource pidfInput = new InputSource(new ByteArrayInputStream(xml.getBytes()));
	    	ResourceListParser listParser = new ResourceListParser(pidfInput);
	    	ResourceListDocument resList = listParser.getResourceList();
	    	if (resList != null) {
	    		Vector<String> entries = resList.getEntries();
	    		for(int i=0; i < entries.size(); i++) {
	    			String entry = entries.elementAt(i); 
	    			if (!PhoneUtils.compareNumbers(entry, ImsModule.IMS_USER_PROFILE.getPublicUri())) {
	    				list.add(PhoneUtils.extractNumberFromUri(entry));
	    			}
	    		}
	    	}
	    } catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Can't parse resource-list document", e);
        	}
		}
	}
	
	/**
	 * Add a participant in the list
	 * 
	 * @param participant Participant
	 */
	public void addParticipant(String participant) {
		String number = PhoneUtils.extractNumberFromUri(participant);
		if (!list.contains(participant)) {
	    	if (logger.isActivated()) {
	    		logger.debug("Add participant " + number + " to the list");
	    	}	
	    	list.add(number);
		}
	}

	/**
	 * Remove a participant from the list
	 * 
	 * @param participant Participant
	 */
	public void removeParticipant(String participant) {
		String number = PhoneUtils.extractNumberFromUri(participant);
		if (list.contains(number)) {
	    	if (logger.isActivated()) {
	    		logger.debug("Remove participant " + number + " from the list");
	    	}	
	    	list.remove(number);
		} else {
	    	if (logger.isActivated()) {
	    		logger.debug("Participant " + number + " does not exist");
	    	}	
		}
	}
	
	/**
	 * Get list of participants
	 * 
	 * @return Array list
	 */
	public List<String> getList() {
		return list;
	}
}
