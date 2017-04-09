package org.diagnoseit.rules.mobile.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule analyzes if the backend executed too many equal remote calls to the same device that becomes
 * a bottleneck.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "BackendManyEqualRemoteCallsRule")
public class BackendManyEqualRemoteCallsRule {

	private static final Logger log = LoggerInitializer.getLogger(BackendManyEqualRemoteCallsRule.class.getName());

	private static final double REMOTE_CALLS_PERCENT = AntiPatternConfig.getInstance().getPropertyDouble("BACKEND_MANY_EQUAL_REMOTE_CALLS_RULE_REMOTE_CALLS_PERCENT");

	private static final double DURATION_PERCENT = AntiPatternConfig.getInstance().getPropertyDouble("BACKEND_MANY_EQUAL_REMOTE_CALLS_RULE_DURATION_PERCENT");

	private static final int MIN_AMOUNT_OF_CALLS = AntiPatternConfig.getInstance().getPropertyInt("BACKEND_MANY_EQUAL_REMOTE_CALLS_RULE_MIN_AMOUNT_OF_CALLS");

	@TagValue(type = RuleConstants.TAG_JAVA_AGENT_SUBTRACES)
	private List<SubTrace> javaAgentSubTraces;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_MANY_EQUAL_REMOTE_INVOCATIONS_BACKEND)
	public boolean action() {

		if (javaAgentSubTraces.isEmpty()) {
			return false;
		}

		List<RemoteInvocation> remoteInvocations = new LinkedList<RemoteInvocation>();

		double completeDurationOfSubtraces = 0;

		for (SubTrace subTrace : javaAgentSubTraces) {
			completeDurationOfSubtraces += subTrace.getResponseTime();
			for (Callable callable : subTrace) {
				if (callable instanceof RemoteInvocation) {
					RemoteInvocation remoteInvo = (RemoteInvocation) callable;
					remoteInvocations.add(remoteInvo);
				}
			}
		}

		completeDurationOfSubtraces /= 1000000000.0;

		if (remoteInvocations.isEmpty() || (remoteInvocations.size() < (DURATION_PERCENT * completeDurationOfSubtraces))) {
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
				log.info("Java application executed too many equal remote calls. The amount of the calls is " + amountEqualRemoteInvos + ". Total time of the Java agent traces is: "
						+ completeDurationOfSubtraces
						+ " s. Target information of the remote calls: " + key + "./n");
				tooManyEqualRemoteCalls = true;
			}
		}
		return tooManyEqualRemoteCalls;
	}
}
