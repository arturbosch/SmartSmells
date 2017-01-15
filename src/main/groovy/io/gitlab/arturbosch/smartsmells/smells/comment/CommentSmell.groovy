package io.gitlab.arturbosch.smartsmells.smells.comment

import groovy.transform.Immutable
import groovy.transform.ToString
import io.gitlab.arturbosch.jpal.ast.source.SourcePath
import io.gitlab.arturbosch.jpal.ast.source.SourceRange
import io.gitlab.arturbosch.smartsmells.common.DetectionResult

/**
 * Represents a comment smell. There are two types of comment smell.
 * One is a comment above private and package private methods.
 * The other is a orphan comment inside a method. Both indicate excuses for a poor code style.
 *
 * @author artur
 */
@Immutable
@ToString(includePackage = false)
class CommentSmell implements DetectionResult {

	static String ORPHAN = "ORPHAN"
	static String PRIVATE = "PRIVATE"
	static String MISSING_PARAMETER = "MISSING_PARAMETER"
	static String MISSING_RETURN = "MISSING_RETURN"
	static String MISSING_JAVADOC = "MISSING_JAVADOC"

	String type
	String message

	boolean hasTODO
	boolean hasFIXME

	@Delegate
	SourcePath sourcePath
	@Delegate
	SourceRange sourceRange

	@Override
	String asCompactString() {
		"CommentSmell - $type\n\n$message"
	}
}
