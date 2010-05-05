package com.orangelabs.rcs.platform.file;

/**
 * File description
 * 
 * @author jexa7410
 */
public class FileDescription {
	/**
	 * Name
	 */
	private String name;
	
	/**
	 * Size
	 */
	private long size = -1;
		
	/**
	 * Directory
	 */	
	private boolean directory = false;
	
	/**
	 * Constructor
	 */
	public FileDescription(String name, long size) {
		this.name = name;
		this.size = size;
	}

	/**
	 * Constructor
	 */
	public FileDescription(String name, long size, boolean directory) {
		this.name = name;
		this.size = size;
		this.directory = directory;
	}

	/**
	 * Returns the size of the file
	 * 
	 * @return File size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Returns the name of the file
	 * 
	 * @return File name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Is a directory
	 * 
	 * @return Boolean
	 */
	public boolean isDirectory() {
		return directory;
	}
}
