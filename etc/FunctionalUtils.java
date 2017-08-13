package wbs.utils.etc;

import java.util.function.Function;

import lombok.NonNull;

public
class FunctionalUtils {

	public static <One, Two, Three>
	Function <One, Three> functionChain (
			@NonNull Function <One, Two> function0,
			@NonNull Function <? super Two, Three> function1) {

		return one ->
			function1.apply (
				function0.apply (
					one));

	}

}
