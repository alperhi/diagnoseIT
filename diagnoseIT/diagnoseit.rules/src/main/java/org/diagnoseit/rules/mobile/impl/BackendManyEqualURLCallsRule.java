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
import org.spec.research.open.xtrace.api.core.SubTrace;
import org.spec.research.open.xtrace.api.core.Trace;
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

	private static final Logger log = Logger.getLogger(BackendManyEqualURLCallsRule.class.getName());

	private static final double URL_CALLS_PERCENT = 0.03;

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_MANY_EQUAL_REMOTE_INVOCATIONS_BACKEND)
	public boolean action() {

		// log.info("===== BackendManyEqualURLCallsRule =====");

		List<SubTrace> javaAgentSubTraces = new LinkedList<SubTrace>();

		for (Callable callable : trace.getRoot()) {
			if (callable instanceof RemoteInvocation) {
				RemoteInvocation remoteInvo = (RemoteInvocation) callable;
				if (remoteInvo.getTargetSubTrace().isPresent()) {
					javaAgentSubTraces.add(remoteInvo.getTargetSubTrace().get());
				}
			}
		}

		if (javaAgentSubTraces.isEmpty()) {
			return false;
		}

		List<SubTrace> remoteSubtraces = new LinkedList<SubTrace>();
		int amountOfCallables = 0;

		for (SubTrace subTrace : javaAgentSubTraces) {
			for (Callable callable : subTrace) {
				amountOfCallables++;
				if (callable instanceof RemoteInvocation) {
					RemoteInvocation remoteInvo = (RemoteInvocation) callable;
					if (remoteInvo.getTargetSubTrace().isPresent()) {
						remoteSubtraces.add(remoteInvo.getTargetSubTrace().get());
					}
				}
			}
		}

		if (remoteSubtraces.isEmpty()) {
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

		for (long amountEqualURLInvos : remoteInvoMap.values()) {
			if (amountEqualURLInvos > (amountOfCallables * URL_CALLS_PERCENT)) {
				log.info("BackendManyEqualURLCallsRule: Java application executed too many equal URL calls. Amount = " + amountEqualURLInvos + ".");
				tooManyEqualURLCalls = true;
			}
		}
		return tooManyEqualURLCalls;
	}
}
