package io.gitlab.arturbosch.smartsmells.metrics.internal

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.smartsmells.api.MetricRaiser
import io.gitlab.arturbosch.smartsmells.metrics.Metric
import io.gitlab.arturbosch.smartsmells.metrics.Metrics
import io.gitlab.arturbosch.smartsmells.smells.longmethod.LongMethodVisitor

@CompileStatic
class WMC implements MetricRaiser {
	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		return Metric.of("WeightedMethodCount", Metrics.wmc(aClass))
	}
}

@CompileStatic
class TCC implements MetricRaiser {
	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		return Metric.of("TiedClassCohesion", Metrics.tcc(aClass))
	}
}

@CompileStatic
class ATFD implements MetricRaiser {
	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		return Metric.of("AccessToForeignData", Metrics.atfd(aClass))
	}
}

@CompileStatic
class MCCabe implements MetricRaiser {
	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		def methods = aClass.getNodesByType(MethodDeclaration.class)
		int methodCount = methods.isEmpty() ? 1 : methods.size()
		def mccSum = methods.stream().mapToInt { Metrics.mcCabe(aClass) }.sum()
		def averageMCC = mccSum / methodCount
		return Metric.of("MCCabe", averageMCC.toDouble())
	}
}

@CompileStatic
class LOC implements MetricRaiser {
	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		return Metric.of("LinesOfCode", Metrics.loc(aClass))
	}
}

@CompileStatic
class SLOC implements MetricRaiser {
	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		return Metric.of("SourceLinesOfCode", Metrics.sloc(aClass))
	}
}

@CompileStatic
class NOA implements MetricRaiser {
	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		return Metric.of("NumberOfAttributes", Metrics.noa(aClass))
	}
}

@CompileStatic
class NOM implements MetricRaiser {
	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		return Metric.of("NumberOfMethods", Metrics.nom(aClass))
	}
}

@CompileStatic
class CC implements MetricRaiser {
	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		return Metric.of("CountClasses", Metrics.cc(aClass, resolver))
	}
}

@CompileStatic
class CM implements MetricRaiser {
	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		return Metric.of("CountMethods", Metrics.cm(aClass, resolver))
	}
}

@CompileStatic
class LM implements MetricRaiser {

	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		def methods = aClass.getNodesByType(MethodDeclaration.class)
		def average = methods.stream()
				.mapToInt { LongMethodVisitor.bodyLength(it) }
				.average()

		return Metric.of('LongMethod', average.orElse(0.0))
	}
}

@CompileStatic
class LPL implements MetricRaiser {

	@Override
	Metric raise(ClassOrInterfaceDeclaration aClass) {
		def methods = aClass.getNodesByType(MethodDeclaration.class)
		def average = methods.stream()
				.mapToInt { it.parameters.size() }
				.average()

		return Metric.of('LongParameterList', average.orElse(0.0))
	}
}