package org.diagnoseit.rules.mobile.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * @author AlperHi
 *
 */
@Rule(name = "BackendManyRemoteCallsRule")
public class BackendManyRemoteCallsRule {

	private static final Logger log = LoggerInitializer.getLogger(BackendManyRemoteCallsRule.class.getName());

	private static final double NUMBER_OF_REMOTE_CALLS_IN_A_SECOND = AntiPatternConfig.getInstance().getPropertyDouble("BACKEND_MANY_REMOTE_CALLS_RULE_NUMBER_OF_REMOTE_CALLS_IN_A_SECOND");

	private static final int MIN_AMOUNT_OF_CALLS = AntiPatternConfig.getInstance().getPropertyInt("BACKEND_MANY_REMOTE_CALLS_RULE_MIN_AMOUNT_OF_CALLS");

	@TagValue(type = RuleConstants.TAG_JAVA_AGENT_SUBTRACES)
	private List<SubTrace> javaAgentSubTraces;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_MANY_REMOTE_INVOCATIONS_BACKEND)
	public boolean action() {
		List<RemoteInvocation> remoteInvocations = new LinkedList<RemoteInvocation>();

		long completeDurationOfSubtraces = 0;

		for (SubTrace subTrace : javaAgentSubTraces) {
			completeDurationOfSubtraces += subTrace.getResponseTime();
			for (Callable callable : subTrace) {
				if (callable instanceof RemoteInvocation) {
					RemoteInvocation remoteInvo = (RemoteInvocation) callable;
					remoteInvocations.add(remoteInvo);
				}
			}
		}
		completeDurationOfSubtraces /= 1000000000l;

		if ((remoteInvocations.size() >= MIN_AMOUNT_OF_CALLS) && (remoteInvocations.size() >= (completeDurationOfSubtraces * NUMBER_OF_REMOTE_CALLS_IN_A_SECOND))) {
			log.info("Backend executed too many remote calls. Amount = " + remoteInvocations.size() + ".");
			return true;
		}
		return false;
	}
}
