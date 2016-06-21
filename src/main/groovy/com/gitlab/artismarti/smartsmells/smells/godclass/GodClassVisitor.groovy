package com.gitlab.artismarti.smartsmells.smells.godclass

import com.github.javaparser.ASTHelper
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.gitlab.artismarti.smartsmells.common.Visitor
import com.gitlab.artismarti.smartsmells.common.helper.*
import com.gitlab.artismarti.smartsmells.common.source.SourcePath
import com.gitlab.artismarti.smartsmells.common.source.SourcePosition
import com.gitlab.artismarti.smartsmells.common.source.SourceRange

import java.nio.file.Path
import java.util.stream.Collectors

/**
 * GodClasses := ((ATFD, TopValues(20%)) ∧ (ATFD, HigherThan(4))) ∧
 * ((WMC, HigherThan(20)) ∨ (TCC, LowerThan(0.33)).
 *
 * Metric proposed by:
 * R. Marinescu, Measurement and quality in object-oriented design, Ph.D. thesis in
 * the Faculty of Automatics and Computer Science of the Politehnica University of
 * Timisoara, 2003.
 *
 * @author artur
 */
class GodClassVisitor extends Visitor<GodClass> {

	private int accessToForeignDataThreshold
	private int weightedMethodCountThreshold
	private BigDecimal tiedClassCohesionThreshold

	GodClassVisitor(int accessToForeignDataThreshold,
	                int weightedMethodCountThreshold,
	                double tiedClassCohesionThreshold,
	                Path path) {
		super(path)
		this.accessToForeignDataThreshold = accessToForeignDataThreshold
		this.weightedMethodCountThreshold = weightedMethodCountThreshold
		this.tiedClassCohesionThreshold = tiedClassCohesionThreshold
	}

	@Override
	void visit(CompilationUnit n, Object arg) {

		def classes = ASTHelper.getNodesByType(n, ClassOrInterfaceDeclaration.class)

		classes.each {
			def classVisitor = new InternalGodClassVisitor()
			classVisitor.visit(it)
		}

	}

	private class InternalGodClassVisitor extends VoidVisitorAdapter<Object> {

		private int atfd = 0
		private int wmc = 0
		private double tcc = 0.0

		private String currentClassName = ""
		private List<String> fields = new ArrayList<>()
		private List<String> methods = new ArrayList<>()

		void visit(ClassOrInterfaceDeclaration n) {
			if (TypeHelper.isEmptyBody(n)) return
			if (TypeHelper.hasNoMethods(n)) return

			this.currentClassName = n.name

			def filteredFields = NodeHelper.findFields(n).stream()
					.filter { ClassHelper.inCurrentClass(it, currentClassName) }
					.collect(Collectors.toList())

			def filteredMethods = NodeHelper.findMethods(n).stream()
					.filter { ClassHelper.inCurrentClass(it, currentClassName) }
					.collect(Collectors.toList())

			fields = NameHelper.toFieldNames(filteredFields)
			methods = NameHelper.toMethodNames(filteredMethods)

			// traverse all nodes and calculate values before evaluate for god class
			super.visit(n, null)

			tcc = MetricHelper.tcc(n)
			wmc = MetricHelper.wmc(n)

			if (checkThresholds()) {
				addSmell(n)
			}
		}

		private boolean checkThresholds() {
			atfd > accessToForeignDataThreshold &&
					wmc > weightedMethodCountThreshold &&
					tcc < tiedClassCohesionThreshold
		}

		private boolean addSmell(ClassOrInterfaceDeclaration n) {
			smells.add(new GodClass(n.name, BadSmellHelper.createClassSignature(n), wmc, tcc, atfd,
					weightedMethodCountThreshold, tiedClassCohesionThreshold,
					accessToForeignDataThreshold, SourcePath.of(path),
					SourceRange.of(SourcePosition.of(n.beginLine, n.beginColumn),
							SourcePosition.of(n.endLine, n.endColumn))))
		}

		def inScope(Node node, Closure code) {
			def declaredClass = NodeHelper.findDeclaringClass(node)
					.filter { it.name == currentClassName }
			if (declaredClass.isPresent()) {
				code()
			}
		}

		@Override
		void visit(ConstructorDeclaration n, Object arg) {
			inScope(n) {
				wmc += MetricHelper.mcCabe(n)
				super.visit(n, arg)
			}
		}

		@Override
		void visit(MethodDeclaration n, Object arg) {
			inScope(n) {

				wmc += MetricHelper.mcCabe(n)

				ASTHelper.getNodesByType(n, MethodCallExpr.class).each {
					if (isNotMemberOfThisClass(it.name, methods)) {
						if (isNotAGetterOrSetter(it.name)) {
							atfd++
						}
					}
				}

				ASTHelper.getNodesByType(n, FieldAccessExpr.class).each {
					if (isNotMemberOfThisClass(it.field, fields)) {
						atfd++
					}
				}

				super.visit(n, arg)
			}
		}

		private static boolean isNotMemberOfThisClass(String name, List<String> members) {
			!members.contains(name)
		}

		static boolean isNotAGetterOrSetter(String name) {
			!name.startsWith("get") && !name.startsWith("set") && !name.startsWith("is")
		}

	}
}

