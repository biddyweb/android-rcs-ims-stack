package com.orangelabs.rcs.core.ims.service.presence.watcherinfo;

import java.util.Vector;

/**
 * Watcher info document
 * 
 * @author jexa7410
 */
public class WatcherInfoDocument {
	private String resource;

	private String packageId;

	private Vector<Watcher> watcherList = new Vector<Watcher>();

	public WatcherInfoDocument(String resource, String packageId) {
		this.resource = resource;
		this.packageId = packageId;
	}
	
	public void addWatcher(Watcher watcher) {
		watcherList.addElement(watcher);
	}

	public Vector<Watcher> getWatcherList() {
		return watcherList;
	}

	public String getResource() {
		return resource;
	}

	public String getPackageId() {
		return packageId;
	}	
}
