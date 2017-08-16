package wbs.utils.string;

import java.util.Iterator;
import java.util.NoSuchElementException;

import lombok.NonNull;

public
class CodePointIterator
	implements Iterator <Integer> {

	private final
	String source;

	private final
	int end;

	private
	int position;

	public
	CodePointIterator (
			@NonNull String source,
			int start,
			int end) {

		if (start < 0) {

			throw new IllegalArgumentException (
				"Start is negative");

		}

		if (end < start) {

			throw new IllegalArgumentException (
				"End is before start");

		}

		if (end > source.length ()) {

			throw new IllegalArgumentException (
				"End is greater than string length");

		}

		this.source = source;
		this.position = start;
		this.end = end;

	}

	public
	CodePointIterator (
			String source) {

		this (
			source,
			0,
			source.length ());

	}

	@Override
	public
	boolean hasNext () {

		return position < end;

	}

	@Override
	public
	Integer next () {

		return nextCodePoint ();

	}

	public
	int nextCodePoint () {

		if (position == end) {
			throw new NoSuchElementException ();
		}

		int codePoint =
			source.codePointAt (
				position);

		if (codePoint > Character.MAX_VALUE) {
			position += 2;
		} else {
			position += 1;
		}

		return codePoint;

	}

}
