package io.gitlab.arturbosch.smartsmells.smells.nestedblockdepth

import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.Immutable
import groovy.transform.ToString
import io.gitlab.arturbosch.jpal.ast.source.SourcePath
import io.gitlab.arturbosch.jpal.ast.source.SourceRange
import io.gitlab.arturbosch.smartsmells.common.DetectionResult
import io.gitlab.arturbosch.smartsmells.smells.MethodSpecific

/**
 * @author Artur Bosch
 */
@Immutable
@ToString(includePackage = false)
class NestedBlockDepth implements DetectionResult, MethodSpecific {

	String methodName
	String methodSignature

	int depth
	int depthThreshold

	@Delegate
	SourceRange sourceRange
	@Delegate
	SourcePath sourcePath

	@Override
	String asCompactString() {
		"NestedBlockDepth \n\ndepth: $depth with threshold: $depthThreshold"
	}

	@Override
	MethodSpecific copy(MethodDeclaration method) {
		return new NestedBlockDepth(method.getNameAsString(), method.declarationAsString,
				depth, depthThreshold, SourceRange.fromNode(method), sourcePath)
	}
}
