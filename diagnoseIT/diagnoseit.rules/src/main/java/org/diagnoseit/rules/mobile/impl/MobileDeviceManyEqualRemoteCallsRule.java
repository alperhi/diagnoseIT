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
 * Rule analyzes if the mobile device executed to many remote calls to the same
 * backend.
 *
 * @author Alper Hi
 *
 */
@Rule(name = "MobileDeviceManyEqualRemoteCallsRule")
public class MobileDeviceManyEqualRemoteCallsRule {

	private static final Logger log = Logger.getLogger(MobileDeviceManyEqualRemoteCallsRule.class.getName());

	private static final double REMOTE_CALLS_PERCENT = 0.03;

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_MANY_EQUAL_REMOTE_INVOCATIONS_MOBILE)
	public boolean action() {

		// log.info("===== MobileDeviceManyEqualRemoteCallsRule =====");

		int amountOfCallables = 0;
		List<RemoteInvocation> remoteInvocations = new LinkedList<RemoteInvocation>();

		for (Callable callable : trace.getRoot()) {
			amountOfCallables++;
			if (callable instanceof RemoteInvocation) {
				RemoteInvocation remoteInvo = (RemoteInvocation) callable;
				remoteInvocations.add(remoteInvo);
			}
		}
		if (remoteInvocations.isEmpty()) {
			return false;
		}

		/**
		 * getTarget gibt zur�ck: Host, RuntimeEnvironment, Application,
		 * BusinessTransaction. Ist das selbe wie:
		 * targetSubTrace.getLocation().toString() getTarget().equal(....)
		 * vergleicht die vier Parameter von Location. Wenn die 4 Parameter von
		 * einer RemoteInvocation mit den 4 Parametern von einer anderen
		 * RemoteInvocation �bereinstimmen, dann war der RemoteCall in beiden
		 * F�llen sozusagen der selbe (Anti-Pattern ?) -->
		 * remoteInvo.getTarget()
		 */

		HashMap<String, Long> remoteInvoMap = new HashMap<String, Long>();

		for (RemoteInvocation remoteInvo : remoteInvocations) {
			String remoteTarget = remoteInvo.getTarget();

			if (remoteInvoMap.containsKey(remoteTarget)) {
				remoteInvoMap.put(remoteTarget,
						remoteInvoMap.get(remoteTarget) + 1);
			} else {
				long amount = 1;
				remoteInvoMap.put(remoteTarget, amount);
			}
		}

		boolean tooManyEqualRemoteCalls = false;

		for (long amountEqualRemoteInvos : remoteInvoMap.values()) {
			if (amountEqualRemoteInvos > (amountOfCallables
					* REMOTE_CALLS_PERCENT)) {
				log.info("MobileDeviceManyEqualRemoteCallsRule: Mobile application executed too many equal remote calls. Amount = "
						+ amountEqualRemoteInvos + ".");
				// return true;
				tooManyEqualRemoteCalls = true;
			}
		}
		// return false;
		return tooManyEqualRemoteCalls;
	}
}