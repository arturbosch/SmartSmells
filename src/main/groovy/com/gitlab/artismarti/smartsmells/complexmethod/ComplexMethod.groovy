package com.gitlab.artismarti.smartsmells.complexmethod

import com.gitlab.artismarti.smartsmells.longmethod.LongMethod
import groovy.transform.Immutable
import groovy.transform.ToString
/**
 * @author artur
 */
@Immutable
@ToString(includePackage = false)
class ComplexMethod {

	@Delegate
	LongMethod longMethod

	int cyclomaticComplexity
}
