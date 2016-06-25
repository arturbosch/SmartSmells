package com.gitlab.artismarti.smartsmells.common

import com.github.javaparser.ASTHelper
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.gitlab.artismarti.smartsmells.common.helper.TypeHelper

import java.nio.file.Path

/**
 * @author artur
 */
class CompilationInfo {

	QualifiedType qualifiedType
	CompilationUnit unit
	Path path
	List<QualifiedType> usedTypes
	Set<QualifiedType> innerClasses

	private CompilationInfo(QualifiedType qualifiedType, CompilationUnit unit, Path path,
	                        List<QualifiedType> usedTypes, Set<QualifiedType> innerClasses) {

		this.qualifiedType = qualifiedType
		this.unit = unit
		this.path = path
		this.usedTypes = usedTypes
		this.innerClasses = innerClasses
	}

	static CompilationInfo of(QualifiedType qualifiedType, CompilationUnit unit, Path path) {
		List<QualifiedType> types = unit.imports.stream()
				.filter { !it.isEmptyImportDeclaration() }
				.map { it.name.toStringWithoutComments() }
				.filter { it.startsWith("java") }
				.map { new QualifiedType(it, QualifiedType.TypeToken.REFERENCE) }
				.collect()
		def innerClasses = TypeHelper.getQualifiedTypesOfInnerClasses(unit)
		return new CompilationInfo(qualifiedType, unit, path, types, innerClasses)
	}

	boolean isWithinScope(QualifiedType type) {
		return usedTypes.contains(type) || searchForTypeWithinUnit(unit, type)
	}

	private static boolean searchForTypeWithinUnit(CompilationUnit unit, QualifiedType qualifiedType) {
		def shortName = qualifiedType.shortName()
		def types = ASTHelper.getNodesByType(unit, ClassOrInterfaceType.class)
		return types.any { it.name == shortName }
	}

	@Override
	public String toString() {
		return "CompilationInfo{" +
				"qualifiedType=" + qualifiedType +
				'}';
	}
}
