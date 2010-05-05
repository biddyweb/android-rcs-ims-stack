package com.orangelabs.rcs.platform.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.orangelabs.rcs.platform.FactoryException;

/**
 * File factory
 * 
 * @author jexa7410
 */
public abstract class FileFactory {
	/**
	 * Current platform factory
	 */
	private static FileFactory factory = null;
	
	/**
	 * Load the factory
	 * 
	 * @param classname Factory classname
	 * @throws Exception
	 */
	public static void loadFactory(String classname) throws FactoryException {
		if (factory != null) {
			return;
		}
		
		try {
			factory = (FileFactory)Class.forName(classname).newInstance();
		} catch(Exception e) {
			throw new FactoryException("Can't load the factory " + classname);
		}
	}
	
	/**
	 * Returns the current factory
	 * 
	 * @return Factory
	 */
	public static FileFactory getFactory() {
		return factory;
	}

	/**
	 * Open a configuration file input stream 
	 * 
	 * @param filename Configuration filename
	 * @return Input stream
	 * @throws IOException
	 */
	public abstract InputStream openConfigFile(String filename) throws IOException;

	/**
	 * Open a file input stream
	 * 
	 * @param url URL
	 * @return Input stream
	 * @throws IOException
	 */
	public abstract InputStream openFileInputStream(String url) throws IOException;

	/**
	 * Open a file output stream
	 * 
	 * @param url URL
	 * @return Output stream
	 * @throws IOException
	 */
	public abstract OutputStream openFileOutputStream(String url) throws IOException;
	
	/**
	 * Returns the description of a file
	 * 
	 * @param url URL of the file
	 * @return File description
	 * @throws IOException
	 */
	public abstract FileDescription getFileDescription(String url) throws IOException;
	
	/**
	 * Returns the root directory for photos
	 * 
	 *  @return Directory path
	 */
	public abstract String getPhotoRootDirectory();

	/**
	 * Returns the root directory for videos
	 * 
	 *  @return Directory path
	 */
	public abstract String getVideoRootDirectory();
	
	/**
	 * Returns the root directory for files
	 * 
	 *  @return Directory path
	 */
	public abstract String getFileRootDirectory();	
}
