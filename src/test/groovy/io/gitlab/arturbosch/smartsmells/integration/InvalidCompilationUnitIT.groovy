package io.gitlab.arturbosch.smartsmells.integration

import io.gitlab.arturbosch.smartsmells.api.DetectorFacade
import spock.lang.Specification

import java.nio.file.Paths
import java.util.concurrent.CompletionException

/**
 * @author artur
 */
class InvalidCompilationUnitIT extends Specification {

	def "handle parse exception if a compilation unit cannot be constructed due to syntax errors"() {
		given:
		def path = Paths.get(getClass().getResource("/invalid/InvalidDummy.java").getFile())

		when:
		DetectorFacade.fullStackFacade().run(path)

		then:
		notThrown(CompletionException.class)
	}
}
