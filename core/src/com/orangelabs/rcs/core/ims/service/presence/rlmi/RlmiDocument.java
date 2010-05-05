package com.orangelabs.rcs.core.ims.service.presence.rlmi;

import java.util.Vector;

/**
 * Resource list document
 * 
 * @author jexa7410
 */
public class RlmiDocument {
	private String uri = null;
	
	private Vector<ResourceInstance> resourceList = new Vector<ResourceInstance>();

	public RlmiDocument(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public void addResource(ResourceInstance res) {
		resourceList.addElement(res);
	}

	public Vector<ResourceInstance> getResourceList() {
		return resourceList;
	}
}
