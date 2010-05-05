package com.orangelabs.rcs.core.ims.service.presence.pidf;

public class Note {

	private String value = null;
	private String lang = null;

	public Note() {
	}

	public Note(String lang, String value) {
		this.value = value;
		this.lang = lang;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
