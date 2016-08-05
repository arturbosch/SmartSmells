package io.gitlab.arturbosch.smartsmells.smells.dataclass

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import io.gitlab.arturbosch.smartsmells.common.helper.MethodHelper

/**
 * @author artur
 */
class DataClassHelper extends VoidVisitorAdapter {

	private DataClassHelper() {}

	static boolean checkMethods(List<MethodDeclaration> methods) {
		boolean isDataClass = true
		methods.each {
			if (it.getBody()) {
				def stmts = it.getBody().stmts
				if (stmts) {
					isDataClass &= MethodHelper.isGetterOrSetter(stmts)
				}
			}
		}
		return isDataClass
	}
}