package wbs.utils.etc;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import lombok.NonNull;

public
class JpStringComparator
	implements Comparator <String> {

	@Override
	public
	int compare (
			@NonNull String left,
			@NonNull String right) {

		Iterator <StringSlice> leftIterator =
			split (
				left);

		Iterator <StringSlice> rightIterator =
			split (
				right);

		for (;;) {

			boolean leftHasNext =
				leftIterator.hasNext ();

			boolean rightHasNext =
				rightIterator.hasNext ();

			if (leftHasNext && rightHasNext) {

				StringSlice leftSlice =
					leftIterator.next ();

				StringSlice rightSlice =
					rightIterator.next ();

				int result =
					leftSlice.compareTo (
						rightSlice);

				if (result != 0) {
					return result;
				}

			} else if (leftHasNext) {

				return 9;

			} else if (rightHasNext) {

				return -9;

			} else {

				return 0;

			}

		}

	}

	private
	Iterator <StringSlice> split (
			@NonNull String input) {

		return new Iterator <StringSlice> () {

			int position = 0;

			@Override
			public
			boolean hasNext () {
				return position < input.length ();
			}

			@Override
			public
			StringSlice next () {

				if (position == input.length ()) {
					throw new NoSuchElementException ();
				}

				int start =
					position;

				int firstChar =
					nextChar ();

				if (
					Character.isAlphabetic (
						firstChar)
				) {

					// alphabetic

					while (position < input.length ()) {

						int currencChar =
							currentChar ();

						if (
							! Character.isLowerCase (
								currencChar)
						) {
							break;
						}

						skipChar (
							currencChar);

					}

					return new StringSlice (
						input,
						start,
						position);

				} else {

					// non-alphabetic

					while (position < input.length ()) {

						int currencChar =
							currentChar ();

						if (
							Character.isAlphabetic (
								currencChar)
						) {
							break;
						}

						skipChar (
							currencChar);

					}

					return new StringSlice (
						input,
						start,
						position);

				}

			}

			private
			int currentChar () {

				return input.codePointAt (
					position);

			}

			private
			int nextChar () {

				int nextChar =
					input.codePointAt (
						position);

				skipChar (
					nextChar);

				return nextChar;

			}

			private
			void skipChar (
					int currentChar) {

				if (currentChar > Character.MAX_VALUE) {
					position += 2;
				} else {
					position += 1;
				}

			}

		};

	}

	private
	class StringSlice
		implements
			CharSequence,
			Comparable <StringSlice> {

		private final
		String string;

		private final
		int start;

		private final
		int end;

		private
		StringSlice (
				String string,
				int start,
				int end) {

			this.string = string;
			this.start = start;
			this.end = end;

		}

		@Override
		public
		int compareTo (
				StringSlice other) {

			// compare first character

			int thisChar =
				this.string.codePointAt (
					this.start);

			int otherChar =
				other.string.codePointAt (
					other.start);

			// non-alphabetic before alphabetic

			boolean thisIsAlphabetic =
				Character.isAlphabetic (
					thisChar);

			boolean otherIsAlphabetic =
				Character.isAlphabetic (
					otherChar);

			if (! thisIsAlphabetic && otherIsAlphabetic) {
				return -99;
			}

			if (thisIsAlphabetic && ! otherIsAlphabetic) {
				return 99;
			}

			// capitalised before lowercase

			boolean thisIsCapitalised =
				Character.isUpperCase (
					thisChar);

			boolean otherIsCapitalised =
				Character.isUpperCase (
					otherChar);

			if (thisIsCapitalised && ! otherIsCapitalised) {
				return -999;
			}

			if (! thisIsCapitalised && otherIsCapitalised) {
				return 999;
			}

			// simple compare

			int thisPosition = this.start;
			int otherPosition = other.start;

			for (;;) {

				if (thisChar >= Character.MAX_VALUE) {
					thisPosition += 2;
				} else {
					thisPosition += 1;
				}

				if (otherChar >= Character.MAX_VALUE) {
					thisPosition += 2;
				} else {
					otherPosition += 1;
				}

				if (
					thisPosition == this.end
					|| otherPosition == other.end
				) {
					break;
				}

				thisChar =
					this.string.codePointAt (
						thisPosition);

				otherChar =
					other.string.codePointAt (
						otherPosition);

				if (thisChar != otherChar) {
					return otherChar - thisChar;
				}

			}

			if (thisPosition != this.end) {
				return 9999;
			}

			if (otherPosition != other.end) {
				return -9999;
			}

			return 0;

		}

		@Override
		public
		String toString () {

			return string.substring (
				start,
				end);

		}

		@Override
		public
		int length () {
			return end - start;
		}

		@Override
		public
		char charAt (
				int index) {

			if (start + index >= end) {
				throw new IndexOutOfBoundsException ();
			}

			return string.charAt (
				start + index);

		}

		@Override
		public
		CharSequence subSequence (
				int start,
				int end) {

			if (end < start) {
				throw new IllegalArgumentException ();
			}

			if (this.start + end >= this.end) {
				throw new IndexOutOfBoundsException ();
			}

			return new StringSlice (
				this.string,
				this.start + start,
				this.start + end);

		}

	}

	public final static
	JpStringComparator instance =
		new JpStringComparator ();

}
