package wbs.utils.etc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	List <String> example;

	public
	JpStringComparatorTest (
			@NonNull List <String> example) {

		this.example = example;

	}

	@Test
	public
	void test () {

		Collections.sort (
			example,
			JpStringComparator.instance);

		Assert.assertArrayEquals (
			sortedStrings.toArray (),
			example.toArray ());

	}

	@Parameters
	public static
	List <Object []> testCases () {

		Random random =
			new Random (
				0xdeadbeef);

		return IntStream.range (0, 64)

			.mapToObj (
				index -> {

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

				return new Object[] {
					result
				};

			})

			.collect (
				Collectors.toList ())

		;

	}

	private static
	List <String> sortedStrings =
		ImmutableList.of (
			".Hello",
			".hello",
			"Hello",
			"Hello-World",
			"HelloWorld",
			"Helloworld",
			"hello");

}
