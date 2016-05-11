package com.gitlab.artismarti.smartsmells.config

import com.gitlab.artismarti.smartsmells.comment.CommentDetector
import com.gitlab.artismarti.smartsmells.common.Detector
import com.gitlab.artismarti.smartsmells.complexmethod.ComplexMethodDetector
import com.gitlab.artismarti.smartsmells.cycle.CycleDetector
import com.gitlab.artismarti.smartsmells.dataclass.DataClassDetector
import com.gitlab.artismarti.smartsmells.deadcode.DeadCodeDetector
import com.gitlab.artismarti.smartsmells.featureenvy.FeatureEnvyDetector
import com.gitlab.artismarti.smartsmells.featureenvy.FeatureEnvyFactor
import com.gitlab.artismarti.smartsmells.godclass.GodClassDetector
import com.gitlab.artismarti.smartsmells.largeclass.LargeClassDetector
import com.gitlab.artismarti.smartsmells.longmethod.LongMethodDetector
import com.gitlab.artismarti.smartsmells.longparam.LongParameterListDetector
import com.gitlab.artismarti.smartsmells.messagechain.MessageChainDetector
import com.gitlab.artismarti.smartsmells.middleman.MiddleManDetector
import com.gitlab.artismarti.smartsmells.middleman.MiddleManVisitor

import java.util.function.Supplier

import static com.gitlab.artismarti.smartsmells.util.Numbers.toDouble
import static com.gitlab.artismarti.smartsmells.util.Numbers.toInt
/**
 * @author artur
 */
enum Smell {

	COMMENT{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			return initDefault(detectorConfig, Constants.COMMENT, { new CommentDetector() });
		}
	}, COMPLEX_METHOD{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			Map<String, String> config = detectorConfig.getKey(Constants.COMPLEX_METHOD);
			if (isActive(config)) {
				return Optional.of(new ComplexMethodDetector(
						toInt(config.get(Constants.THRESHOLD), Defaults.COMPLEX_METHOD)));
			}
			Optional.empty()
		}
	}, CYCLE{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			return initDefault(detectorConfig, Constants.CYCLE, { new CycleDetector() });
		}
	}, DATA_CLASS{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			return initDefault(detectorConfig, Constants.DATA_CLASS, { new DataClassDetector() });
		}
	}, DEAD_CODE{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			return initDefault(detectorConfig, Constants.DEAD_CODE, { new DeadCodeDetector() });
		}
	}, FEATURE_ENVY{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			Map<String, String> config = detectorConfig.getKey(Constants.FEATURE_ENVY);
			if (isActive(config)) {
				return Optional.of(new FeatureEnvyDetector(FeatureEnvyFactor.newInstance(
						toDouble(config.get(Constants.THRESHOLD), Defaults.FEATURE_ENVY_FACTOR),
						toDouble(config.get(Constants.BASE), Defaults.FEATURE_ENVY_BASE),
						toDouble(config.get(Constants.WEIGHT), Defaults.FEATURE_ENVY_WEIGHT))));
			}
			Optional.empty()
		}
	}, GOD_CLASS{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			Map<String, String> config = detectorConfig.getKey(Constants.GOD_CLASS);
			if (isActive(config)) {
				return Optional.of(new GodClassDetector(
						toInt(config.get(Constants.WMC), Defaults.WEIGHTED_METHOD_COUNT),
						toInt(config.get(Constants.ATFD), Defaults.ACCESS_TO_FOREIGN_DATA),
						toDouble(config.get(Constants.TCC), Defaults.TIED_CLASS_COHESION)))
			}
			Optional.empty()
		}
	}, LARGE_CLASS{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			Map<String, String> config = detectorConfig.getKey(Constants.LARGE_CLASS);
			if (isActive(config)) {
				return Optional.of(new LargeClassDetector(
						toInt(config.get(Constants.THRESHOLD), Defaults.LARGE_CLASS)));
			}
			Optional.empty()
		}
	}, LONG_METHOD{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			Map<String, String> config = detectorConfig.getKey(Constants.LONG_METHOD);
			if (isActive(config)) {
				return Optional.of(new LongMethodDetector(
						toInt(config.get(Constants.THRESHOLD), Defaults.LONG_METHOD)));
			}
			Optional.empty()
		}
	}, LONG_PARAM{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			Map<String, String> config = detectorConfig.getKey(Constants.LONG_PARAM);
			if (isActive(config)) {
				return Optional.of(new LongParameterListDetector(
						toInt(config.get(Constants.THRESHOLD), Defaults.LONG_PARAMETER_LIST)));
			}
			Optional.empty()
		}
	}, MESSAGE_CHAIN{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			Map<String, String> config = detectorConfig.getKey(Constants.MESSAGE_CHAIN);
			if (isActive(config)) {
				return Optional.of(new MessageChainDetector(
						toInt(config.get(Constants.THRESHOLD), Defaults.CHAIN_SIZE)));
			}
			Optional.empty()
		}
	}, MIDDLE_MAN{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			Map<String, String> config = detectorConfig.getKey(Constants.MIDDLE_MAN);
			if (isActive(config)) {
				return Optional.of(new MiddleManDetector(MiddleManVisitor.MMT.valueOf(config.get(Constants.THRESHOLD))));
			}
			Optional.empty()
		}
	}, UNKNOWN{
		@Override
		Optional<Detector> initialize(DetectorConfig detectorConfig) {
			return Optional.empty()
		}
	}


	abstract Optional<Detector> initialize(DetectorConfig detectorConfig)

	private static Optional<Detector> initDefault(DetectorConfig detectorConfig,
	                                              String key,
	                                              Supplier<Detector> supplier) {
		Map<String, String> config = detectorConfig.getKey(key);
		if (isActive(config)) {
			return Optional.of(supplier.get());
		}
		Optional.empty()
	}

	private static boolean isActive(final Map<String, String> config) {
		return config != null && !config.isEmpty() && "true".equals(config.get(Constants.ACTIVE));
	}

}
