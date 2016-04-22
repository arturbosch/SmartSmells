package com.gitlab.artismarti.smartsmells.comment

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.ModifierSet
import com.github.javaparser.ast.comments.Comment
import com.gitlab.artismarti.smartsmells.common.BadSmellHelper
import com.gitlab.artismarti.smartsmells.common.Visitor
import com.gitlab.artismarti.smartsmells.domain.SourcePath

import java.nio.file.Path

/**
 * Visits all method declaration of a compilation unit and examines them
 * for orphan comments or comments above private/package-private methods.
 *
 * @author artur
 */
class CommentVisitor extends Visitor {

	private final
	static String DEFAULT_MESSAGE = "are considered as a smell, try to refactor your code so others will understand it without a comment."
	private final static String ORPHAN_MESSAGE = "Loosely comments " + DEFAULT_MESSAGE
	private final static String JAVADOC_MESSAGE = "Javadoc over private or package private methods " + DEFAULT_MESSAGE

	CommentVisitor(Path path) {
		super(path)
	}

	@Override
	void visit(MethodDeclaration n, Object arg) {
		if (n.comment != null) {
			def modifiers = n.modifiers
			if (ModifierSet.isPrivate(modifiers) || ModifierSet.hasPackageLevelAccess(modifiers)) {
				addCommentSmell(CommentSmell.PRIVATE, n.comment, JAVADOC_MESSAGE)
			}
		}

		for (comment in n.getAllContainedComments()) {
			addCommentSmell(CommentSmell.ORPHAN, comment, ORPHAN_MESSAGE)
		}

		super.visit(n, arg)
	}

	private void addCommentSmell(String type, Comment comment, String message) {

		smells.add(new CommentSmell(type, comment.toString(), message,
				hasTodoOrFixme(comment, "TODO"), hasTodoOrFixme(comment, "FIXME"),
				SourcePath.of(path), BadSmellHelper.createSourceRangeFromNode(comment)))
	}

	private static boolean hasTodoOrFixme(Comment comment, String pattern) {
		comment.content.contains(pattern)
	}
}
