package io.gitlab.arturbosch.smartsmells.smells.featureenvy

import io.gitlab.arturbosch.smartsmells.common.Test
import spock.lang.Specification

/**
 * @author artur
 */
class FeatureEnvyDetectorTest extends Specification {

	def "find one feature envy method not ignoring static methods"() {

		expect:
		smells.size() == 3
		methods.collect { it.name }.contains("envyMethod")
		methods.collect { it.signature }.contains("public void envyMethod()")
		methods.collect { it.objectName }.contains("otherLogic")
		methods.collect { it.objectSignature }.contains("HasFeatures")
		methods.collect { it.factor }.grep { it > 0.5d }.size() > 0
		methods.collect { it.factorThreshold }.grep { it >= 0.52d }.size() > 0

		where:
		smells = new FeatureEnvyDetector().run(Test.FEATURE_ENVY_PATH)
		methods = Arrays.asList(smells[0], smells[1], smells[2])
	}

	def "find one feature envy method ignoring static methods"() {

		expect:
		smells.size() == 2

		where:
		smells = new FeatureEnvyDetector(ignoreStatic: true).run(Test.FEATURE_ENVY_PATH)
	}

}
