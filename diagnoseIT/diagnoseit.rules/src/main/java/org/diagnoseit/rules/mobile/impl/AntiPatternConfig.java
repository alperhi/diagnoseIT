package org.diagnoseit.rules.mobile.impl;

import java.util.Properties;

/**
 * @author AlperHi
 *
 */
public class AntiPatternConfig {

	private static AntiPatternConfig config = null;
	private Properties properties = null;

	private AntiPatternConfig() {

	}

	public static AntiPatternConfig getInstance() {
		if (config == null) {
			config = new AntiPatternConfig();
		}
		return config;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Gets {@link #properties}.
	 *
	 * @return {@link #properties}
	 */
	public Properties getProperties() {
		return this.properties;
	}

	public String getProperty(String key) {
		return this.properties.getProperty(key);
	}

	public int getPropertyInt(String key) {
		return Integer.parseInt(this.properties.getProperty(key));
	}

	public double getPropertyDouble(String key) {
		return Double.parseDouble(this.properties.getProperty(key));
	}

}
