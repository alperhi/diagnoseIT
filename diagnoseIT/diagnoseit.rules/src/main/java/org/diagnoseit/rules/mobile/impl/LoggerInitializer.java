package org.diagnoseit.rules.mobile.impl;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author AlperHi
 *
 */
public class LoggerInitializer {

	private static Logger logger;

	public static Logger getLogger(String name) {

		if (logger == null) {
			logger = Logger.getLogger("diagnoseIT");
			initLogger(logger);
		}
		return logger;
	}

	private static void initLogger(Logger logger) {

		String directory = "";
		String windowsDirectory = "C:/diagnoseIT";
		if (!new File(windowsDirectory).exists()) {
			String linuxDirectory = "/etc/diagnoseIT";
			if (!new File(linuxDirectory).exists()) {
				return;
			}
			directory = linuxDirectory;
		} else {
			directory = windowsDirectory;
		}

		FileHandler handler;

		try {
			handler = new FileHandler(directory + "/diagnoseIT.log", true);
			logger.addHandler(handler);
			SimpleFormatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
