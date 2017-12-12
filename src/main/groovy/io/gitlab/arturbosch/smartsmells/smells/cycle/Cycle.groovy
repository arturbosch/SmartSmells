package io.gitlab.arturbosch.smartsmells.smells.cycle

import com.github.javaparser.ast.body.FieldDeclaration
import groovy.transform.CompileStatic
import groovy.transform.ToString
import io.gitlab.arturbosch.smartsmells.smells.CycleSpecific
import io.gitlab.arturbosch.smartsmells.smells.DetectionResult
import io.gitlab.arturbosch.smartsmells.smells.ElementTarget

/**
 * @author artur
 */
@ToString(includeNames = false, includePackage = false)
@CompileStatic
class Cycle implements DetectionResult, CycleSpecific {

	Dependency source
	Dependency target

	ElementTarget elementTarget = ElementTarget.TWO_CLASSES

	@Override
	ElementTarget elementTarget() {
		return elementTarget
	}

	Cycle(Dependency source, Dependency target) {
		this.source = source
		this.target = target
	}

	boolean comparePath(String path) {
		return source.path == path || target.path == path
	}

	@Override
	String asCompactString() {
		"Cycle \n\nSource: $source.name\nTarget: $target.name"
	}

	@Override
	String asComparableString() {
		return "${javaClassName()}$source.signature#$target.signature"
	}

	@Override
	boolean equals(Object obj) {
		if (obj instanceof Cycle) {
			return (source == obj.source && target == obj.target) ||
					(source == obj.target && target == obj.source)
		}
		return false
	}

	@Override
	String toString() {
		return "Cycle{" +
				"source=" + source.toString() +
				", target=" + target.toString() +
				'}'
	}

	@Override
	int hashCode() {
		return Objects.hash(source) + Objects.hash(target)
	}

	@Override
	String name() {
		return source.name
	}

	@Override
	String signature() {
		return source.signature
	}

	@Override
	String secondName() {
		return target.name
	}

	@Override
	String secondSignature() {
		return target.signature
	}

	@Override
	CycleSpecific copyOnSecond(FieldDeclaration field) {
		def newTarget = source.copy(field) as Dependency
		return new Cycle(source, newTarget)
	}

	@Override
	CycleSpecific copy(FieldDeclaration field) {
		def newSource = source.copy(field) as Dependency
		return new Cycle(newSource, target)
	}
}
