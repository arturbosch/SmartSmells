package com.gitlab.artismarti.smartsmells

import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author artur
 */
class Main {

	def static benchmark = { closure ->
		def start = System.currentTimeMillis()
		closure.call()
		def now = System.currentTimeMillis()
		now - start
	}

	static void main(String... args) {
		def path = Paths.get("/home/artur/Repos/quide/Implementierung/QuideService/src")

//		(0..10).each { syncTest(path) }
		println()
		(0..0).each { asyncTest(path) }
	}

	static asyncTest(Path path) {
		println "\n Async Duration: " + benchmark { DetectorFacade.start(path) } / 1000
	}

	static syncTest(Path path) {
		println "\n Sync Duration: " + benchmark { DetectorFacade.syncStart(path) } / 1000
	}
}
