package wbs.utils.exception;

import lombok.NonNull;

public
class RuntimeClassNotFoundException
	extends RuntimeReflectiveOperationException {

	public
	RuntimeClassNotFoundException (
			@NonNull ReflectiveOperationException cause) {

		super (
			cause);

	}

}
