package system

import io.gitlab.arturbosch.jpal.core.CompilationStorage
import io.gitlab.arturbosch.jpal.core.JPAL
import io.gitlab.arturbosch.jpal.resolution.Resolver
import io.gitlab.arturbosch.smartsmells.Main
import io.gitlab.arturbosch.smartsmells.api.DetectorFacade
import spock.lang.Specification

import java.nio.file.Paths

/**
 * @author Artur Bosch
 */
class AllDetectorsPerformanceTest extends Specification {

	def "all detectors"() {
		given:
		def path = "/home/artur/Repos/elasticsearch/core/src/main/"
		CompilationStorage storage
		def time = Main.benchmark {
			storage = JPAL.newInstance(Paths.get(path))
		} // ~7500 to ~8500 ms
		// JPAL RC5: ~9600 ms
		println "CompilationStorage: $time ms"
		def facade = DetectorFacade.builder().fullStackFacade()

		when:
		def detectors = -1
		def smells = -1
		time = Main.benchmark {
			def smellSets = facade.justRun(storage.allCompilationInfo, new Resolver(storage)).smellSets
			detectors = smellSets.size()
			smells = smellSets.values().flatten().size()
		} //  27574 (w/o SS) ms, 35351 (w/ SS) ms
		// JPAL RC2: 23k-24456 ms, 33k ms
		// JPAL RC5: 34k ms
		println "Detectors: $time ms"
		println "#detectors: $detectors"
		println "#smells: $smells"

		then:
		detectors != -1
	}

}
