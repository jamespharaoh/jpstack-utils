package wbs.utils.time.duration;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;

public
interface DurationFormatterMethods {

	// duration string

	String durationStringNumericSeconds (
			ReadableDuration interval);

	default
	String durationStringNumericSeconds (
			@NonNull ReadableInstant start,
			@NonNull ReadableInstant end) {

		return durationStringNumericSeconds (
			new Duration (
				start,
				end));

	}

	String durationStringNumericMinutes (
			ReadableDuration interval);

	default
	String durationStringNumericMinutes (
			@NonNull ReadableInstant start,
			@NonNull ReadableInstant end) {

		return durationStringNumericMinutes (
			new Duration (
				start,
				end));

	}

	String durationStringNumericHours (
			ReadableDuration interval);

	default
	String durationStringNumericHours (
			@NonNull ReadableInstant start,
			@NonNull ReadableInstant end) {

		return durationStringNumericHours (
			new Duration (
				start,
				end));

	}

	String durationStringExact (
			ReadableDuration interval);

	default
	String durationStringExact (
			@NonNull ReadableInstant start,
			@NonNull ReadableInstant end) {

		return durationStringExact (
			new Duration (
				start,
				end));

	}

	String durationStringApproximate (
			ReadableDuration interval);

	default
	String durationStringApproximate (
			@NonNull ReadableInstant start,
			@NonNull ReadableInstant end) {

		return durationStringApproximate (
			new Duration (
				start,
				end));

	}

	// string to duration

	Optional <Duration> stringToDuration (
			String input);

	default
	Duration stringToDurationRequired (
			@NonNull String input) {

		return optionalGetRequired (
			stringToDuration (
				input));

	}

}
