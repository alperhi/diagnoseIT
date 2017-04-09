package org.diagnoseit.rules.mobile.impl;

import java.util.LinkedList;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.diagnoseit.engine.rule.annotation.Action;
import org.diagnoseit.engine.rule.annotation.Rule;
import org.diagnoseit.engine.rule.annotation.TagValue;
import org.diagnoseit.engine.tag.Tags;
import org.diagnoseit.rules.RuleConstants;
import org.spec.research.open.xtrace.api.core.Trace;
import org.spec.research.open.xtrace.api.core.callables.Callable;
import org.spec.research.open.xtrace.api.core.callables.MobileMetadataMeasurement;

/**
 * Rule for detecting the Ramp anti-pattern in use case (RAM usage)
 *
 * @author Alper Hidiroglu
 *
 */
@Rule(name = "RAMRampRule")
public class RAMRampRule {

	/**
	 * Config parameter: slope of the regression line has to be higher than this value
	 */
	private static final double SLOPE_THRESHOLD = AntiPatternConfig.getInstance().getPropertyDouble("RAM_RAMP_RULE_SLOPE_THRESHOLD");

	private static final Logger log = LoggerInitializer.getLogger(RAMRampRule.class.getName());

	@TagValue(type = Tags.ROOT_TAG)
	private Trace trace;

	/**
	 * Rule execution.
	 *
	 * @return
	 */
	@Action(resultTag = RuleConstants.TAG_RAM_RAMP)
	public boolean action() {

		LinkedList<MobileMetadataMeasurement> mobileCallables = new LinkedList<MobileMetadataMeasurement>();

		for (Callable callable : trace.getRoot()) {
			if (callable instanceof MobileMetadataMeasurement) {
				MobileMetadataMeasurement mobileMeasurement = (MobileMetadataMeasurement) callable;
				if (mobileMeasurement.getMemoryUsage().isPresent()) {
					mobileCallables.add(mobileMeasurement);
				}
			}
		}

		if (mobileCallables.size() < 2) {
			return false;
		}

		// put regression line through data points
		SimpleRegression regression = new SimpleRegression();

		long firstTimestamp = mobileCallables.get(0).getTimestamp();

		// decrease the range of timestamps, begin by zero
		for (MobileMetadataMeasurement measurement : mobileCallables) {
			double RAMUsage = measurement.getMemoryUsage().get() * 100;
			long timestamp = measurement.getTimestamp() - firstTimestamp;

			regression.addData(timestamp, RAMUsage);
		}

		// the slope through the data points has to be equal or higher than the
		// threshold to detect the ramp
		if (!(regression.getSlope() > SLOPE_THRESHOLD)) {
			return false;
		}

		log.info("High increase of RAM usage detected within use case. Slope of the regression line = " + regression.getSlope() + ".\n");
		return true;
	}

}
