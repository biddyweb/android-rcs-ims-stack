package com.orangelabs.rcs.core.ims.service.presence.pidf;

public class Status {

	private Basic basic = null;

	public Status(Basic basic) {
		this.basic = basic;
	}

	public Status() {
	}

	public Basic getBasic() {
		return basic;
	}

	public void setBasic(Basic basic) {
		this.basic = basic;
	}
}
