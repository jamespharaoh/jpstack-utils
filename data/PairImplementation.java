package wbs.utils.data;

import lombok.NonNull;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@DataClass ("pair")
public final
class PairImplementation <Left, Right>
	implements Pair <Left, Right> {

	// state

	@DataAttribute
	private final
	Left left;

	@DataAttribute
	private final
	Right right;

	// constructors;

	public
	PairImplementation (
			@NonNull Left left,
			@NonNull Right right) {

		this.left =
			left;

		this.right =
			right;

	}

	// accessors

	@Override
	public
	Left left () {
		return left;
	}

	@Override
	public
	Right right () {
		return right;
	}

	// map entry implementations

	@Override
	public
	Left getKey () {
		return left;
	}

	@Override
	public
	Right getValue () {
		return right;
	}

	@Override
	public
	Right setValue (
			@NonNull Right value) {

		throw new UnsupportedOperationException (
			"Pair.setValue (...)");

	}

}
