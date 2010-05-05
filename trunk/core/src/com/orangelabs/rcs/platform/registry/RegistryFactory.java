package com.orangelabs.rcs.platform.registry;

import com.orangelabs.rcs.platform.FactoryException;

/**
 * Application registry factory
 * 
 * @author jexa7410
 */
public abstract class RegistryFactory {
	/**
	 * Current platform factory
	 */
	private static RegistryFactory factory = null;
	
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
			factory = (RegistryFactory)Class.forName(classname).newInstance();
		} catch(Exception e) {
			throw new FactoryException("Can't load the factory " + classname);
		}
	}
	
	/**
	 * Returns the current factory
	 * 
	 * @return Factory
	 */
	public static RegistryFactory getFactory() {
		return factory;
	}
	
	/**
	 * Read a string value in the registry
	 * 
	 * @param key Key name to be read
	 * @param defaultValue Default value
	 * @return String
	 */
	public abstract String readString(String key, String defaultValue);

	/**
	 * Write a string value in the registry
	 * 
	 * @param key Key name to be updated
	 * @param value New value
	 */
	public abstract void writeString(String key, String value);

	/**
	 * Read a long value in the registry
	 * 
	 * @param key Key name to be read
	 * @param defaultValue Default value
	 * @return Long
	 */
	public abstract long readLong(String key, long defaultValue);

	/**
	 * Write a long value in the registry
	 * 
	 * @param key Key name to be updated
	 * @param value New value
	 */
	public abstract void writeLong(String key, long value);

	/**
	 * Read a boolean value in the registry
	 * 
	 * @param key Key name to be read
	 * @param defaultValue Default value
	 * @return Boolean
	 */
	public abstract boolean readBoolean(String key, boolean defaultValue);

	/**
	 * Write a boolean value in the registry
	 * 
	 * @param key Key name to be updated
	 * @param value New value
	 */
	public abstract void writeBoolean(String key, boolean value);
	
	/**
	 * Remove a parameter in the registry
	 * 
	 * @param key Key name to be removed
	 */
	public abstract void removeParameter(String key);
}
