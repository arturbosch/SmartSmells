package io.gitlab.arturbosch.smartsmells.smells.messagechain

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.MethodCallExpr
import io.gitlab.arturbosch.jpal.ast.source.SourcePath
import io.gitlab.arturbosch.jpal.ast.source.SourceRange
import io.gitlab.arturbosch.jpal.internal.Printer
import io.gitlab.arturbosch.jpal.resolution.Resolver
import io.gitlab.arturbosch.jpal.resolution.symbols.WithPreviousSymbolReference
import io.gitlab.arturbosch.smartsmells.common.Visitor
import io.gitlab.arturbosch.smartsmells.smells.ElementTarget

import java.util.stream.Collectors

/**
 * @author Artur Bosch
 */
class MessageChainVisitor extends Visitor<MessageChain> {

	private int chainSizeThreshold

	private Map<String, MethodCallExpr> methodCallExprMap = new HashMap<>()

	MessageChainVisitor(int chainSizeThreshold) {
		this.chainSizeThreshold = chainSizeThreshold
	}

	@Override
	void visit(CompilationUnit n, Resolver resolver) {
		super.visit(n, resolver)

		methodCallExprMap.entrySet().stream()
				.filter { isValidChainAndNoBuilderPattern(it.value, resolver) }
				.collect { Map.Entry<String, MethodCallExpr> it ->

			def signature = it.value.toString(Printer.NO_COMMENTS)
			new MessageChain(signature, extractSourceString(signature),
					it.value.nameAsString, countOccurrences(it.key, "."), chainSizeThreshold,
					SourcePath.of(info), SourceRange.fromNode(it.value), ElementTarget.LOCAL
			)
		}.each { smells.add(it) }
	}

	private boolean isValidChainAndNoBuilderPattern(MethodCallExpr call, Resolver resolver) {
		def reference = resolver.resolve(call.name, info).orElse(null)
		return reference && reference instanceof WithPreviousSymbolReference &&
				!(reference as WithPreviousSymbolReference).isBuilderPattern()
	}

	static String extractSourceString(String it) {
		it.split("\\.")[0].replace("(", "").replace(")", "")
	}

	@Override
	void visit(MethodCallExpr n, Resolver resolver) {
		def linkedExpr = extractExpressionNames(n)
		def expr = filterCollectionGets(linkedExpr)
		def count = countOccurrences(expr, ".")

		boolean found = false
		if (count >= chainSizeThreshold) {
			methodCallExprMap.keySet().each {
				if (it.contains(expr)) {
					found = true
				}
			}
			if (!found) {
				methodCallExprMap.put(expr, n)
			}
		}
		super.visit(n, resolver)
	}

	private static String filterCollectionGets(String expressions) {
		Arrays.stream(expressions.split("\\."))
				.filter { it != "get" }
				.collect(Collectors.joining("."))
	}

	def extractExpressionNames(MethodCallExpr n) {
		if (!n.scope.isPresent() || !(n.scope.get() instanceof MethodCallExpr)) return n.nameAsString
		return extractExpressionNames((MethodCallExpr) n.scope.get()) + "." + n.nameAsString
	}

	static countOccurrences(String source, String pattern) {
		if (source.isEmpty() || pattern.isEmpty()) {
			return 0
		}

		int count = 0
		for (int pos = 0; (pos = source.indexOf(pattern, pos)) != -1; count++) {
			pos += pattern.length()
		}

		return count + 1
	}
}
