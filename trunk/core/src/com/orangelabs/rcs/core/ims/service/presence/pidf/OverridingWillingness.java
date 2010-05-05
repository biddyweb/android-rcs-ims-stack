package com.orangelabs.rcs.core.ims.service.presence.pidf;

import com.orangelabs.rcs.utils.DateUtils;

public class OverridingWillingness {

	private Basic basic = null;
	
	private long until = -1;

	public OverridingWillingness(Basic basic) {
		this.basic = basic;
	}

	public OverridingWillingness() {
	}

	public Basic getBasic() {
		return basic;
	}

	public void setBasic(Basic basic) {
		this.basic = basic;
	}

	public long getUntilTimestamp() {
		return until;
	}

	public void setUntilTimestamp(String ts) {
		this.until = DateUtils.decodeDate(ts);
	}	
}
