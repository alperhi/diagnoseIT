package org.diagnoseit.rules.mobile.impl;

import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.MobileRemoteMeasurement;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule checks whether on the client side there was a timeout. It checks further
 * if the response would have been received.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "ResponseTimeoutRule")
public class ResponseTimeoutRule {

	private static final Logger log = Logger.getLogger(ResponseTimeoutRule.class.getName());

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	/**
	 * Rule execution.
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_RESPONSE_TIMEOUT)
	public boolean action() {

		for (Callable callable : trace.getRoot()) {
			if (!(callable instanceof RemoteInvocation)) {
				continue;
			}
			RemoteInvocation remoteInvo = (RemoteInvocation) callable;
			if (remoteInvo.getRequestMeasurement().isPresent()) {
				MobileRemoteMeasurement mobileRemoteMeasurement = remoteInvo
						.getRequestMeasurement().get();
				if (mobileRemoteMeasurement.getTimeout().isPresent()) {
					boolean isTimeout = mobileRemoteMeasurement.getTimeout()
							.get();
					if (isTimeout && remoteInvo.getTargetSubTrace().isPresent()) {
						log.info("ResponseTimeoutRule: Timeout on mobile client and the response came too late.");
						return true;
					} else if (isTimeout) {
						log.info("ResponseTimeoutRule: Timeout on mobile client and no response.");
					}
				}
			}
		}
		return false;
	}
}
