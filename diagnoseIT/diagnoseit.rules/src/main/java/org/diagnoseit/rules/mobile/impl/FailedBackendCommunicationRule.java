package org.diagnoseit.rules.mobile.impl;

import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.MobileRemoteMeasurement;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule checks whether there was a request but due to a missing network connection the communication
 * with the backend failed.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "FailedBackendCommunicationRule")
public class FailedBackendCommunicationRule {

	private static final Logger log = Logger.getLogger(FailedBackendCommunicationRule.class.getName());

	@TagValue(type = RuleConstants.TAG_REMOTE_INVOCATION)
	private RemoteInvocation remoteInvocation;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_FAILED_BACKEND_COMMUNICATION)
	public boolean action() {

		// log.info("===== FailedBackendCommunicationRule =====");

		// for (Callable callable : trace.getRoot()) {
		// if (callable instanceof RemoteInvocation) {
		// RemoteInvocation current = (RemoteInvocation) callable;
		// if (current.getTargetSubTrace().isPresent()) {
		// SubTrace targetSubTrace = current.getTargetSubTrace().get();
		// Callable rootOfSubTrace = targetSubTrace.getRoot();
		// if (rootOfSubTrace instanceof HTTPRequestProcessing) {
		// HTTPRequestProcessing hrp = (HTTPRequestProcessing) rootOfSubTrace;
		// if (hrp.getResponseCode().isPresent()) {
		// long responseCode = hrp.getResponseCode().get();
		// }
		// }
		// }
		// }
		// }

		if (!remoteInvocation.getRequestMeasurement().isPresent()) {
			return false;
		}

		MobileRemoteMeasurement mobileRemMeasurement = remoteInvocation.getRequestMeasurement().get();

		if (!mobileRemMeasurement.getNetworkConnection().isPresent()) {
			return false;
		}

		String networkConnection = mobileRemMeasurement.getNetworkConnection().get();

		if (networkConnection.equals("unknown")) {
			log.info("FailedBackendCommunicationRule: Due to missing network connection the communication with the backend failed.");
			return true;
		}
		return false;
	}
}