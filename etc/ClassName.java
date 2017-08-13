package wbs.utils.etc;

import static wbs.utils.string.StringUtils.stringIntern;

import lombok.NonNull;

import wbs.utils.string.StringFormat;

public
class ClassName
	implements CharSequence {

	private final
	String value;

	public
	ClassName (
			@NonNull CharSequence valueCharSequence) {

		String valueString =
			stringIntern (
				valueCharSequence);

		StringFormat.className.verifyAndThrow (
			valueString);

		this.value =
			valueString;

	}

	@Override
	public
	int length () {

		return value.length ();

	}

	@Override
	public
	char charAt (
			int index) {

		return value.charAt (
			index);

	}

	@Override
	public
	CharSequence subSequence (
			int start,
			int end) {

		return value.subSequence (
			start,
			end);

	}

	@Override
	public
	String toString () {

		return value;

	}

	@Override
	public
	int hashCode () {

		return value.hashCode ();

	}

}
