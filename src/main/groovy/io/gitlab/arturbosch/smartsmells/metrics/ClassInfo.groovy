package io.gitlab.arturbosch.smartsmells.metrics

import groovy.transform.Immutable
import groovy.transform.ToString
import io.gitlab.arturbosch.smartsmells.common.Smelly
import io.gitlab.arturbosch.smartsmells.common.source.SourcePath
import io.gitlab.arturbosch.smartsmells.common.source.SourceRange

/**
 * @author artur
 */
@Immutable
@ToString(includePackage = false, includeNames = true)
class ClassInfo implements Smelly {
	String name
	int wmc
	double tcc
	int atfd
	int noa
	int nom
	int loc
	int sloc
	double mlm
	double plm
	double mld
	double pld
	int cc
	int cm
	String signature
	@Delegate
	SourcePath sourcePath
	@Delegate
	SourceRange sourceRange

	@Override
	String asCompactString() {
		"ClassInfo - not supported yet"
	}
}
