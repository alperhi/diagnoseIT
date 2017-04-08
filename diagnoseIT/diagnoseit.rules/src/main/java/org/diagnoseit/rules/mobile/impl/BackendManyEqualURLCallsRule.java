package org.diagnoseit.rules.mobile.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.HTTPRequestProcessing;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule analyzes if the backend executed too many equal remote calls to the same URL.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "BackendManyEqualURLCallsRule")
public class BackendManyEqualURLCallsRule {

	private static final Logger log = LoggerInitializer.getLogger(BackendManyEqualURLCallsRule.class.getName());

	private static final double URL_CALLS_PERCENT = AntiPatternConfig.getInstance().getPropertyDouble("BACKEND_MANY_EQUAL_URL_CALLS_RULE_URL_CALLS_PERCENT");

	private static final double DURATION_PERCENT = AntiPatternConfig.getInstance().getPropertyDouble("BACKEND_MANY_EQUAL_URL_CALLS_RULE_DURATION_PERCENT");

	private static final int MIN_AMOUNT_OF_CALLS = AntiPatternConfig.getInstance().getPropertyInt("BACKEND_MANY_EQUAL_URL_CALLS_RULE_MIN_AMOUNT_OF_CALLS");

	@TagValue(type = RuleConstants.TAG_JAVA_AGENT_SUBTRACES)
	private List<SubTrace> javaAgentSubTraces;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_MANY_EQUAL_URL_CALLS_BACKEND)
	public boolean action() {


		if (javaAgentSubTraces.isEmpty()) {
			return false;
		}

		List<SubTrace> remoteSubtraces = new LinkedList<SubTrace>();
		double completeDurationOfSubtraces = 0;

		for (SubTrace subTrace : javaAgentSubTraces) {

			completeDurationOfSubtraces += subTrace.getResponseTime();

			for (Callable callable : subTrace) {
				if (callable instanceof RemoteInvocation) {
					RemoteInvocation remoteInvo = (RemoteInvocation) callable;
					if (remoteInvo.getTargetSubTrace().isPresent()) {
						remoteSubtraces.add(remoteInvo.getTargetSubTrace().get());
					}
				}
			}
		}

		completeDurationOfSubtraces /= 1000000000.0;

		if (remoteSubtraces.isEmpty() || (remoteSubtraces.size() < (DURATION_PERCENT * completeDurationOfSubtraces))) {
			return false;
		}

		List<HTTPRequestProcessing> httpRequests = new LinkedList<HTTPRequestProcessing>();

		for (SubTrace subtrace : remoteSubtraces) {
			if (subtrace.getRoot() instanceof HTTPRequestProcessing) {
				HTTPRequestProcessing httpInvo = (HTTPRequestProcessing) subtrace.getRoot();
				httpRequests.add(httpInvo);
			}
		}

		if (httpRequests.isEmpty()) {
			return false;
		}

		HashMap<String, Long> remoteInvoMap = new HashMap<String, Long>();

		for (HTTPRequestProcessing httpInvo : httpRequests) {
			String url = httpInvo.getUri();

			if (remoteInvoMap.containsKey(url)) {
				remoteInvoMap.put(url, remoteInvoMap.get(url) + 1);
			} else {
				long amount = 1;
				remoteInvoMap.put(url, amount);
			}
		}

		boolean tooManyEqualURLCalls = false;

		for (long amountEqualURLCalls : remoteInvoMap.values()) {
			if ((amountEqualURLCalls >= MIN_AMOUNT_OF_CALLS) && (amountEqualURLCalls > (remoteSubtraces.size() * URL_CALLS_PERCENT))) {
				log.info("Java application executed too many equal URL calls. Amount = " + amountEqualURLCalls + ".");
				tooManyEqualURLCalls = true;
			}
		}
		return tooManyEqualURLCalls;
	}
}
