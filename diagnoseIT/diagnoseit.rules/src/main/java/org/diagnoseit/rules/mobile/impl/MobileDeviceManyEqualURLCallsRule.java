package org.diagnoseit.rules.mobile.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule analyzes if the mobile device executed to many equal URL calls.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "MobileDeviceManyEqualURLCallsRule")
public class MobileDeviceManyEqualURLCallsRule {

	private static final Logger log = Logger.getLogger(MobileDeviceManyEqualURLCallsRule.class.getName());

	private static final double REMOTE_CALLS_PERCENT = 0.03;

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_MANY_EQUAL_URL_CALLS_MOBILE)
	public boolean action() {

		// log.info("===== MobileDeviceManyEqualURLCallsRule =====");

		int amountOfCallables = 0;
		List<RemoteInvocation> remoteInvocations = new LinkedList<RemoteInvocation>();

		for (Callable callable : trace.getRoot()) {
			amountOfCallables++;
			if (callable instanceof RemoteInvocation) {
				RemoteInvocation remoteInvo = (RemoteInvocation) callable;
				if (remoteInvo.getResponseMeasurement().isPresent() && remoteInvo.getResponseMeasurement().get().getUrl().isPresent()) {
					remoteInvocations.add(remoteInvo);
				}
			}
		}
		if (remoteInvocations.isEmpty()) {
			return false;
		}

		HashMap<String, Long> remoteInvoMap = new HashMap<String, Long>();

		for (RemoteInvocation remoteInvo : remoteInvocations) {
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
			if (amountEqualURLCalls > (amountOfCallables * REMOTE_CALLS_PERCENT)) {
				log.info("MobileDeviceManyEqualURLCallsRule: Mobile application executed too many equal URL calls. Amount = " + amountEqualURLCalls + ".");
				// return true;
				tooManyEqualURLCalls = true;
			}
		}
		// return false;
		return tooManyEqualURLCalls;
	}
}