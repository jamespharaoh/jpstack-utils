package wbs.utils.collection;

import static wbs.utils.etc.NullUtils.isNull;

import java.util.Iterator;
import java.util.function.Function;

import lombok.NonNull;

public
class MapIterator <In, Out>
	implements Iterator <Out> {

	// state

	private final
	Iterator <? extends In> inIterator;

	private final
	Function <? super In, ? extends Out> mapFunction;

	// constructors

	public
	MapIterator (
			@NonNull Iterator <? extends In> inIterator,
			@NonNull Function <? super In, ? extends Out> mapFunction) {

		this.inIterator =
			inIterator;

		this.mapFunction =
			mapFunction;

	}

	// public implementation

	@Override
	public
	boolean hasNext () {

		return inIterator.hasNext ();

	}

	@Override
	public
	Out next () {

		In inItem =
			inIterator.next ();

		if (
			isNull (
				inItem)
		) {

			throw new NullPointerException (
				"Source iterator returned null item");

		}

		return mapFunction.apply (
			inItem);

	}

}
