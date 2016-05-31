package com.gitlab.artismarti.smartsmells.cycle

import com.gitlab.artismarti.smartsmells.common.CompilationTree
import spock.lang.Specification

/**
 * @author artur
 */
abstract class AbstractCycleTest extends Specification {

	def cleanup() {
		CompilationTree.reset()
	}

}
