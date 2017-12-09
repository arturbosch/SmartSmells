package io.gitlab.arturbosch.smartsmells.metrics

import groovy.transform.Immutable
import groovy.transform.ToString
import io.gitlab.arturbosch.jpal.ast.source.SourcePath
import io.gitlab.arturbosch.jpal.ast.source.SourceRange

/**
 * @author Artur Bosch
 */
@Immutable
@ToString(includePackage = false, includeNames = true)
class ClassInfo {

	String name
	String signature
	final List<Metric> metrics

	@Delegate
	SourcePath sourcePath
	@Delegate
	SourceRange sourceRange

	@Override
	String toString() {
		return "ClassInfo{name=$name, signature=$signature\n\t\t" +
				metrics.collect { it.toString() }.join("\n\t\t") +
				"}"
	}
}
