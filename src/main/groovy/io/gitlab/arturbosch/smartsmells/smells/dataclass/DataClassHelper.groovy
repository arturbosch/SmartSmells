package io.gitlab.arturbosch.smartsmells.smells.dataclass

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import io.gitlab.arturbosch.jpal.ast.MethodHelper

/**
 * @author artur
 */
final class DataClassHelper extends VoidVisitorAdapter {

	private DataClassHelper() {}

	static boolean checkMethods(List<MethodDeclaration> methods) {
		boolean isDataClass = true
		methods.grep { !isDataClassMethod(it as MethodDeclaration) }.each {
			isDataClass &= MethodHelper.isGetterOrSetter(it)
		}
		return isDataClass
	}

	static boolean isDataClassMethod(MethodDeclaration method) {
		def maybeOverride = method.annotations.find { it.name.name == "Override" }
		def is
		switch (method.name) {
			case "equals":
			case "toString":
			case "compareTo":
			case "hashCode": is = true; break
			default: is = false
		}
		return is && maybeOverride != null
	}
}
