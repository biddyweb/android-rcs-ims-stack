package com.orangelabs.rcs.core.ims.service.presence.pidf;

/**
 * Status icon
 * 
 * @author jexa7410
 */
public class StatusIcon {
	private String url = null;
	private String etag = null;

	public StatusIcon() {
		
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}
}
