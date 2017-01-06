package io.gitlab.arturbosch.smartsmells.smells.shotgunsurgery

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.ast.ClassHelper
import io.gitlab.arturbosch.jpal.ast.source.SourcePath
import io.gitlab.arturbosch.jpal.ast.source.SourceRange
import io.gitlab.arturbosch.smartsmells.common.Visitor
import io.gitlab.arturbosch.smartsmells.common.visitor.InternalVisitor
import io.gitlab.arturbosch.smartsmells.metrics.Metrics

import java.nio.file.Path

/**
 * @author Artur Bosch
 */
@CompileStatic
class ShotgunSurgeryVisitor extends Visitor<ShotgunSurgery> {

	private int ccThreshold
	private int cmThreshold

	ShotgunSurgeryVisitor(Path path, int cc, int cm) {
		super(path)
		this.cmThreshold = cm
		this.ccThreshold = cc
	}

	@Override
	void visit(CompilationUnit n, Object arg) {

		def classes = n.getNodesByType(ClassOrInterfaceDeclaration.class)

		classes.each {
			def classVisitor = new InternalSSVisitor(path)
			classVisitor.visit(it)
		}

	}

	@CompileStatic
	private class InternalSSVisitor extends InternalVisitor {

		InternalSSVisitor(Path thePath) {
			super(thePath)
		}

		@Override
		protected void visit(ClassOrInterfaceDeclaration n) {
			if (isEmpty(n)) return

			def cc = Metrics.cc(n)
			def cm = Metrics.cm(n)

			if (cc > ccThreshold && cm > cmThreshold) {
				smells.add(new ShotgunSurgery(n.nameAsString, ClassHelper.createFullSignature(n),
						cc, cm, ccThreshold, cmThreshold, SourcePath.of(thePath), SourceRange.fromNode(n)))
			}
		}

	}

}
