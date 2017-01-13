package io.gitlab.arturbosch.smartsmells.api

import io.gitlab.arturbosch.jpal.core.CompilationInfo
import io.gitlab.arturbosch.jpal.core.CompilationInfoProcessor
import io.gitlab.arturbosch.jpal.core.CompilationStorage
import io.gitlab.arturbosch.jpal.core.JPAL
import io.gitlab.arturbosch.jpal.resolution.Resolver
import io.gitlab.arturbosch.smartsmells.common.DetectionResult
import io.gitlab.arturbosch.smartsmells.common.Detector
import io.gitlab.arturbosch.smartsmells.config.DetectorConfig
import io.gitlab.arturbosch.smartsmells.config.DetectorInitializer
import io.gitlab.arturbosch.smartsmells.metrics.ClassInfoDetector
import io.gitlab.arturbosch.smartsmells.smells.comment.CommentDetector
import io.gitlab.arturbosch.smartsmells.smells.complexmethod.ComplexMethodDetector
import io.gitlab.arturbosch.smartsmells.smells.cycle.CycleDetector
import io.gitlab.arturbosch.smartsmells.smells.dataclass.DataClassDetector
import io.gitlab.arturbosch.smartsmells.smells.deadcode.DeadCodeDetector
import io.gitlab.arturbosch.smartsmells.smells.featureenvy.FeatureEnvyDetector
import io.gitlab.arturbosch.smartsmells.smells.godclass.GodClassDetector
import io.gitlab.arturbosch.smartsmells.smells.largeclass.LargeClassDetector
import io.gitlab.arturbosch.smartsmells.smells.longmethod.LongMethodDetector
import io.gitlab.arturbosch.smartsmells.smells.longparam.LongParameterListDetector
import io.gitlab.arturbosch.smartsmells.smells.messagechain.MessageChainDetector
import io.gitlab.arturbosch.smartsmells.smells.middleman.MiddleManDetector
import io.gitlab.arturbosch.smartsmells.util.Validate

import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/**
 * @author artur
 */
class DetectorFacade {

	private List<Detector<DetectionResult>> detectors = new LinkedList<>()

	private DetectorFacade(List<Detector> detectors) {
		this.detectors = detectors
	}

	static DetectorFacadeBuilder builder() {
		return new DetectorFacadeBuilder()
	}

	static DetectorFacade fullStackFacade() {
		return new DetectorFacadeBuilder().fullStackFacade()
	}

	static DetectorFacade fromConfig(final DetectorConfig config) {
		Validate.notNull(config, "Configuration must not be null!")
		return new DetectorFacade(DetectorInitializer.init(config))
	}

	SmellResult run(Path startPath) {
		return internalRun(startPath) { JPAL.new(startPath) }
	}

	def <T> SmellResult runWithProcessor(Path startPath, CompilationInfoProcessor<T> processor) {
		return internalRun(startPath) { JPAL.new(startPath, processor) }
	}

	private SmellResult internalRun(Path startPath, Closure<CompilationStorage> create) {
		Validate.notNull(startPath)

		def storage = create()
		def resolver = new Resolver(storage)
		def infos = storage.getAllCompilationInfo()

		return justRun(infos, resolver)
	}

	SmellResult justRun(List<CompilationInfo> infos, Resolver resolver) {
		def forkJoinPool = new ForkJoinPool(
				Runtime.getRuntime().availableProcessors(),
				ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)

		List<CompletableFuture> futures = new ArrayList<>(infos.size())

		infos.forEach { info ->
			futures.add(CompletableFuture.runAsync({
				for (Detector detector : detectors) {
					detector.execute(info, resolver)
				}
			}, forkJoinPool).exceptionally { handle(it) })
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join()
		forkJoinPool.shutdown()
		return new SmellResult(detectors.collectEntries { [it.type, it.smells] })
	}

	private static ArrayList handle(Throwable throwable) {
		println throwable.printStackTrace()
		return new ArrayList<>()
	}

	int numberOfDetectors() {
		return detectors.size()
	}

	private static class DetectorFacadeBuilder {

		private List<Detector> detectors = new LinkedList<>()

		DetectorFacadeBuilder with(Detector detector) {
			Validate.notNull(detector)
			detectors.add(detector)
			return this
		}

		DetectorFacade fullStackFacade() {
			detectors = [new ComplexMethodDetector(), new CommentDetector(), new LongMethodDetector(),
						 new LongParameterListDetector(), new DeadCodeDetector(), new LargeClassDetector(),
						 new MessageChainDetector(), new MiddleManDetector(), new FeatureEnvyDetector(),
						 new CycleDetector(), new DataClassDetector(), new GodClassDetector()/*new ShotgunSurgeryDetector()*/]
			build()
		}

		static DetectorFacade metricFacade() {
			return new DetectorFacadeBuilder().with(new ClassInfoDetector()).build()
		}

		DetectorFacade build() {
			return new DetectorFacade(detectors)
		}
	}


}

import java.util.concurrent.ForkJoinPool