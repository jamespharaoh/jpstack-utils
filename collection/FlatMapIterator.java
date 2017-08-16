package wbs.utils.collection;

import java.util.Iterator;
import java.util.function.Function;

import lombok.NonNull;

public
class FlatMapIterator <In, Out>
	implements Iterator <Out> {

	// state

	private final
	Iterator <? extends In> inIterator;

	private final
	Function <? super In, ? extends Iterator <? extends Out>> mapFunction;

	private
	Iterator <? extends Out> outIterator;

	// constructors

	public
	FlatMapIterator (
			@NonNull Iterator <? extends In> inIterator,
			@NonNull Function <? super In, ? extends Iterator <? extends Out>>
				mapFunction) {

		this.inIterator =
			inIterator;

		this.mapFunction =
			mapFunction;

	}

	// public implementation

	@Override
	public
	boolean hasNext () {

		setOutIterator ();

		return outIterator != null;

	}

	@Override
	public
	Out next () {

		return outIterator.next ();

	}

	// private implementation

	private
	void setOutIterator () {

		if (outIterator != null && outIterator.hasNext ()) {
			return;
		}

		outIterator = null;

		while (inIterator.hasNext ()) {

			outIterator =
				mapFunction.apply (
					inIterator.next ());

			if (outIterator.hasNext ()) {
				return;
			}

		}

		outIterator = null;

	}

}
