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

	private static final Logger log = LoggerInitializer.getLogger(FailedBackendCommunicationRule.class.getName());

	private static final String NO_CONNECTION = "no connection";

	@TagValue(type = RuleConstants.TAG_REMOTE_INVOCATION)
	private RemoteInvocation remoteInvocation;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_FAILED_BACKEND_COMMUNICATION)
	public boolean action() {

		if (!remoteInvocation.getRequestMeasurement().isPresent()) {
			return false;
		}

		MobileRemoteMeasurement mobileRemMeasurement = remoteInvocation.getRequestMeasurement().get();

		if (!mobileRemMeasurement.getNetworkConnection().isPresent()) {
			return false;
		}

		String networkConnection = mobileRemMeasurement.getNetworkConnection().get();

		if (networkConnection.equalsIgnoreCase(NO_CONNECTION)) {

			if (remoteInvocation.getIdentifier().isPresent()) {
				log.info("Due to missing network connection the communication with the backend failed. The identifier of the remote invocation is: "
						+ remoteInvocation.getIdentifier().get());
			} else {
				log.info("Due to missing network connection the communication with the backend failed. The identifier of the remote invocation is unknown");
			}
			return true;
		}
		return false;
	}
}