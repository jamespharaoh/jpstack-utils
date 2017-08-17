package wbs.utils.string;

import static wbs.utils.etc.EnumUtils.enumNameHyphens;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.string.StringUtils.joinWithPipe;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringMatches;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import lombok.NonNull;

public
enum StringFormat {

	camelCase,
	className,
	hyphenated,
	integer,
	snakeCase,
	text;

	public
	boolean matches (
			@NonNull String value) {

		switch (this) {

		case camelCase:

			return stringMatches (
				camelCasePattern,
				value);

		case className:

			return stringMatches (
				classNamePattern,
				value);

		case hyphenated:

			return stringMatches (
				hyphenatedPattern,
				value);

		case integer:

			return stringMatches (
				integerPattern,
				value);

		case snakeCase:

			return stringMatches (
				snakeCasePattern,
				value);

		case text:

			return true;

		default:

			throw shouldNeverHappen ();

		}

	}

	public
	Predicate <String> matches () {
		return this::matches;
	}

	public
	void verifyAndThrow (
			@NonNull CharSequence valueCharSequence) {

		String valueString =
			valueCharSequence.toString ();

		if (
			! matches (
				valueString)
		) {

			throw new IllegalArgumentException (
				stringFormat (
					"String does not match format %s: %s",
					enumNameHyphens (
						this),
					valueString));

		}

	}

	public
	void verifyParameterAndThrow (
			@NonNull String parameterName,
			@NonNull CharSequence valueCharSequence) {

		String valueString =
			valueCharSequence.toString ();

		if (
			! matches (
				valueString)
		) {

			throw new IllegalArgumentException (
				stringFormat (
					"Parameter '%s' does not match format %s: %s",
					parameterName,
					enumNameHyphens (
						this),
					valueString));

		}

	}

	public
	Consumer <String> verifyAndThrow () {
		return this::verifyAndThrow;
	}

	public final static
	Pattern camelCasePattern =
		Pattern.compile (
			"([a-z]+|[0-9])([A-Z][a-z]*|[0-9])*");

	public final static
	Pattern classNamePattern =
		Pattern.compile (
			joinWithPipe (
				joinWithoutSeparator (
					"(",
					joinWithPipe (
						joinWithoutSeparator (
							"([a-z][a-z0-9]*\\.)*",
							"([A-Z][a-zA-Z0-9]*)",
							"(\\$[A-Z][a-zA-Z0-9]*)?"),
						"boolean",
						"byte",
						"char",
						"double",
						"float",
						"int",
						"long"),
					")",
					"(\\[\\])?"),
				"void"));

	public final static
	Pattern hyphenatedPattern =
		Pattern.compile (
			"[a-z0-9]+(-[a-z0-9]+)*");

	public final static
	Pattern integerPattern =
		Pattern.compile (
			"-?(0|[1-9][0-9]*)");

	public final static
	Pattern snakeCasePattern =
		Pattern.compile (
			"[a-z0-9]+(_[a-z0-9]+)*");

}
