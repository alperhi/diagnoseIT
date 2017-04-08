package org.diagnoseit.rules.mobile.impl;

import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.MobileRemoteMeasurement;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule detects high latency, although there is a good network connection.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "HighLatencyRule")
public class HighLatencyRule {

	private static final Logger log = LoggerInitializer.getLogger(HighLatencyRule.class.getName());

	/**
	 * In seconds.
	 */
	private static final int LATENCY_THRESHOLD = AntiPatternConfig.getInstance().getPropertyInt("HIGH_LATENCY_RULE_LATENCY_THRESHOLD");

	@TagValue(type = RuleConstants.TAG_REMOTE_INVOCATION)
	private RemoteInvocation remoteInvocation;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_LATENCY)
	public boolean action() {

		if (!remoteInvocation.getTargetSubTrace().isPresent()) {
			return false;
		}

		if (!remoteInvocation.getRequestMeasurement().isPresent() || !remoteInvocation.getResponseMeasurement().isPresent()) {
			return false;
		}

		MobileRemoteMeasurement requestMeasurement = remoteInvocation.getRequestMeasurement().get();
		MobileRemoteMeasurement responseMeasurement = remoteInvocation.getResponseMeasurement().get();

		if (!requestMeasurement.getTimestamp().isPresent() || !responseMeasurement.getTimestamp().isPresent() || !requestMeasurement.getNetworkConnection().isPresent()
				|| !responseMeasurement.getNetworkConnection().isPresent()) {
			return false;
		}

		long timestampOfRequest = requestMeasurement.getTimestamp().get();
		long timestampOfResponse = responseMeasurement.getTimestamp().get();

		long durationOfRemoteCallOnMobileDevice = timestampOfResponse - timestampOfRequest;

		long durationOfRemoteCallOnServer = remoteInvocation.getTargetSubTrace().get().getResponseTime();

		long latency = durationOfRemoteCallOnMobileDevice - durationOfRemoteCallOnServer;

		if (latency > LATENCY_THRESHOLD) {
			if (remoteInvocation.getIdentifier().isPresent()) {
				log.info("High Latency (= " + latency + ") in remote invocation detected. The identifier of the remote invocation is: " + remoteInvocation.getIdentifier().get());
			} else {
				log.info("High Latency (= " + latency + ") in remote invocation detected. The identifier of the remote invocation is unknown.");
			}
			return true;
		}

		return false;

	}
}