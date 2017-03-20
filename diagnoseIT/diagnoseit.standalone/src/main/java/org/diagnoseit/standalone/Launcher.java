package org.diagnoseit.standalone;

import java.util.Collections;
import java.util.List;

import org.diagnoseit.engine.session.ISessionCallback;
import org.diagnoseit.rules.result.ProblemOccurrence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spec.research.open.xtrace.adapters.introscope.source.IntroscopeTraceConverter;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.shared.TraceConverter;

/**
 * Launcher for rules that analyze a single trace.
 * 
 * @author Alper Hi
 *
 */
public class Launcher {

	/**
	 * Rules that should be executed.
	 */
	public static final String RULES_PACKAGE = "org.diagnoseit.rules.impl";

	/**
	 * Path to traces that should be analyzed.
	 */
	private static final String INTROSCOPE_FILE = "C:/Users/Alper Hi/Desktop/Universität/Bachelorarbeit/Traces_CA/CA_Trace_Problematic.xml";

	private static final String DYNATRACE_FILE = "path to dynatrace trace file";

	private static final String INSPECTIT_FILE = "";

	private static final String KIEKER_FILE = "path to kieker file";

	public static void main(String[] args) throws ClassNotFoundException {
		TraceConverter converter = new IntroscopeTraceConverter();
		Trace trace = converter.convertTraces(INTROSCOPE_FILE).get(0);
		startLauncher(trace);
	}

	/**
	 * @param trace
	 * @throws ClassNotFoundException
	 */
	public static void startLauncher(Trace trace) throws ClassNotFoundException {
		startLauncher(trace, RULES_PACKAGE);
	}

	/**
	 * @param trace
	 * @throws ClassNotFoundException
	 */
	public static void startLauncher(Trace trace, String rulePackage)
			throws ClassNotFoundException {
		DiagnoseIT diagnoseIT = new DiagnoseIT(
				Collections.singletonList(rulePackage));
		diagnoseIT.init(new ResultHandler());

		long baseline = 1000L;

		diagnoseIT.diagnose(trace, baseline);
	}

	private static class ResultHandler implements
			ISessionCallback<List<ProblemOccurrence>> {
		/** The logger of this class. */
		private static final Logger log = LoggerFactory
				.getLogger(Launcher.class);

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(List<ProblemOccurrence> result) {
			System.out.println("Success!!");
			// TODO: Do Something with diagnosis result
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onFailure(Throwable t) {
			log.warn("Failed conducting diagnosis!", t);
		}
	}

}
