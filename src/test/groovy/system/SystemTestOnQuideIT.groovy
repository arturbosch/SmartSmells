package system

import com.gitlab.artismarti.smartsmells.config.Smell
import com.gitlab.artismarti.smartsmells.out.XMLWriter
import com.gitlab.artismarti.smartsmells.start.DetectorFacade
import spock.lang.Specification

import java.nio.file.Paths
import java.util.stream.Collectors
/**
 * @author artur
 */
class SystemTestOnQuideIT extends Specification {

	def "run on quide and find no same feature envy twice"() {
		given:
//		def path = "/home/artur/Repos/quide/Implementierung/QuIDE_Plugin/src/de.uni_bremen.st.quide.plugin/"
//		def path = "/home/artur/Arbeit/pooka-co/trunk/pooka/src"
//		def path = "/home/artur/Repos/quide/Implementierung/Analyse/in/7004a9e5bae12864d2c1e05e3233183cbf2006c2/modules/elasticsearch/src/main/java/org/elasticsearch/index/translog"
//		Resources.getResource("")
		def path = Paths.getResource("/cornercases").getFile()
//		def path = "/home/artur/Repos/quide/Implementierung/Analyse/in/7004a9e5bae12864d2c1e05e3233183cbf2006c2"
		def result = DetectorFacade.builder().fullStackFacade().run(Paths.get(path))
		def envies = result.of(Smell.FEATURE_ENVY)
		when:
		def duplicates = envies.stream()
				.filter { Collections.frequency(envies, it) > 1 }
				.collect(Collectors.toSet())
		envies.each { println it.toString() }
		then:
		duplicates.isEmpty()
		when:
		def xml = XMLWriter.toXml(result)
//		println xml
		then:
		xml.contains("FeatureEnvy")
	}
}
