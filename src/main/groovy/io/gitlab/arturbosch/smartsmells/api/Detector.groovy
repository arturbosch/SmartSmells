package io.gitlab.arturbosch.smartsmells.api

import io.gitlab.arturbosch.jpal.core.CompilationInfo
import io.gitlab.arturbosch.jpal.core.JPAL
import io.gitlab.arturbosch.jpal.resolution.Resolver
import io.gitlab.arturbosch.smartsmells.common.Visitor
import io.gitlab.arturbosch.smartsmells.config.DetectorConfig
import io.gitlab.arturbosch.smartsmells.config.Smell
import io.gitlab.arturbosch.smartsmells.smells.DetectionResult
import io.gitlab.arturbosch.smartsmells.util.Validate

import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.BinaryOperator
import java.util.stream.Collectors

/**
 * @author artur
 */
abstract class Detector<T extends DetectionResult> {

	private DetectorConfig config = null
	protected Smell type = getType()
	private Queue<T> smells = new ConcurrentLinkedQueue<>()

	/**
	 * Binary operator combines two sets into one.
	 */
	static combine = new BinaryOperator<Set<T>>() {
		@Override
		Set<T> apply(Set<T> l1, Set<T> l2) {
			Set<T> list = new HashSet<>(l1)
			list.addAll(l2)
			return list
		}
	}

	/**
	 * This method is called from detector facade which handles all paths.
	 *
	 * @param path current file path
	 * @return smells of current analyzed file
	 */
	Set<T> execute(CompilationInfo info, Resolver resolver) {
		def visitor = getVisitor()
		visitor.initialize(info)
		visitor.visit(info, resolver)
		def newSmells = visitor.smells
		smells.addAll(newSmells)
		return newSmells
	}

	/**
	 * Attention!!! Only use this method if running this detector in single mode.
	 *
	 * Walks from the given base path down all directories and analyzes java source files.
	 *
	 * @param startPath start path of analyzing project
	 * @return set of smells
	 */
	Set<T> run(Path startPath) {
		def storage = JPAL.new(startPath)
		def resolver = new Resolver(storage)
		return storage.allCompilationInfo.stream().map {
			execute(it, resolver)
		}.collect(Collectors.reducing(new HashSet(), combine))
	}

	protected abstract Visitor getVisitor()

	void clear() {
		smells.clear()
	}

	@SuppressWarnings("GrMethodMayBeStatic")
	String getId() {
		return "NOT_SET"
	}

	Smell getType() {
		return Smell.UNKNOWN
	}

	List<T> getSmells() {
		return new ArrayList<T>(smells)
	}

	void setConfig(DetectorConfig config) {
		this.config = config
	}

	String valueOfConfigKey(String key) {
		Validate.isTrue(config != null, "No DetectorConfig set for this detector!")
		return config.getKey(getId())[key]
	}
}
