package io.gitlab.arturbosch.smartsmells.smells.featureenvy

import groovy.transform.Immutable
import groovy.transform.ToString
import io.gitlab.arturbosch.jpal.ast.source.SourcePath
import io.gitlab.arturbosch.jpal.ast.source.SourceRange
import io.gitlab.arturbosch.smartsmells.common.Smelly

/**
 * @author artur
 */
@Immutable
@ToString(includeNames = false, includePackage = false)
class FeatureEnvy implements Smelly {

	String name
	String signature
	String inClass

	String objectName
	String objectSignature
	String objectType

	double factor
	double factorThreshold

	@Delegate
	SourcePath sourcePath
	@Delegate
	SourceRange sourceRange

	@Override
	String asCompactString() {
		"FeatureEnvy \n\nMethod $name is jealousy of $objectName: $objectType" +
				"\nFactor: $factor with threshold: $factorThreshold"
	}

	boolean equals(o) {
		if (this.is(o)) return true
		if (!(o instanceof FeatureEnvy)) return false

		FeatureEnvy that = (FeatureEnvy) o

		if (inClass != that.inClass) return false
		if (name != that.name) return false
		if (objectName != that.objectName) return false
		if (objectSignature != that.objectSignature) return false
		if (objectType != that.objectType) return false
		if (signature != that.signature) return false
		if (sourcePath != that.sourcePath) return false

		return true
	}

	int hashCode() {
		int result
		result = (name != null ? name.hashCode() : 0)
		result = 31 * result + (signature != null ? signature.hashCode() : 0)
		result = 31 * result + (inClass != null ? inClass.hashCode() : 0)
		result = 31 * result + (objectName != null ? objectName.hashCode() : 0)
		result = 31 * result + (objectSignature != null ? objectSignature.hashCode() : 0)
		result = 31 * result + (objectType != null ? objectType.hashCode() : 0)
		result = 31 * result + (sourcePath != null ? sourcePath.hashCode() : 0)
		return result
	}
}
