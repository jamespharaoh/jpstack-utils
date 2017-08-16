package wbs.utils.collection;

public
interface ParallelIterable <Item>
	extends Iterable <Item> {

	@Override
	ParallelIterator <?, Item> iterator ();

}
