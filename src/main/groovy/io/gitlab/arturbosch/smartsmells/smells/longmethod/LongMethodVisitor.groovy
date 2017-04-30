package io.gitlab.arturbosch.smartsmells.smells.longmethod

import com.github.javaparser.ast.body.BodyDeclaration
import io.gitlab.arturbosch.smartsmells.common.visitor.MethodMetricVisitor
import io.gitlab.arturbosch.smartsmells.util.JavaLoc

/**
 * @author artur
 */
class LongMethodVisitor extends MethodMetricVisitor<LongMethod> {

	int size

	LongMethodVisitor(int threshold) {
		super(threshold)
	}

	static int bodyLength(BodyDeclaration n) {
		JavaLoc.analyze(n.toString().split("\\n").toList(), false, false)
	}

	@Override
	protected byThreshold(BodyDeclaration n) {
		size = bodyLength(n)
		return size > threshold
	}

	protected addSmell(BodyDeclaration n) {
		smells.add(newLongMethod(n, size))
	}
}
