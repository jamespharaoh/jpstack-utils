package wbs.utils.time.core;

import static wbs.utils.time.TimeUtils.localTime;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableInstant;

public
interface TimeFormatterTimeMethods {

	// time string seconds

	String timeStringSeconds (
			LocalTime time);

	default
	String timeStringSeconds (
			@NonNull ReadableDateTime dateTime) {

		return timeStringSeconds (
			localTime (
				dateTime));

	}

	default
	String timeStringSeconds (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return timeStringSeconds (
			localTime (
				timeZone,
				instant));

	}

	// time string minutes

	String timeStringMinutes (
			LocalTime time);

	default
	String timeStringMinutes (
			@NonNull ReadableDateTime dateTime) {

		return timeStringMinutes (
			localTime (
				dateTime));

	}

	default
	String timeStringMinutes (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return timeStringMinutes (
			localTime (
				timeZone,
				instant));

	}

	// time string hours

	String timeStringHours (
			LocalTime time);

	default
	String timeStringHours (
			@NonNull ReadableDateTime dateTime) {

		return timeStringHours (
			localTime (
				dateTime));

	}

	default
	String timeStringHours (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return timeStringHours (
			localTime (
				timeZone,
				instant));

	}

}
