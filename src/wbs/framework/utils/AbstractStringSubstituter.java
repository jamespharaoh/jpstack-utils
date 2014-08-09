package wbs.framework.utils;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract
class AbstractStringSubstituter {

	private final static
	Pattern paramPattern =
		Pattern.compile ("\\{([-_\\p{Alnum}]+)\\}");

	protected abstract
	String getSubstitute (
			String name);

	public
	String substitute (
			String input) {

		Matcher matcher =
			paramPattern.matcher (input);

		StringBuffer stringBuffer =
			new StringBuffer ();

		while (matcher.find ()) {

			String substitute =
				getSubstitute (matcher.group (1));

			if (substitute == null) {

				throw new NullPointerException (
					stringFormat (
						"No substition defined for %s",
						matcher.group (1)));

			}

			matcher.appendReplacement (
				stringBuffer,
				substitute);

		}

		matcher.appendTail (
			stringBuffer);

		return stringBuffer.toString ();

	}

}
