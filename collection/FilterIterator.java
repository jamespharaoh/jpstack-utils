package wbs.utils.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import lombok.NonNull;

public
class FilterIterator <Item>
	implements Iterator <Item> {

	// state

	private final
	Iterator <? extends Item> inIterator;

	private final
	Predicate <? super Item> filter;

	private
	boolean haveItem = false;

	private
	Item item = null;

	// constructors

	public
	FilterIterator (
			@NonNull Iterator <? extends Item> inIterator,
			@NonNull Predicate <? super Item> filter) {

		this.inIterator =
			inIterator;

		this.filter =
			filter;

	}

	// public implementation

	@Override
	public
	boolean hasNext () {

		if (haveItem) {
			return true;
		}

		while (inIterator.hasNext ()) {

			Item item =
				inIterator.next ();

			if (
				filter.test (
					item)
			) {

				haveItem = true;

				this.item = item;

				return true;

			}

		}

		return false;

	}

	@Override
	public
	Item next () {

		if (! hasNext ()) {
			throw new NoSuchElementException ();
		}

		Item item =
			this.item;

		this.haveItem = false;

		return item;

	}

}
