package wbs.utils.etc;

import static wbs.utils.collection.IterableUtils.iterableOrderToList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith (Parameterized.class)
public
class JpStringComparatorTest {

	private final
	List <String> expected;

	private final
	List <String> unsorted;

	public
	JpStringComparatorTest (
			@NonNull List <String> expected,
			@NonNull List <String> unsorted) {

		this.expected = expected;
		this.unsorted = unsorted;

	}

	@Test
	public
	void test () {

		List <String> sorted =
			iterableOrderToList (
				unsorted,
				JpStringComparator.instance);

		Assert.assertArrayEquals (
			expected.toArray (),
			sorted.toArray ());

	}

	@Parameters
	public static
	List <Object []> testCases () {

		Random random =
			new Random (
				0xdeadbeef);

		List <Object []> testCases =
			new ArrayList<> ();

		for (
			List <String> sortedStrings
				: sortedStringsList
		) {

			for (
				int index = 0;
				index < 8;
				index ++
			) {

				List <String> remaningStrings =
					new LinkedList<> (
						sortedStrings);

				ArrayList <String> result =
					new ArrayList<> ();

				while (! remaningStrings.isEmpty ()) {

					result.add (
						remaningStrings.remove (
							random.nextInt (
								remaningStrings.size ())));

				}

				testCases.add (
					new Object[] {
						sortedStrings,
						result
					});

			}

		}

		return testCases;

	}

	private static
	List <List <String>> sortedStringsList =
		ImmutableList.of (

		ImmutableList.of (
			"cache.IdCacheBuilder",
			"cache.IdLookupCache",
			"random.RandomLogicImplementation",
			"thread.ThreadManagerImplementation",
			"time.core.DefaultTimeFormatterFactory",
			"time.core.TimeFormatterManager",
			"time.duration.DurationFormatterImplementation"),

		ImmutableList.of (
			"context.RequestContextImplementation",
			"mvc.WebActionRequestHandler",
			"mvc.WebResponderRequestHandler",
			"pathhandler.RegexpPathHandler",
			"responder.BinaryResponder",
			"responder.JsonResponder",
			"responder.TextResponder",
			"servlet.PathHandlerServlet",
			"servlet.ResponseFilter"),

		ImmutableList.of (
			".Hello",
			".hello",
			"Hello",
			"Hello-World",
			"HelloWorld",
			"Helloworld",
			"hello")

	);

}
