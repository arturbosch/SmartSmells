package io.gitlab.arturbosch.smartsmells.smells.longparam

import com.github.javaparser.ast.body.BodyDeclaration
import com.github.javaparser.ast.body.Parameter
import io.gitlab.arturbosch.smartsmells.common.helper.MethodHelper
import io.gitlab.arturbosch.smartsmells.common.visitor.MethodMetricVisitor

import java.nio.file.Path

/**
 * @author artur
 */
class LongParameterListVisitor extends MethodMetricVisitor<LongParameterList> {

	List<Parameter> parameters

	LongParameterListVisitor(int threshold, Path path) {
		super(threshold, path)
	}

	@Override
	protected byThreshold(BodyDeclaration n) {
		parameters = MethodHelper.extractParameters(n)
		return parameters.size() > threshold
	}

	@Override
	protected addSmell(BodyDeclaration n) {
		def size = parameters.size()
		smells.add(new LongParameterList(newLongMethod(n, size),
				parameters.collect { it.toStringWithoutComments() }, size, threshold))
	}
}
