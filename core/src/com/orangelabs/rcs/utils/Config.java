/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright © 2010 France Telecom S.A.
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
package com.orangelabs.rcs.utils;

import java.io.InputStream;
import java.util.Hashtable;

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.platform.file.FileFactory;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Configuration file manager that makes XML parsing of the configuration file.
 * XML parameters are stored into memory.
 * 
 * @author JM. Auffret
 */
public class Config {

	/**
	 * Parameters
	 */
	private Hashtable<String, String> parameters = new Hashtable<String, String>();

	/**
	 * The logger
	 */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
	 * @param name Configuration filename (without path and extention)
	 * @throws CoreException
	 */
	public Config(String name) throws CoreException {
    	if (logger.isActivated()) {
    		logger.debug("Parse the configuration file " + name);
    	}
		try {
			// Get an input stream from XML configuration file
			InputStream is = FileFactory.getFactory().openConfigFile(name);
			if (is == null) {
				throw new CoreException("XML configuration file " + name + " not found");
			}
			
			// Parse XML config file
		    String txt = new String();
		    int c = -1;
		    while ((c = is.read()) != -1) {
		    	// Remove bad character
		    	if ((c != '\r') || (c != '\n') || (c != '\t')) {
	    			txt += (char)c;
		    	}
		    }
		
			// Remove comments
		    int index = 0;
		    int begin = -1;
		    String data = "";
		    while((begin = txt.indexOf("<!--", index)) != -1) {
		    	int end = txt.indexOf("-->");
		    	data += txt.substring(index, begin-1);
		    	index = end+3;
		    }
		    if ((begin == -1) || (begin < txt.length())) {
		    	data += txt.substring(index);
		    }		    

		    // Extract parameters
		    int index1 = 0;
		    while((index1 = data.indexOf("<parameter", index1)) != -1) {
		    	int index2 = data.indexOf("name=\"", index1);
		    	int index3 = data.indexOf("\"", index2+6);
		    	String key = data.substring(index2+6, index3);
		    	
		    	int index4 = data.indexOf("value=\"", index3+1);
		    	int index5 = data.indexOf("\"", index4+7);
		    	String value = data.substring(index4+7, index5);
		    	
		    	parameters.put(key, value.trim());
		    	index1 = index5+1;
		    }
		} catch(CoreException e) {
			throw e;
		} catch(Exception e) {
        	if (logger.isActivated()) {
        		logger.error("Error during XML parsing", e);
        	}
			throw new CoreException("Can't parse correctly the XML configuration file");
		}
	}

	/**
	 * Read a string parameter
	 * 
	 * @param key Parameter key
	 * @return String value
	 * @throws CoreException
	 */
	public String getString(String key) throws CoreException {
		String result = null;
		if (key != null) {
			result = parameters.get(key);
		} else {
			throw new CoreException("Bad key value for " + key);
		}
		return result;
	}
	
	/**
	 * Get a string value, a default value may be used
	 * 
	 * @param key Key to be search
	 * @param defaultValue Default value
	 * @return String value of the key
	 */
	public String getString(String key, String defaultValue) {
		String result;
		try {
			result = getString(key);
		} catch(Exception e) {
			result = defaultValue;
		}
		return result;
	}

	/**
	 * Get a numeric value
	 * 
	 * @param key Key to be search
	 * @return Numeric value of the key
	 * @throws CoreException
	 */
	public int getInteger(String key) throws CoreException {
		String value = getString(key);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new CoreException("Bad key value for " +
					key + ": should be an integer");
		}
	}

	/**
	 * Get a numeric value, a default value may be used
	 * 
	 * @param key Key to be search
	 * @param defaultValue Default value
	 * @return Numeric value of the key
	 */
	public int getInteger(String key, int defaultValue) {
		int result;
		try {
			result = getInteger(key);
		} catch (Exception e) {
			result = defaultValue;
		}
		return result;
	}

	/**
	 * Get a boolean value
	 * 
	 * @param key Key to be search
	 * @return Boolean value of the key
	 * @throws CoreException
	 */
	public boolean getBoolean(String key) throws CoreException {
		String value = getString(key);
		if (value.toLowerCase().equals("true")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get a boolean value, a default value may be used
	 * 
	 * @param key Key to be search
	 * @param defaultValue Default value
	 * @return Boolean value of the key
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		boolean result;
		try {
			result = getBoolean(key);
		} catch(Exception e) {
			result = defaultValue;
		}
		return result;
	}

	/**
	 * Get a double value
	 * 
	 * @param key Key to be search
	 * @return Double value of the key
	 * @throws CoreException
	 */
	public double getDouble(String key) throws CoreException {
		String value = getString(key);
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new CoreException("Bad key value for " +
					key + ": should be a double");
		}
	}

	/**
	 * Get a double value, a default value may be used
	 * 
	 * @param key Key to be search
	 * @param defaultValue Default value
	 * @return Double value of the key
	 */
	public double getDouble(String key, int defaultValue) {
		double result;
		try {
			result = getDouble(key);
		} catch (Exception e) {
			result = defaultValue;
		}
		return result;
	}
}
