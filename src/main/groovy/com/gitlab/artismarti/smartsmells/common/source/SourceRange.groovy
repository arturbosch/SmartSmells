package com.gitlab.artismarti.smartsmells.common.source
/**
 * A source range is a tuple of source positions and represent a range inside a file.
 *
 * @author artur
 */
class SourceRange extends Tuple2<SourcePosition, SourcePosition> {

	SourceRange(SourcePosition first, SourcePosition second) {
		super(first, second)
	}

	static of(int startLine, int endLine, int startColumn, int endColumn) {
		return new SourceRange(new SourcePosition(startLine, startColumn), new SourcePosition(endLine, endColumn))
	}

	static of(SourcePosition begin, SourcePosition end) {
		return new SourceRange(begin, end)
	}


	@Override
	public String toString() {
		return "$first.first,$first.second,$second.first,$second.second";
	}
}
