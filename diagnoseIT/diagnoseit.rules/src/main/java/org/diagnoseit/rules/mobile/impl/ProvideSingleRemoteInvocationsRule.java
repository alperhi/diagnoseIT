package org.diagnoseit.rules.mobile.impl;

import java.util.List;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.callables.RemoteInvocation;

/***
 * Rule extracts remote invocations from mobile trace and returns each one separately.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "ProvideSingleRemoteInvocationsRule")
public class ProvideSingleRemoteInvocationsRule {

	@TagValue(type = RuleConstants.TAG_REMOTE_INVOCATIONS)
	private List<RemoteInvocation> remoteInvocations;

	/**
	 * Execution of the rule. Possibly returns more than one remote invocation.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_REMOTE_INVOCATION, resultQuantity = Action.Quantity.MULTIPLE)
	public List<RemoteInvocation> action() {

		return remoteInvocations;

	}
}
