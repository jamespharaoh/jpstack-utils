package wbs.utils.etc;

import static wbs.utils.etc.Misc.isNull;

import java.util.function.Supplier;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

public
class NullUtils {

	@SafeVarargs
	public static <Type>
	Type ifNull (
			@NonNull Type... values) {

		for (Type value : values) {

			if (value != null)
				return value;

		}

		return null;

	}

	@SafeVarargs
	public static <Type>
	Type ifNull (
			@NonNull Supplier <Type> ... valueSuppliers) {

		for (
			Supplier <Type> valueSupplier
				: valueSuppliers
		) {

			Type value =
				valueSupplier.get ();

			if (value != null)
				return value;

		}

		return null;

	}

	public static <Type>
	Type ifNull (
			Type input,
			Type ifNull) {

		return input == null
			? ifNull
			: input;

	}

	@SafeVarargs
	public static <Type>
	Type ifNullThenRequired (
			@NonNull Supplier <? extends Type> ... valueSuppliers) {

		for (
			Supplier <? extends Type> valueSupplier
				: valueSuppliers
		) {

			Type value =
				valueSupplier.get ();

			if (value != null) {
				return value;
			}

		}

		throw new NullPointerException ();

	}

	public static
	<Type> Type nullIf (
			Type input,
			Type nullIf) {

		if (input == null)
			return null;

		if (nullIf != null && input.equals (nullIf))
			return null;

		return input;

	}

	public static
	void errorIfNull (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String name,
			Object value) {

		if (
			isNull (
				value)
		) {

			parentTaskLogger.errorFormat (
				"Parameter %s is null",
				name);

		}

	}

}
