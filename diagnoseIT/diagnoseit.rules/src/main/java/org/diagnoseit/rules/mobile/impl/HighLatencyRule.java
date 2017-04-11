package org.diagnoseit.rules.mobile.impl;

import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.MobileRemoteMeasurement;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule detects high latency.
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

		// In milliseconds
		long durationOfRemoteCallOnMobileDevice = timestampOfResponse - timestampOfRequest;

		long durationOfRemoteCallOnServer = remoteInvocation.getTargetSubTrace().get().getResponseTime() / 1000000;

		long latency = durationOfRemoteCallOnMobileDevice - durationOfRemoteCallOnServer;

		if (latency > LATENCY_THRESHOLD) {
			if (remoteInvocation.getTargetSubTrace().isPresent()) {
				log.info("High Latency (= " + latency + "ms) during remote call detected. Target information of the remote call: " + remoteInvocation.getTarget() + ".\n");
			} else {
				log.info("High Latency (= " + latency + "ms) during remote call detected.\n");
			}
			return true;
		}

		return false;

	}
}