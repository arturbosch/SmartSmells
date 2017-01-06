package io.gitlab.arturbosch.smartsmells.smells.shotgunsurgery

import io.gitlab.arturbosch.smartsmells.common.Detector
import io.gitlab.arturbosch.smartsmells.common.Visitor
import io.gitlab.arturbosch.smartsmells.config.Defaults
import io.gitlab.arturbosch.smartsmells.config.Smell

import java.nio.file.Path

/**
 * @author Artur Bosch
 */
class ShotgunSurgeryDetector extends Detector<ShotgunSurgery> {

	private int cc
	private int cm

	ShotgunSurgeryDetector(int cc = Defaults.CHANGING_CLASSES, int cm = Defaults.CHANGING_METHODS) {
		this.cm = cm
		this.cc = cc
	}

	@Override
	protected Visitor getVisitor(Path path) {
		return new ShotgunSurgeryVisitor(path, cc, cm)
	}

	@Override
	Smell getType() {
		return Smell.SHOTGUN_SURGERY
	}
}
