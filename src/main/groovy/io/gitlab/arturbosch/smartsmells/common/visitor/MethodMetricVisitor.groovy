package io.gitlab.arturbosch.smartsmells.common.visitor

import com.github.javaparser.ast.body.BodyDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.BlockStmt
import io.gitlab.arturbosch.jpal.ast.source.SourcePath
import io.gitlab.arturbosch.jpal.ast.source.SourceRange
import io.gitlab.arturbosch.smartsmells.common.DetectionResult
import io.gitlab.arturbosch.smartsmells.common.Visitor
import io.gitlab.arturbosch.smartsmells.smells.longmethod.LongMethod

import java.nio.file.Path

/**
 * @author artur
 */
abstract class MethodMetricVisitor<T extends DetectionResult> extends Visitor<T> {

	int threshold

	MethodMetricVisitor(int threshold, Path path) {
		super(path)
		this.threshold = threshold
	}

	@Override
	void visit(ConstructorDeclaration node, Object arg) {
		visitBlock(Optional.ofNullable(node.body), node)
	}

	private void visitBlock(Optional<BlockStmt> blockStmt, BodyDeclaration body) {
		blockStmt.map { it.statements }
				.filter { byThreshold(body) }
				.ifPresent { addSmell(body) }
	}

	@Override
	void visit(MethodDeclaration node, Object arg) {
		visitBlock(node.body, node)
	}

	protected LongMethod newLongMethod(BodyDeclaration n, int size) {
		if (n instanceof MethodDeclaration)
			longMethodIntern(n.declarationAsString, n.nameAsString,
					n.getDeclarationAsString(false, false, true), n, size)
		else {
			def node = (ConstructorDeclaration) n
			longMethodIntern(node.declarationAsString, node.nameAsString,
					node.getDeclarationAsString(false, false, true), n, size)
		}
	}

	private LongMethod longMethodIntern(String header, String name, String signature, BodyDeclaration n, int size) {
		new LongMethod(header, name, signature, size, threshold, SourceRange.fromNode(n), SourcePath.of(path))
	}

	protected abstract byThreshold(BodyDeclaration n)

	protected abstract addSmell(BodyDeclaration n)
}
