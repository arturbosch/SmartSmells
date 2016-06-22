package com.gitlab.artismarti.smartsmells.metrics

import com.gitlab.artismarti.smartsmells.common.CompilationTree
import com.gitlab.artismarti.smartsmells.common.Test
import com.gitlab.artismarti.smartsmells.smells.cycle.AbstractCompilationTreeTest

/**
 * @author artur
 */
class MetricsTest extends AbstractCompilationTreeTest {

	def "mccabe"() {
		expect:
		mcc == 10

		where:
		method = Test.nth(Test.compile(Test.GOD_CLASS_DUMMY_PATH), 3)
		mcc = Metrics.mcCabe(method)
	}

	def "wmc"() {
		expect:
		wmc == 22

		where:
		clazz = Test.firstClass(Test.compile(Test.GOD_CLASS_DUMMY_PATH))
		wmc = Metrics.wmc(clazz)
	}

	def "noa"() {
		expect: "5 without inner classes"
		noa == 5

		where:
		clazz = Test.firstClass(Test.compile(Test.FEATURE_ENVY_PATH))
		noa = Metrics.noa(clazz)
	}

	def "nom"() {
		expect: "2 without inner classes"
		nom == 2

		where:
		clazz = Test.firstClass(Test.compile(Test.FEATURE_ENVY_PATH))
		nom = Metrics.nom(clazz)
	}

	def "atfd"() {
		expect: "uses ComplexDummy"
		atfd == 5

		where:
		clazz = Test.firstClass(Test.compile(Test.GOD_CLASS_DUMMY_PATH))
		atfd = Metrics.atfd(clazz)
	}

	def "tcc"() {
		expect:
		tcc == 0.7d

		where:
		clazz = Test.firstClass(Test.compile(Test.GOD_CLASS_DUMMY_PATH))
		tcc = Metrics.tcc(clazz)
	}


	def "sloc"() {
		when:
		def clazz = Test.firstClass(Test.compile(Test.COMMENT_DUMMY_PATH))
		int count = Metrics.sloc(clazz, Test.COMMENT_DUMMY_PATH)
		then:
		count == 6
	}


	def "loc"() {
		when:
		def clazz = Test.firstClass(Test.compile(Test.COMMENT_DUMMY_PATH))
		int count = Metrics.loc(clazz, Test.COMMENT_DUMMY_PATH)
		then:
		count == 18
	}

	def "cc"() {
		given:
		CompilationTree.registerRoot(Test.PATH)
		when:
		def clazz = Test.firstClass(Test.compile(Test.GOD_CLASS_DUMMY_PATH))
		int cc = Metrics.cc(clazz)
		then:
		cc == 2
	}

	def "cm"() {
		given:
		CompilationTree.registerRoot(Test.PATH)
		when:
		def clazz = Test.firstClass(Test.compile(Test.COMPLEX_METHOD_DUMMY_PATH))
		int cm = Metrics.cm(clazz)
		then:
		cm == 6
	}
}
