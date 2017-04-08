package org.diagnoseit.rules.mobile.impl;

import java.util.HashMap;
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
 * Rule analyzes if the mobile device executed to many equal URL calls.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "MobileDeviceManyEqualURLCallsRule")
public class MobileDeviceManyEqualURLCallsRule {

	private static final Logger log = LoggerInitializer.getLogger(MobileDeviceManyEqualURLCallsRule.class.getName());

	private static final double DURATION_PERCENT = AntiPatternConfig.getInstance().getPropertyDouble("MOBILE_DEVICE_MANY_EQUAL_URL_CALLS_RULE_DURATION_PERCENT");

	private static final double REMOTE_CALLS_PERCENT = AntiPatternConfig.getInstance().getPropertyDouble("MOBILE_DEVICE_MANY_EQUAL_URL_CALLS_RULE_REMOTE_CALLS_PERCENT");

	private static final int MIN_AMOUNT_OF_CALLS = AntiPatternConfig.getInstance().getPropertyInt("MOBILE_DEVICE_MANY_EQUAL_URL_CALLS_RULE_MIN_AMOUNT_OF_CALLS");

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	@TagValue(type = RuleConstants.TAG_REMOTE_INVOCATIONS)
	private List<RemoteInvocation> remoteInvocations;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_MANY_EQUAL_URL_CALLS_MOBILE)
	public boolean action() {

		NestingCallable rootCallable = (NestingCallable) trace.getRoot().getRoot();

		long useCaseDuration = rootCallable.getResponseTime() / 1000;

		if (remoteInvocations.isEmpty() || (remoteInvocations.size() < (DURATION_PERCENT * useCaseDuration))) {
			return false;
		}

		HashMap<String, Long> remoteInvoMap = new HashMap<String, Long>();

		for (RemoteInvocation remoteInvo : remoteInvocations) {
			if (!remoteInvo.getResponseMeasurement().isPresent()) {
				continue;
			}
			if (!remoteInvo.getResponseMeasurement().get().getUrl().isPresent()) {
				continue;
			}
			String url = remoteInvo.getResponseMeasurement().get().getUrl().get();

			if (remoteInvoMap.containsKey(url)) {
				remoteInvoMap.put(url, remoteInvoMap.get(url) + 1);
			} else {
				long amount = 1;
				remoteInvoMap.put(url, amount);
			}
		}

		boolean tooManyEqualURLCalls = false;

		for (long amountEqualURLCalls : remoteInvoMap.values()) {
			if ((amountEqualURLCalls >= MIN_AMOUNT_OF_CALLS) && (amountEqualURLCalls > (remoteInvocations.size() * REMOTE_CALLS_PERCENT))) {
				log.info("Mobile application executed too many equal URL calls. Amount = " + amountEqualURLCalls + ".");
				// return true;
				tooManyEqualURLCalls = true;
			}
		}
		// return false;
		return tooManyEqualURLCalls;
	}
}