package io.gitlab.arturbosch.smartsmells.metrics.internal

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import io.gitlab.arturbosch.smartsmells.metrics.Metric

import java.util.stream.Collectors

/**
 * @author Artur Bosch
 */
@CompileStatic
trait CompositeMetricRaiser {

	List<MetricRaiser> raisers = Collections.emptyList()

	abstract List<Metric> raise(ClassOrInterfaceDeclaration aClass)

	void init(@ClosureParams(FirstParam.FirstGenericType.class) Closure<List<MetricRaiser>> initialization) {
		initialization.call(raisers)
	}
}

@CompileStatic
class SimpleCompositeMetricRaiser implements CompositeMetricRaiser {

	SimpleCompositeMetricRaiser(List<MetricRaiser> raisers) {
		this.raisers = raisers
	}

	List<Metric> raise(ClassOrInterfaceDeclaration aClass) {
		return raisers.collect { it.raise(aClass) }
	}
}

@CompileStatic
class CombinedCompositeMetricRaiser implements CompositeMetricRaiser {

	List<CompositeMetricRaiser> compositions

	CombinedCompositeMetricRaiser(List<CompositeMetricRaiser> compositions) {
		this.compositions = compositions
	}

	@Override
	List<Metric> raise(ClassOrInterfaceDeclaration aClass) {
		return compositions.stream()
				.flatMap { it.raise(aClass).stream() }
				.collect(Collectors.toList())
	}

	@Override
	void init(@ClosureParams(FirstParam.FirstGenericType.class) Closure<List<MetricRaiser>> initialization) {
		compositions.each { it.init(initialization) }
	}
}

@CompileStatic
class LongMethodAverageAndDeviation implements CompositeMetricRaiser {

	@Override
	List<Metric> raise(ClassOrInterfaceDeclaration aClass) {
		def methods = aClass.getNodesByType(MethodDeclaration.class)
		int methodCount = methods.isEmpty() ? 1 : methods.size()


		def methodSizes = methods.stream().map { Optional.ofNullable(it.body) }.filter { it.isPresent() }
				.map { it.get() }.mapToInt() { it.map { it.statements.size() }.orElse(0) }
		def amlSum = methodSizes.sum()
		def aml = amlSum / methodCount
		def sml = Math.sqrt(methods.stream().map { Optional.ofNullable(it.body) }.filter { it.isPresent() }
				.map { it.get() }.mapToInt { it.map { it.statements.size() }.orElse(0) }
				.mapToDouble { Math.pow(it - amlSum, 2) }.sum() / methodCount)

		return [Metric.of('LongMethodAverage', aml.toDouble()),
				Metric.of('LongMethodDeviation', sml)]
	}
}

@CompileStatic
class LongParameterListAverageAndDeviation implements CompositeMetricRaiser {

	@Override
	List<Metric> raise(ClassOrInterfaceDeclaration aClass) {
		def methods = aClass.getNodesByType(MethodDeclaration.class)
		int methodCount = methods.isEmpty() ? 1 : methods.size()
		def aplSum = methods.stream().mapToInt { it.parameters.size() }.sum()
		def apl = aplSum / methodCount
		def spl = Math.sqrt(methods.stream().mapToInt { it.parameters.size() }
				.mapToDouble { Math.pow(it - aplSum, 2) }.sum() / methodCount)

		return [Metric.of('LongMethodAverage', apl.toDouble()),
				Metric.of('LongMethodDeviation', spl)]
	}
}