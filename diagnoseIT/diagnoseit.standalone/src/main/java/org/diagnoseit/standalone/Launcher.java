package org.diagnoseit.standalone;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.diagnoseit.engine.session.ISessionCallback;
import org.diagnoseit.rules.mobile.impl.AntiPatternConfig;
import org.diagnoseit.rules.mobile.impl.LoggerInitializer;
import org.diagnoseit.rules.result.ProblemOccurrence;
import org.spec.research.open.xtrace.api.core.Trace;

import creator.TraceCreator;

/**
 * Launcher for rules that analyze a single trace.
 *
 * @author Alper Hi
 *
 */
public class Launcher {

	private static final Logger log = LoggerInitializer.getLogger(Launcher.class.getName());

	/**
	 * Rules that should be executed.
	 */
	public enum RulePackage {
		DefaultPackage("org.diagnoseit.rules.impl"), MobilePackage("org.diagnoseit.rules.mobile.impl");

		private String packageName;

		RulePackage(String packageName) {
			this.packageName = packageName;
		}

		public String getPackageName() {
			return this.packageName;
		}

	};

	/**
	 *
	 * @param args
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException {

		Trace trace = TraceCreator.getTestTrace(true, 100, 3);
		startLauncher(trace);

	}

	/**
	 *
	 * @param filename
	 * @return
	 */
	private static boolean loadConfigFile(String filename) {

		if (!new File(filename).exists()) {
			return false;
		}
		Properties properties = new Properties();
		try {
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream(filename));
			properties.load(stream);
			stream.close();

			AntiPatternConfig.getInstance().setProperties(properties);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 *
	 */
	private static void loadConfigFile() {
		boolean configFound = false;

		configFound = loadConfigFile("C:/diagnoseIT/AntiPattern.properties");
		if (configFound) {
			return;
		}

		configFound = loadConfigFile("/etc/diagnoseIT/AntiPattern.properties");
		if (configFound) {
			return;
		}

		File file = new File("src/main/resources/AntiPattern.properties");
		String path = file.getAbsolutePath();
		configFound = loadConfigFile(path);
	}

	/**
	 * @param trace
	 * @throws ClassNotFoundException
	 */
	public static void startLauncher(Trace trace) {
		startLauncher(trace, RulePackage.MobilePackage);
	}

	/**
	 * @param trace
	 * @throws ClassNotFoundException
	 */
	public static void startLauncher(Trace trace, RulePackage rulePackage) {

		String output = "\n######################################################################\n" + "######################################################################\n"
				+ "########### diagnoseIT starts (TraceID: " + trace.getIdentifier().orElse(0) + ") ##########\n" + "######################################################################\n"
				+ "######################################################################";

		log.info(output);

		loadConfigFile();

		DiagnoseIT diagnoseIT = new DiagnoseIT(Collections.singletonList(rulePackage.getPackageName()));

		try {
			diagnoseIT.init(new ResultHandler());

			long baseline = 1000L;

			diagnoseIT.diagnose(trace, baseline);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @author AlperHi
	 *
	 */
	private static class ResultHandler implements ISessionCallback<List<ProblemOccurrence>> {
		/** The logger of this class. */
		private static final Logger log = LoggerInitializer.getLogger(Launcher.class.getName());

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(List<ProblemOccurrence> result) {
			log.info("Successfully conducted diagnosis!");
			// TODO: Do Something with diagnosis result
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onFailure(Throwable t) {
			log.info("Failed conducting diagnosis! Message: " + t.getMessage());
		}
	}

}