package com.telefonica.iot.cygnus.utils;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.telefonica.iot.cygnus.log.CygnusLogger;

/**
 * 
 */

/**
 * @author joelcamus
 *
 */
public class Property {

	private static final CygnusLogger LOGGER = new CygnusLogger(Property.class);
	private String file;
	private InputStream input = null;
	private Properties prop = null;

	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param file
	 */
	public Property(String file) {
		super();
		this.file = file;
		try {
			prop = new Properties();
			input = Property.class.getClassLoader().getResourceAsStream("login.properties");

			// load a properties file
			prop.load(input);
		} catch (IOException ex) {
			// ex.printStackTrace();
			LOGGER.error("Error open the file. Details=" + ex.getLocalizedMessage());
		}

	}

	public String getProperty(String key) {
		LOGGER.debug("Innit getProperty(key --> " + key + ", file --> " + file + ")");
		String value = null;

		// get the property value
		value = prop.getProperty(key);
		return value;

	}
}
