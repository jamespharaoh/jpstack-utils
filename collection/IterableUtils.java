package wbs.utils.collection;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.dynamicCast;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isInstanceOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;

import lombok.NonNull;

import wbs.utils.data.Pair;

public
class IterableUtils {

	public static
	long iterableCount (
			@NonNull Iterable <?> iterable) {

		long size = 0;

		for (
			@SuppressWarnings ("unused")
			Object _item
				: iterable
		) {
			size ++;
		}

		return size;

	}

	public static <In, Out>
	Iterable <Out> iterableMap (
			@NonNull Iterable <? extends In> iterable,
			@NonNull Function <? super In, ? extends Out> mapFunction) {

		return () ->
			new MapIterator <In, Out> (
				iterable.iterator (),
				mapFunction);

	}

	public static <InputType, OutputType>
	Iterable <OutputType> iterableFlatMap (
			@NonNull Iterable <? extends InputType> iterable,
			@NonNull Function <
				? super InputType,
				? extends Iterable <? extends OutputType>
			> mapFunction) {

		return () ->
			new FlatMapIterator <InputType, OutputType> (
				iterable.iterator (),
				in ->
					mapFunction.apply (
						in
					).iterator ());

	}

	public static <InputType, OutputType>
	List <OutputType> iterableFlatMapToList (
			@NonNull Iterable <? extends InputType> iterable,
			@NonNull Function <
				? super InputType,
				? extends Iterable <? extends OutputType>
			> mapFunction) {

		return ImmutableList.copyOf (
			new FlatMapIterator <InputType, OutputType> (
				iterable.iterator (),
				in ->
					mapFunction.apply (
						in
					).iterator ()));

	}

	public static <InLeft, InRight, Out>
	Iterable <Out> iterableMap (
			@NonNull Iterable <? extends Pair <
				? extends InLeft,
				? extends InRight
			>> iterable,
			@NonNull BiFunction <
				? super InLeft,
				? super InRight,
				? extends Out
			> mapFunction) {

		return () ->
			new MapIterator <Pair <
				? extends InLeft,
				? extends InRight>,
			Out> (
				iterable.iterator (),
				pair ->
					mapFunction.apply (
						pair.left (),
						pair.right ()));

	}

	public static <InputType, OutputType>
	List <OutputType> iterableMapToList (
			@NonNull Iterable <InputType> input,
			@NonNull Function <
				? super InputType,
				? extends OutputType
			> mapFunction) {

		return ImmutableList.copyOf (
			iterableMap (
				input,
				mapFunction));

	}

	public static <InputLeftType, InputRightType, OutputType>
	List <OutputType> iterableMapToList (
			@NonNull Iterable <Pair <InputLeftType, InputRightType>> input,
			@NonNull BiFunction <
				? super InputLeftType,
				? super InputRightType,
				? extends OutputType
			> mapFunction) {

		return ImmutableList.copyOf (
			iterableMap (
				input,
				mapFunction));

	}

	public static <InLeft, InRight, OutKey, OutValue>
	Map <OutKey, OutValue> iterableMapToMap (
			@NonNull Iterable <Pair <InLeft, InRight>> input,
			@NonNull BiFunction <
				? super InLeft,
				? super InRight,
				? extends OutKey
			> keyFunction,
			@NonNull BiFunction <
				? super InLeft,
				? super InRight,
				? extends OutValue
			> valueFunction) {

		ImmutableMap.Builder <OutKey, OutValue> builder =
			ImmutableMap.builder ();

		for (
			Pair <InLeft, InRight> item
				: input
		) {

			builder.put (
				keyFunction.apply (
					item.left (),
					item.right ()),
				valueFunction.apply (
					item.left (),
					item.right ()));

		}

		return builder.build ();

	}

	public static <InputType, OutputType>
	Set <OutputType> iterableMapToSet (
			@NonNull Iterable <InputType> input,
			@NonNull Function <
				? super InputType,
				? extends OutputType
			> mapFunction) {

		return ImmutableSet.copyOf (
			iterableMap (
				input,
				mapFunction));

	}

	public static <In, Out>
	Iterable <Out> iterableMapWithIndex (
			@NonNull Iterable <? extends In> iterable,
			@NonNull BiFunction <Long, ? super In, Out> mapFunction) {

		return () ->
			new Iterator <Out> () {

			Iterator <? extends In> iterator =
				iterable.iterator ();

			long index = 0;

			@Override
			public
			boolean hasNext () {
				return iterator.hasNext ();
			}

			@Override
			public
			Out next () {

				Out value =
					mapFunction.apply (
						index,
						iterator.next ());

				index ++;

				return value;

			}

		};

	}

	public static <In, OutKey, OutValue>
	Map <OutKey, OutValue> iterableMapWithIndexToMap (
			@NonNull Iterable <? extends In> iterable,
			@NonNull BiFunction <Long, ? super In, OutKey> keyFunction,
			@NonNull BiFunction <Long, ? super In, OutValue> valueFunction) {

		ImmutableMap.Builder <OutKey, OutValue> builder =
			ImmutableMap.builder ();

		long index = 0;

		for (
			In item
				: iterable
		) {

			builder.put (
				keyFunction.apply (
					index,
					item),
				valueFunction.apply (
					index,
					item));

			index ++;

		}

		return builder.build ();

	}

	public static <In, Out>
	List <Out> iterableMapWithIndexToList (
			@NonNull Iterable <? extends In> iterable,
			@NonNull BiFunction <Long, ? super In, Out> mapFunction) {

		ImmutableList.Builder <Out> builder =
			ImmutableList.builder ();

		long index = 0;

		for (
			In item
				: iterable
		) {

			builder.add (
				mapFunction.apply (
					index,
					item));

			index ++;

		}

		return builder.build ();

	}

	public static <Type>
	Set <Type> iterableToSet (
			@NonNull Iterable <Type> input) {

		return ImmutableSet.copyOf (
			input);

	}

	public static <ItemType>
	Iterable <ItemType> iterableFilter (
			@NonNull Iterable <ItemType> input,
			@NonNull Predicate <? super ItemType> predicate) {

		return () ->
			iterableStream (
				input)

			.filter (
				predicate)

			.iterator ();

	}

	public static <LeftType, RightType>
	Iterable <Pair <LeftType, RightType>> iterableFilter (
			@NonNull BiPredicate <
				? super LeftType,
				? super RightType
			> predicate,
			@NonNull Iterable <Pair <LeftType, RightType>> input) {

		return () ->
			iterableStream (
				input)

			.filter (
				pair ->
					predicate.test (
						pair.left (),
						pair.right ()))

			.iterator ();

	}

	public static <ItemType>
	List <ItemType> iterableFilterToList (
			@NonNull Iterable <ItemType> input,
			@NonNull Predicate <? super ItemType> predicate) {

		return iterableStream (input)

			.filter (
				predicate)

			.collect (
				Collectors.toList ());

	}

	public static <ItemType>
	Set <ItemType> iterableFilterToSet (
			@NonNull Iterable <ItemType> input,
			@NonNull Predicate <? super ItemType> predicate) {

		return iterableStream (input)

			.filter (
				predicate)

			.collect (
				Collectors.toSet ());

	}

	public static <InputType, OutputType>
	Iterable <OutputType> iterableFilterMap (
			@NonNull Iterable <? extends InputType> iterable,
			@NonNull Predicate <? super InputType> predicate,
			@NonNull Function <? super InputType, OutputType> mapping) {

		return () ->
			Streams.stream (
				iterable)

			.filter (
				predicate)

			.map (
				mapping::apply)

			.iterator ()

		;

	}

	public static <InLeft, InRight, Out>
	Iterable <Out> iterableFilterMap (
			@NonNull Iterable <Pair <? extends InLeft, ? extends InRight>> iterable,
			@NonNull BiPredicate <? super InLeft, ? super InRight> predicate,
			@NonNull BiFunction <? super InLeft, ? super InRight, Out> mapping) {

		return () ->
			Streams.stream (
				iterable)

			.filter (
				pair ->
					predicate.test (
						pair.left (),
						pair.right ()))

			.map (
				pair ->
					mapping.apply (
						pair.left (),
						pair.right ()))

			.iterator ()

		;

	}

	public static <InputType, OutputType>
	List <OutputType> iterableFilterMapToList (
			@NonNull Iterable <? extends InputType> iterable,
			@NonNull Predicate <? super InputType> predicate,
			@NonNull Function <? super InputType, OutputType> mapping) {

		return Streams.stream (
			iterable)

			.filter (
				predicate)

			.map (
				mapping::apply)

			.collect (
				Collectors.toList ())

		;

	}

	public static <InputType, OutputType>
	Set <OutputType> iterableFilterMapToSet (
			@NonNull Iterable <? extends InputType> iterable,
			@NonNull Predicate <? super InputType> predicate,
			@NonNull Function <? super InputType, OutputType> mapping) {

		return Streams.stream (
			iterable)

			.filter (
				predicate)

			.map (
				mapping::apply)

			.collect (
				Collectors.toSet ())

		;

	}

	public static <ItemType>
	Stream <ItemType> iterableStream (
			@NonNull Iterable <ItemType> iterable) {

		return StreamSupport.stream (
			iterable.spliterator (),
			false);

	}

	public static <ItemType>
	Optional <ItemType> iterableFindFirst (
			@NonNull Iterable <ItemType> iterable,
			@NonNull Predicate <ItemType> predicate) {

		for (
			ItemType item
				: iterable
		) {

			if (
				predicate.test (
					item)
			) {

				return optionalOf (
					item);

			}

		}

		return optionalAbsent ();

	}

	public static <ItemType>
	ItemType iterableFindExactlyOneRequired (
			@NonNull Iterable <ItemType> iterable,
			@NonNull Predicate <? super ItemType> predicate) {

		Optional <ItemType> value =
			optionalAbsent ();

		for (
			ItemType item
				: iterable
		) {

			if (
				! predicate.test (
					item)
			) {
				continue;
			}

			if (
				optionalIsPresent (
					value)
			) {
				throw new IllegalArgumentException (
					"Multiple matching elements");
			}

			value =
				optionalOf (
					item);

		}

		if (
			optionalIsNotPresent (
				value)
		) {
			throw new IllegalArgumentException (
				"No matching element found");
		}

		return optionalGetRequired (
			value);

	}

	public static <ItemType>
	ItemType iterableFindExactlyOneRequired (
			@NonNull Iterable <?> iterable,
			@NonNull Class <ItemType> itemClass) {

		return genericCastUnchecked (
			iterableFindExactlyOneRequired (
				iterable,
				isInstanceOf (
					itemClass)));

	}

	public static <ItemType>
	Optional <ItemType> iterableOnlyItem (
			@NonNull Iterable <ItemType> iterable) {

		Iterator <ItemType> iterator =
			iterable.iterator ();

		if (! iterator.hasNext ()) {
			return optionalAbsent ();
		}

		ItemType item =
			iterator.next ();

		if (iterator.hasNext ()) {
			throw new IllegalArgumentException ();
		}

		return optionalOf (
			item);

	}

	public static <InType, OutType extends InType>
	Optional <OutType> iterableOnlyItemByClass (
			@NonNull Iterable <InType> iterable,
			@NonNull Class <OutType> targetClass) {

		OutType foundItem =
			null;

		for (
			InType inItem
				: iterable
		) {

			Optional <OutType> outItemOptional =
				dynamicCast (
					targetClass,
					inItem);

			if (
				optionalIsNotPresent (
					outItemOptional)
			) {
				continue;
			}

			if (foundItem != null) {

				throw new IllegalArgumentException (
					"Multiple items matched");

			}

			foundItem =
				optionalGetRequired (
					outItemOptional);

		}

		return optionalFromNullable (
			foundItem);

	}

	public static <ItemType>
	ItemType iterableOnlyItemRequired (
			@NonNull Iterable <ItemType> iterable) {

		Iterator <ItemType> iterator =
			iterable.iterator ();

		if (! iterator.hasNext ()) {
			throw new IllegalArgumentException ();
		}

		ItemType item =
			iterator.next ();

		if (iterator.hasNext ()) {
			throw new IllegalArgumentException ();
		}

		return item;

	}

	public static <InType, OutType extends InType>
	Iterable <OutType> iterableFilterByClass (
			@NonNull Iterable <InType> iterable,
			@NonNull Class <OutType> targetClass) {

		return () ->
			genericCastUnchecked (
				new FilterIterator <InType> (
					iterable.iterator (),
					isInstanceOf (
						targetClass)));

	}

	public static <Item>
	Iterable <Item> iterableChain (
			@NonNull Iterable <? extends Iterable <? extends Item>>
				inIterables) {

		return () ->
			new FlatMapIterator <Iterable <? extends Item>, Item> (
				inIterables.iterator (),
				Iterable::iterator);

	}

	@SafeVarargs
	public static <Item>
	Iterable <Item> iterableChainArguments (
			@NonNull Iterable <? extends Item> ... iterables) {

		return iterableChain (
			Arrays.asList (
				iterables));

	}

	public static <Type>
	Iterable <Type> iterableChainArguments () {

		return emptyList ();

	}

	public static <Type>
	Iterable <Type> iterableChainArguments (
			@NonNull Iterable <Type> iterable0) {

		return iterable0;

	}

	public static <Item>
	List <Item> iterableChainToList (
			@NonNull Iterable <? extends Iterable <? extends Item>>
				inIterables) {

		return ImmutableList.copyOf (
			iterableChain (
				inIterables));

	}

	public static
	long iterableSize (
			@NonNull Iterable <?> iterable) {

		long size = 0;

		Iterator <?> iterator =
			iterable.iterator ();

		while (iterator.hasNext ()) {

			size ++;

			iterator.next ();

		}

		return size;

	}

	public static
	boolean iterableIsEmpty (
			@NonNull Iterable <?> iterable) {

		return ! iterable.iterator ().hasNext ();

	}

	public static
	boolean iterableIsNotEmpty (
			@NonNull Iterable <?> iterable) {

		return iterable.iterator ().hasNext ();

	}

	public static <Item>
	List <Item> iterableOrderToList (
			@NonNull Iterable <? extends Item> iterable,
			@NonNull Comparator <? super Item> comparator) {

		List <Item> list =
			new ArrayList<> ();

		for (
			Item item
				: iterable
		) {

			list.add (
				item);

		}

		Collections.sort (
			list,
			comparator);

		return list;

	}

	public static <Left, Right>
	Iterable <Pair <Left, Right>> iterableZipRequired (
			@NonNull Iterable <? extends Left> leftIterable,
			@NonNull Iterable <? extends Right> rightIterable) {

		return () -> {

			Iterator <? extends Left> leftIterator =
				leftIterable.iterator ();

			Iterator <? extends Right> rightIterator =
				rightIterable.iterator ();

			return new Iterator <Pair <Left, Right>> () {

				@Override
				public
				boolean hasNext () {

					boolean leftHasNext =
						leftIterator.hasNext ();

					boolean rightHasNext =
						rightIterator.hasNext ();

					if (leftHasNext && rightHasNext) {
						return true;
					}

					if (! leftHasNext && ! rightHasNext) {
						return false;
					}

					throw new RuntimeException ();

				}

				@Override
				public
				Pair <Left, Right> next () {

					return Pair.of (
						leftIterator.next (),
						rightIterator.next ());

				}

			};

		};

	}

	public static <Item>
	void iterableForEach (
			@NonNull Iterable <Item> iterable,
			@NonNull Consumer <Item> function) {

		for (
			Item item
				: iterable
		) {

			function.accept (
				item);

		}

	}

	public static <Left, Right>
	void iterableForEach (
			@NonNull Iterable <Pair <Left, Right>> iterable,
			@NonNull BiConsumer <Left, Right> function) {

		for (
			Pair <Left, Right> item
				: iterable
		) {

			function.accept (
				item.left (),
				item.right ());

		}

	}

	public static <In, Out>
	ParallelIterable <Out> iterableMapParallel (
			@NonNull Long threads,
			@NonNull Long maxTasks,
			@NonNull Iterable <In> inItems,
			@NonNull Function <In, Out> mapFunction) {

		return () ->
			new ParallelIterator <In, Out> (
				threads,
				maxTasks,
				inItems.iterator (),
				mapFunction);

	}

	public static <Item>
	void iterableForEachParallel (
			@NonNull Long threads,
			@NonNull Long maxTasks,
			@NonNull Iterable <Item> inItems,
			@NonNull Consumer <Item> function) {

		try (

			ParallelIterator <Item, Object> iterator =
				new ParallelIterator <Item, Object> (
					threads,
					maxTasks,
					inItems.iterator (),
					item -> {

				function.accept (
					item);

				return new Object ();

			});

		) {

			while (iterator.hasNext ()) {
				iterator.next ();
			}

		}

	}

}
