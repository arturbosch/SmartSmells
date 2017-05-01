package io.gitlab.arturbosch.smartsmells.api

import io.gitlab.arturbosch.jpal.core.JPAL
import io.gitlab.arturbosch.jpal.resolution.Resolver
import io.gitlab.arturbosch.smartsmells.config.DetectorConfig
import io.gitlab.arturbosch.smartsmells.metrics.ClassInfo
import io.gitlab.arturbosch.smartsmells.metrics.ClassInfoDetector
import io.gitlab.arturbosch.smartsmells.metrics.FileMetricProcessor
import io.gitlab.arturbosch.smartsmells.metrics.Metric
import io.gitlab.arturbosch.smartsmells.util.Validate

import java.nio.file.Path
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * @author Artur Bosch
 */
class MetricFacade {

	private final List<Pattern> filters
	private final ClassInfoDetector classInfoDetector

	MetricFacade(final CombinedCompositeMetricRaiser compositeMetricRaiser,
				 final DetectorConfig config = null,
				 final List<String> filters = Collections.emptyList()) {
		Validate.notNull(compositeMetricRaiser)
		this.filters = Validate.notNull(filters)?.collect { Pattern.compile(it) }
		classInfoDetector = new ClassInfoDetector(compositeMetricRaiser)
		classInfoDetector.setConfig(config)
	}

	static MetricFacadeBuilder builder() {
		return new MetricFacadeBuilder()
	}

	List<ClassInfo> run(Path root) {
		def storage = root ? JPAL.initializedUpdatable(root, null, filters) : JPAL.updatable(null, filters)
		def resolver = new Resolver(storage)
		def processor = new FileMetricProcessor(classInfoDetector, resolver)
		def classInfos = storage.allCompilationInfo
				.parallelStream()
				.map { processor.process(it) }
				.flatMap { it.classes.stream() }
				.collect(Collectors.toList())
		return classInfos as List<ClassInfo>
	}

	static List<Metric> averageAndDeviation(List<ClassInfo> infos) {
		return infos.stream()
				.flatMap { it.metrics.stream() }
				.collect()
				.groupBy { (it as Metric).type }
				.collect {
			def meanMetric = averageMetric(it)
			def devMetric = deviationMetric(asDouble(meanMetric), it)
			[meanMetric, devMetric]
		}.flatten() as List<Metric>
	}

	private static Metric averageMetric(Map.Entry<String, List<Metric>> it) {
		Metric.of(it.key + "Mean", it.value.inject(0.0) { result, metric ->
			double value = asDouble(metric)
			result + value
		} / it.value.size())
	}

	private static Metric deviationMetric(double mean, Map.Entry<String, List<Metric>> it) {
		def size = it.value.size()
		Metric.of(it.key + "Deviation",
				Math.sqrt((1 / size) * it.value.stream().mapToDouble {
					Math.pow(asDouble(it) - mean, 2)
				}.sum())
		)
	}

	private static double asDouble(Metric it) {
		return it.isDouble ? it.asDouble() : it.value
	}
}
