package org.diagnoseit.rules.mobile.impl;

import java.util.List;
import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.NestingCallable;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule analyzes if the mobile device executed to many remote calls to the same backend that becomes
 * a bottleneck.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "MobileDeviceManyRemoteCallsRule")
public class MobileDeviceManyRemoteCallsRule {

	private static final Logger log = LoggerInitializer.getLogger(MobileDeviceManyRemoteCallsRule.class.getName());

	private static final double NUMBER_OF_REMOTE_CALLS_IN_A_SECOND = AntiPatternConfig.getInstance().getPropertyDouble("MOBILE_DEVICE_MANY_REMOTE_CALLS_RULE_NUMBER_OF_REMOTE_CALLS_IN_A_SECOND");

	private static final int MIN_AMOUNT_OF_CALLS = AntiPatternConfig.getInstance().getPropertyInt("MOBILE_DEVICE_MANY_REMOTE_CALLS_RULE_MIN_AMOUNT_OF_CALLS");


	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	@TagValue(type = RuleConstants.TAG_REMOTE_INVOCATIONS)
	private List<RemoteInvocation> remoteInvocations;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_MANY_REMOTE_CALLS_MOBILE)
	public boolean action() {

		NestingCallable rootCallable = (NestingCallable) trace.getRoot().getRoot();

		double useCaseDuration = rootCallable.getResponseTime() / 1000000000.0;

		if ((remoteInvocations.size() >= MIN_AMOUNT_OF_CALLS) && (remoteInvocations.size() >= (useCaseDuration * NUMBER_OF_REMOTE_CALLS_IN_A_SECOND))) {
			log.info("Mobile application executed too many remote calls. Amount = " + remoteInvocations.size() + ".");
			return true;
		}
		return false;
	}
}