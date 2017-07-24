package io.gitlab.arturbosch.smartsmells.smells.shotgunsurgery

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.ast.ClassHelper
import io.gitlab.arturbosch.jpal.ast.source.SourcePath
import io.gitlab.arturbosch.jpal.ast.source.SourceRange
import io.gitlab.arturbosch.jpal.core.CompilationInfo
import io.gitlab.arturbosch.jpal.resolution.Resolver
import io.gitlab.arturbosch.smartsmells.common.Visitor
import io.gitlab.arturbosch.smartsmells.common.visitor.InternalVisitor
import io.gitlab.arturbosch.smartsmells.smells.ElementTarget

/**
 * @author Artur Bosch
 */
@CompileStatic
class ShotgunSurgeryVisitor extends Visitor<ShotgunSurgery> {

	private int ccThreshold
	private int cmThreshold

	ShotgunSurgeryVisitor(int cc, int cm) {
		this.cmThreshold = cm
		this.ccThreshold = cc
	}

	@Override
	void visit(CompilationUnit n, Resolver resolver) {

		def classes = n.getChildNodesByType(ClassOrInterfaceDeclaration.class)

		classes.each {
			def classVisitor = new InternalSSVisitor()
			classVisitor.visit(it, resolver)
		}

	}

	private CompilationInfo thisInfo() {
		return info
	}

	@CompileStatic
	private class InternalSSVisitor extends InternalVisitor {

		@Override
		void visit(ClassOrInterfaceDeclaration n, Resolver resolver) {
			if (isEmpty(n)) return

			def cmCc = CMCCMetrics.raise(n, resolver)
			def cm = cmCc.a
			def cc = cmCc.b

			if (cc > ccThreshold && cm > cmThreshold) {
				smells.add(new ShotgunSurgery(n.nameAsString, ClassHelper.createFullSignature(n),
						cc, cm, ccThreshold, cmThreshold, SourcePath.of(thisInfo()),
						SourceRange.fromNode(n), ElementTarget.CLASS))
			}
		}

	}

}
