package com.gitlab.artismarti.smartsmells.middleman

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.ReturnStmt
import com.gitlab.artismarti.smartsmells.common.BadSmellHelper
import com.gitlab.artismarti.smartsmells.common.MethodHelper
import com.gitlab.artismarti.smartsmells.common.NodeHelper
import com.gitlab.artismarti.smartsmells.common.Visitor
import com.gitlab.artismarti.smartsmells.domain.SourcePath

import java.nio.file.Path

/**
 * @author artur
 */
class MiddleManVisitor extends Visitor<MiddleMan> {

	MiddleManVisitor(Path path) {
		super(path)
	}

	@Override
	void visit(ClassOrInterfaceDeclaration n, Object arg) {
		if (isEmptyBody(n)) return
		if (hasNoMethods(n)) return

		def methods = NodeHelper.findMethods(n)

		def allMatch = methods.stream()
				.allMatch {
			hasBodySizeOne(it) &&
					hasComplexityOfOne(it) &&
					useSameParametersForMethodInvocation(it) &&
					hasNoFilteredAnnotations(it)
		}

		if (allMatch) {
			smells.add(new MiddleMan(n.name, BadSmellHelper.createSignature(n),
					SourcePath.of(path), BadSmellHelper.createSourceRangeFromNode(n)))
		}

		super.visit(n, arg)
	}

	private static boolean isEmptyBody(ClassOrInterfaceDeclaration n) {
		n.members.empty
	}

	private static boolean hasNoMethods(ClassOrInterfaceDeclaration n) {
		n.members.stream().filter { it instanceof MethodDeclaration }.count() == 0
	}

	private static boolean hasBodySizeOne(MethodDeclaration method) {
		return Optional.ofNullable(method.body)
				.filter { it.stmts.size() == 1 }
				.isPresent()
	}

	private static boolean useSameParametersForMethodInvocation(MethodDeclaration method) {
		def statement = method.body.stmts[0]
		if (statement instanceof ReturnStmt) {
			def expr = statement.expr
			if (expr instanceof MethodCallExpr) {
				def argsExpr = expr.args.collect { it.toStringWithoutComments() }
				def argsMethod = method.parameters.collect { it.id.name }
				return argsExpr.containsAll(argsMethod)
			}
		}
		return false
	}

	private static boolean hasComplexityOfOne(MethodDeclaration it) {
		MethodHelper.calcMcCabe(it) == 1
	}

	private static boolean hasNoFilteredAnnotations(MethodDeclaration method) {
		method.annotations.stream().noneMatch { it.name.name == "Bean" }
	}

}

