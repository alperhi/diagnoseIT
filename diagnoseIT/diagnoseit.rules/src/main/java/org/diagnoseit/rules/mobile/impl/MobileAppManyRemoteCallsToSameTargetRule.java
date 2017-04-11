package org.diagnoseit.rules.mobile.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Rule analyzes if the mobile application executed to many remote calls to the same backend that
 * becomes a bottleneck.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "MobileDeviceManyRemoteCallsToSameTargetRule")
public class MobileAppManyRemoteCallsToSameTargetRule {

	private static final Logger log = LoggerInitializer.getLogger(MobileAppManyRemoteCallsToSameTargetRule.class.getName());

	private static final double DURATION_PERCENT = AntiPatternConfig.getInstance().getPropertyDouble("MOBILE_DEVICE_MANY_EQUAL_REMOTE_CALLS_RULE_DURATION_PERCENT");

	private static final double REMOTE_CALLS_PERCENT = AntiPatternConfig.getInstance().getPropertyDouble("MOBILE_DEVICE_MANY_EQUAL_REMOTE_CALLS_RULE_REMOTE_CALLS_PERCENT");

	private static final int MIN_AMOUNT_OF_CALLS = AntiPatternConfig.getInstance().getPropertyInt("MOBILE_DEVICE_MANY_EQUAL_REMOTE_CALLS_RULE_MIN_AMOUNT_OF_CALLS");


	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	@TagValue(type = RuleConstants.TAG_REMOTE_INVOCATIONS)
	private List<RemoteInvocation> remoteInvocations;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_MANY_EQUAL_REMOTE_INVOCATIONS_MOBILE)
	public boolean action() {

		NestingCallable rootCallable = (NestingCallable) trace.getRoot().getRoot();

		double useCaseDuration = rootCallable.getResponseTime() / 1000000000.0;

		if (remoteInvocations.isEmpty() || (remoteInvocations.size() < (DURATION_PERCENT * useCaseDuration))) {
			return false;
		}

		HashMap<String, Long> remoteInvoMap = new HashMap<String, Long>();

		for (RemoteInvocation remoteInvo : remoteInvocations) {
			if (remoteInvo.getTargetSubTrace().isPresent()) {
				String remoteTarget = remoteInvo.getTarget();

				if (remoteInvoMap.containsKey(remoteTarget)) {
					remoteInvoMap.put(remoteTarget, remoteInvoMap.get(remoteTarget) + 1);
				} else {
					long amount = 1;
					remoteInvoMap.put(remoteTarget, amount);
				}
			}
		}

		boolean tooManyEqualRemoteCalls = false;

		for (Map.Entry<String, Long> entry : remoteInvoMap.entrySet()) {
			String key = entry.getKey();
			Long amountEqualRemoteInvos = entry.getValue();
			if ((amountEqualRemoteInvos >= MIN_AMOUNT_OF_CALLS) && (amountEqualRemoteInvos > (remoteInvocations.size() * REMOTE_CALLS_PERCENT))) {
				log.info("Mobile application executed too many remote calls to the same target. The amount of calls is " + amountEqualRemoteInvos + " within " + useCaseDuration
						+ " s. Target information of the calls: "
						+ key + ".\n");
				// return true;
				tooManyEqualRemoteCalls = true;
			}
		}
		// return false;
		return tooManyEqualRemoteCalls;
	}
}