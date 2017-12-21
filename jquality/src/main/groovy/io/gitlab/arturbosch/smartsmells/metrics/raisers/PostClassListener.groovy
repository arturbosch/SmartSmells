package io.gitlab.arturbosch.smartsmells.metrics.raisers

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import groovy.transform.CompileStatic
import io.gitlab.arturbosch.jpal.ast.MethodHelper
import io.gitlab.arturbosch.jpal.ast.TypeHelper
import io.gitlab.arturbosch.jpal.core.CompilationInfo
import io.gitlab.arturbosch.jpal.resolution.QualifiedType
import io.gitlab.arturbosch.jpal.resolution.Resolver
import io.gitlab.arturbosch.smartsmells.metrics.ClassInfo
import io.gitlab.arturbosch.smartsmells.metrics.FileInfo
import io.gitlab.arturbosch.smartsmells.metrics.MethodInfo
import io.gitlab.arturbosch.smartsmells.metrics.Metric

import java.util.stream.Collectors

/**
 * @author Artur Bosch
 */
@CompileStatic
trait PostClassListener {

	int priority() {
		return 0
	}

	abstract void raise(ClassOrInterfaceDeclaration aClass, CompilationInfo info, Resolver resolver)

	ClassInfo findClassInfo(ClassOrInterfaceDeclaration aClass, CompilationInfo info) {
		return info.getData(FileInfo.KEY).findClassByName(aClass.nameAsString)
	}
}

@CompileStatic
class AMW implements PostClassListener {

	static final String AVERAGE_METHOD_WEIGHT = "AverageMethodWeight"

	@Override
	int priority() {
		return 1
	}

	@Override
	void raise(ClassOrInterfaceDeclaration aClass, CompilationInfo info, Resolver resolver) {
		def classInfo = findClassInfo(aClass, info)
		int wmc = classInfo.getMetric(WMC.WEIGHTED_METHOD_COUNT)?.value ?: 0
		int nom = classInfo.getMetric(NOM.NUMBER_OF_METHODS)?.value ?: 1
		Metric metric = Metric.of(AVERAGE_METHOD_WEIGHT, (double) wmc / nom)
		classInfo.addMetric(metric)
	}
}

@CompileStatic
class WMC implements PostClassListener {

	static final String WEIGHTED_METHOD_COUNT = "WeightedMethodCount"

	@Override
	void raise(ClassOrInterfaceDeclaration aClass, CompilationInfo info, Resolver resolver) {
		def classInfo = findClassInfo(aClass, info)
		List<Metric> metrics = classInfo.methods.stream()
				.map { it.getMetric(CYCLO.CYCLOMATIC_COMPLEXITY) }
				.filter { it != null }
				.collect(Collectors.toList())
		def wmc = metrics.stream().mapToInt { it.value }.reduce(0) { int first, int second -> first + second }
		classInfo.addMetric(Metric.of(WEIGHTED_METHOD_COUNT, wmc))
	}
}

@CompileStatic
class NAS implements PostClassListener {

	static final String NUMBER_OF_ADDED_SERVICES = "NumberofAddedServices"
	static final String PERCENTAGE_OF_NEWLY_ADDED_SERVICES = "PercentageofNewlyAddedServices"

	@Override
	void raise(ClassOrInterfaceDeclaration aClass, CompilationInfo info, Resolver resolver) {
		def classInfo = findClassInfo(aClass, info)
		if (classInfo) {
			List<MethodInfo> ancestorMethods = TypeHelper.findAllAncestors(aClass, resolver).stream()
					.flatMap { QualifiedType ancestorType -> resolveAncestorMethods(resolver, ancestorType).stream() }
					.collect(Collectors.toList())
			Set<MethodInfo> methodInfos = classInfo.methods
			Metric pnas = raisePNAS(ancestorMethods, methodInfos)
			Metric nas = raiseNAS(ancestorMethods, methodInfos)
			classInfo.addMetric(nas)
			classInfo.addMetric(pnas)
		}
	}

	private static Metric raisePNAS(Collection<MethodInfo> ancestorMethods, Set<MethodInfo> methodInfos) {
		List<String> publicAncestorServices = ancestorMethods.stream()
				.filter { it.declaration instanceof MethodDeclaration }
				.filter { it.declaration.isPublic() && !it.declaration.isStatic() }
				.filter { !MethodHelper.isGetterOrSetter(it.declaration as MethodDeclaration) }
				.map { it.declarationString }
				.collect(Collectors.toList())
		List<String> publicServices = methodInfos.stream()
				.filter { it.declaration instanceof MethodDeclaration }
				.filter { it.declaration.isPublic() && !it.declaration.isStatic() }
				.filter { !MethodHelper.isGetterOrSetter(it.declaration as MethodDeclaration) }
				.map { it.declarationString }
				.collect(Collectors.toList())

		int publicServiceSize = publicServices.findAll { !(it in publicAncestorServices) }.size()
		int totalPublicServices = (publicServices + publicAncestorServices).toSet().size()
		def value = totalPublicServices == 0 ? 0.0d : (double) publicServiceSize / totalPublicServices

		Metric.of(PERCENTAGE_OF_NEWLY_ADDED_SERVICES, value)
	}

	private static Metric raiseNAS(Collection<MethodInfo> ancestorMethods, Set<MethodInfo> methodInfos) {
		Set<String> ancestorNames = ancestorMethods.stream()
				.filter { it.declaration instanceof MethodDeclaration }
				.filter { it.declaration.isPublic() && !it.declaration.isStatic() }
				.filter { !MethodHelper.isGetterOrSetter(it.declaration as MethodDeclaration) }
				.map { it.declarationString }
				.collect(Collectors.toSet())
		int addedServices = methodInfos.stream()
				.filter { it.declaration instanceof MethodDeclaration }
				.filter { it.declaration.isPublic() && !it.declaration.isStatic() }
				.filter { !MethodHelper.isGetterOrSetter(it.declaration as MethodDeclaration) }
				.filter { !(it.declarationString in ancestorNames) }
				.filter { !it.declaration.getAnnotationByName("Override").isPresent() }
				.count() as int
		Metric.of(NUMBER_OF_ADDED_SERVICES, addedServices)
	}

	private Set<MethodInfo> resolveAncestorMethods(Resolver resolver, QualifiedType ancestorType) {
		resolver.find(ancestorType)
				.map { CompilationInfo ancestorInfo -> extractMethodInfos(ancestorInfo, ancestorType) }
				.orElse(Collections.emptySet() as Set<MethodInfo>)
	}

	private Set<MethodInfo> extractMethodInfos(CompilationInfo ancestorInfo, QualifiedType ancestorType) {
		ancestorInfo.getTypeDeclarationByQualifier(ancestorType)
				.filter { it instanceof ClassOrInterfaceDeclaration }
				.map { findClassInfo(it as ClassOrInterfaceDeclaration, ancestorInfo) }
				.map { it.methods }
				.orElse(Collections.emptySet() as Set<MethodInfo>)
	}
}
