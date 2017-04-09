package org.diagnoseit.rules.mobile.impl;

import java.util.ArrayList;
import java.util.List;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/**
 * Rule extracts remote invocations from mobile trace and returns all of them.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "ExtractRemoteInvocationsRule")
public class ExtractRemoteInvocationsRule {

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	/**
	 * Execution of the rule. Returns all remote invocations in a list.
	 * 
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_REMOTE_INVOCATIONS, resultQuantity =
			Action.Quantity.SINGLE)
	public List<RemoteInvocation> action() {

		List<RemoteInvocation> remoteInvocations = new ArrayList<RemoteInvocation>();

		for (Callable callable : trace.getRoot()) {
			if (callable instanceof RemoteInvocation) {
				RemoteInvocation remoteInvo = (RemoteInvocation) callable;
				remoteInvocations.add(remoteInvo);
			}
		}
		return remoteInvocations;

	}
}
