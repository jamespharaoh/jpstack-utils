package wbs.utils.collection;

import static wbs.utils.etc.DebugUtils.debugFormat;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.maximumJavaInteger;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.utils.etc.SafeCloseable;

public
class ParallelIterator <In, Out>
	implements
		Iterator <Out>,
		SafeCloseable {

	// state

	private final
	Long maxTasks;

	private final
	ExecutorService executor;

	private final
	Iterator <In> inIterator;

	private final
	Function <In, Out> mapFunction;

	private final
	CompletionService <Out> completionService;

	private
	long runningTasks = 0l;

	private
	Optional <Out> nextItemOptional =
		optionalAbsent ();

	private
	Optional <Throwable> exceptionOptional =
		optionalAbsent ();

	private
	boolean error = false;

	// constructors

	public
	ParallelIterator (
			@NonNull Long threads,
			@NonNull Long maxTasks,
			@NonNull Iterator <In> inIterator,
			@NonNull Function <In, Out> mapFunction) {

debugFormat ("CONSTRUCT");

		if (
			threads <= 1l
			|| threads > maximumJavaInteger
		) {
			throw new IllegalArgumentException ("threads");
		}

		if (
			maxTasks < threads
			|| maxTasks > maximumJavaInteger
		) {
			throw new IllegalArgumentException ("maxTasks");
		}

		this.maxTasks =
			maxTasks;

		this.inIterator =
			inIterator;

		this.mapFunction =
			mapFunction;

		executor =
			Executors.newFixedThreadPool (
				toJavaIntegerRequired (
					threads));

		completionService =
			new ExecutorCompletionService<> (
				executor);

		startTasks ();

	}

	// public implementation

	@Override
	public
	boolean hasNext () {

debugFormat ("HAS NEXT %s", integerToDecimalString (runningTasks));

		if (error) {
debugFormat ("ERROR");
			throw new IllegalStateException ();
		}

		if (exceptionOptional.isPresent ()) {
debugFormat ("EXCEPTION");
			return true;
		}

		if (nextItemOptional.isPresent ()) {
debugFormat ("ITEM");
			return true;
		}

		if (runningTasks == 0l) {
debugFormat ("COMPLETE");
			return false;
		}

		try {

			runningTasks --;

debugFormat ("WAIT");

			nextItemOptional =
				optionalOf (
					completionService.take ().get ());

debugFormat ("WAIT OVER");

			startTasks ();

			return runningTasks > 0l;

		} catch (ExecutionException executionException) {

debugFormat ("ERROR");

			drainTasks ();

			exceptionOptional =
				optionalOf (
					executionException.getCause ());

			return true;

		} catch (InterruptedException interruptedException) {

debugFormat ("INTERRUPT");

			drainTasks ();

			Thread.currentThread ().interrupt ();

			return false;

		}

	}

	@Override
	public
	Out next () {

debugFormat ("NEXT");

		if (error) {
			throw new IllegalStateException ();
		}

		if (exceptionOptional.isPresent ()) {

			error = true;

			Throwable exception =
				optionalGetRequired (
					exceptionOptional);

			if (exception instanceof RuntimeException) {

				throw (RuntimeException)
					exception;

			} else if (exception instanceof Error) {

				throw (Error)
					exception;

			} else {

				throw new RuntimeException (
					exception);

			}

		}

		if (! nextItemOptional.isPresent ()) {

			error = true;

			throw new NoSuchElementException ();

		}

		Out nextItem =
			optionalGetRequired (
				nextItemOptional);

		nextItemOptional =
			optionalAbsent ();

		return nextItem;

	}

	@Override
	public
	void close () {
		drainTasks ();
	}

	// private implementation

	private
	void startTasks () {

debugFormat ("START");

		while (
			runningTasks < maxTasks
			&& inIterator.hasNext ()
		) {

debugFormat ("START ONE");

			In inItem =
				inIterator.next ();

			completionService.submit (
				() -> {
					debugFormat ("TASK START");
					Out temp =
						mapFunction.apply (
							inItem);
					debugFormat ("TASK END");
					return temp;
				});

			runningTasks ++;

		}

debugFormat ("STARTED");

	}

	private
	void drainTasks () {

debugFormat ("DRAIN");

		executor.shutdownNow ();

		try {

			executor.awaitTermination (
				1l,
				TimeUnit.MINUTES);

		} catch (InterruptedException interruptedException) {

			Thread.currentThread ().interrupt ();

		}

debugFormat ("DRAINED");

	}

}
