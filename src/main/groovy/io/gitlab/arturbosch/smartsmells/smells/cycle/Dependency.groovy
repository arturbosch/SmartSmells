package io.gitlab.arturbosch.smartsmells.smells.cycle

import groovy.transform.Immutable
import io.gitlab.arturbosch.smartsmells.common.Smelly
import io.gitlab.arturbosch.smartsmells.common.source.SourcePath
import io.gitlab.arturbosch.smartsmells.common.source.SourceRange

/**
 * @author artur
 */
@Immutable
class Dependency implements Smelly {

	String name
	String signature

	@Delegate
	SourcePath sourcePath
	@Delegate
	SourceRange sourceRange

	static Dependency of(String entityName, String entitySignature, SourcePath sourcePath, SourceRange sourceRange) {
		return new Dependency(entityName, entitySignature, sourcePath, sourceRange)
	}

	@Override
	public String toString() {
		return "{$name, $signature, $sourcePath, $sourceRange}"
	}

	@Override
	String asCompactString() {
		"Dependency \n\nName: $name"
	}

}
