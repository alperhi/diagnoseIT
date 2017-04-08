package org.diagnoseit.rules.mobile.impl;

import java.util.logging.Logger;

import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.MobileMetadataMeasurement;

/**
 * @author AlperHi
 */
@Rule(name = "HardDiscRule")
public class HardDiscRule {

	private final double STORAGE_THRESHOLD = AntiPatternConfig.getInstance().getPropertyDouble("HARD_DISC_RULE_STORAGE_THRESHOLD");

	private static final Logger log = LoggerInitializer.getLogger(HardDiscRule.class.getName());

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_HARD_DISC_USAGE)
	public boolean action() {

		double maxUsage = 0.0;
		double currentUsage = 0.0;

		for (Callable callable : trace.getRoot()) {
			if (callable instanceof MobileMetadataMeasurement) {
				MobileMetadataMeasurement mobileMeasurement = (MobileMetadataMeasurement) callable;
				if (mobileMeasurement.getStorageUsage().isPresent()) {
					currentUsage = mobileMeasurement.getStorageUsage().get();
					if (currentUsage > maxUsage) {
						maxUsage = currentUsage;
					}
				}
			}
		}
		if (maxUsage >= STORAGE_THRESHOLD) {
			log.info("High usage of hard disc detected. This could have an impact on the performance. The peek usage was: " + maxUsage);
			return true;
		}
		return false;

	}
}
