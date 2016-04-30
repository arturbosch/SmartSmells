package com.gitlab.artismarti.smartsmells.dataclass

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.gitlab.artismarti.smartsmells.common.helper.BadSmellHelper
import com.gitlab.artismarti.smartsmells.common.Visitor
import com.gitlab.artismarti.smartsmells.common.source.SourcePath
import com.gitlab.artismarti.smartsmells.common.source.SourceRange

import java.nio.file.Path

/**
 * @author artur
 */
class DataClassVisitor extends Visitor<DataClass> {

	DataClassVisitor(Path path) {
		super(path)
	}

	@Override
	void visit(ClassOrInterfaceDeclaration n, Object arg) {
		if (n.interface) return

		def visitor = new ClassVisitor()
		n.accept(visitor, null)

		if (visitor.isDataClass()) {
			def signature = BadSmellHelper.createSignature(n)
			smells.add(new DataClass(n.getName(), signature,
					SourceRange.of(n.beginLine, n.endLine, n.beginColumn, n.endColumn),
					SourcePath.of(path)))
		}
	}

}
