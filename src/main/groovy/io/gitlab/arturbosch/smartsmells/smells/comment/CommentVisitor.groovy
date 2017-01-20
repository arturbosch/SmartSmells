package io.gitlab.arturbosch.smartsmells.smells.comment

import com.github.javaparser.ast.AccessSpecifier
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.Comment
import io.gitlab.arturbosch.jpal.ast.source.SourcePath
import io.gitlab.arturbosch.jpal.ast.source.SourceRange
import io.gitlab.arturbosch.jpal.resolution.Resolver
import io.gitlab.arturbosch.smartsmells.common.Visitor

/**
 * Visits all method declaration of a compilation unit and examines them
 * for orphan comments or comments above private/package-private methods.
 *
 * @author artur
 */
class CommentVisitor extends Visitor<CommentSmell> {

	@Override
	void visit(MethodDeclaration n, Resolver arg) {
		if (n.comment != null) {
			def modifiers = n.modifiers
			def specifier = Modifier.getAccessSpecifier(modifiers)
			if (specifier == AccessSpecifier.PRIVATE || specifier == AccessSpecifier.DEFAULT) {
				addCommentSmell(CommentSmell.Type.PRIVATE, n.declarationAsString, n.comment)
			}
		}

		for (comment in n.getAllContainedComments()) {
			addCommentSmell(CommentSmell.Type.ORPHAN, "orphan comment", comment)
		}

		super.visit(n, arg)
	}

	private void addCommentSmell(CommentSmell.Type type, String name, Comment comment) {

		smells.add(new CommentSmell(type, name,
				hasTodoOrFixme(comment, "TODO"),
				hasTodoOrFixme(comment, "FIXME"),
				SourcePath.of(path), SourceRange.fromNode(comment)))
	}

	private static boolean hasTodoOrFixme(Comment comment, String pattern) {
		comment.content.contains(pattern)
	}
}
