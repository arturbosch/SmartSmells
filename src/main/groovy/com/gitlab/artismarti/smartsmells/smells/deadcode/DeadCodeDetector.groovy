package com.gitlab.artismarti.smartsmells.smells.deadcode

import com.gitlab.artismarti.smartsmells.config.Defaults
import com.gitlab.artismarti.smartsmells.common.Detector
import com.gitlab.artismarti.smartsmells.config.Smell
import com.gitlab.artismarti.smartsmells.common.Visitor

import java.nio.file.Path

/**
 * @author artur
 */
class DeadCodeDetector extends Detector<DeadCode> {

	private boolean onlyPrivate

	DeadCodeDetector(boolean onlyPrivate = Defaults.ONLY_PRIVATE_DEAD_CODE) {
		this.onlyPrivate = onlyPrivate
	}

	@Override
	protected Visitor getVisitor(Path path) {
		return new DeadCodeVisitor(path, onlyPrivate)
	}

	@Override
	Smell getType() {
		return Smell.DEAD_CODE
	}
}
