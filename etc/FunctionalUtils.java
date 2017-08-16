package wbs.utils.etc;

import java.util.function.Function;
import java.util.function.Predicate;

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

	public static <One, Two>
	Predicate <One> functionChain (
			@NonNull Function <One, Two> function0,
			@NonNull Predicate <? super Two> function1) {

		return one ->
			function1.test (
				function0.apply (
					one));

	}

}
