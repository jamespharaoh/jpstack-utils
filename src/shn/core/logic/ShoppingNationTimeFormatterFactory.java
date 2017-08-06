package shn.core.logic;

import lombok.NonNull;

import org.joda.time.format.DateTimeFormat;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.time.core.TimeFormatterPlugin;
import wbs.utils.time.core.TimeFormatterPluginImplementation;

@SingletonComponent ("shoppingNationTimeFormatter")
public
class ShoppingNationTimeFormatterFactory
	implements ComponentFactory <ShoppingNationTimeFormatter> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	ShoppingNationTimeFormatter makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			TimeFormatterPlugin plugin =
				new TimeFormatterPluginImplementation ()

				.name (
					"shopping-nation")

				// timestamp

				.timestampSecondFormat (
					DateTimeFormat.forPattern (
						"yyMMddHHmmss"))

				.timestampMinuteFormat (
					DateTimeFormat.forPattern (
						"yyMMddHHmm"))

				.timestampHourFormat (
					DateTimeFormat.forPattern (
						"yyMMddHH"))

				// timestamp timezone

				.timestampTimezoneSecondFormat (
					DateTimeFormat.forPattern (
						"yyMMddHHmmss ZZ"))

				.timestampTimezoneMinuteFormat (
					DateTimeFormat.forPattern (
						"yyMMddHHmm ZZ"))

				.timestampTimezoneHourFormat (
					DateTimeFormat.forPattern (
						"yyMMddHH ZZ"))

				// timestamp timezone short

				.timestampTimezoneSecondShortFormat (
					DateTimeFormat.forPattern (
						"yyMMddHHmmss zzz"))

				.timestampTimezoneMinuteShortFormat (
					DateTimeFormat.forPattern (
						"yyMMddHHmm zzz"))

				.timestampTimezoneHourShortFormat (
					DateTimeFormat.forPattern (
						"yyMMddHH zzz"))

				// timestamp timezone

				.timestampTimezoneSecondLongFormat (
					DateTimeFormat.forPattern (
						"yyMMddHHmmss zzzz"))

				.timestampTimezoneMinuteLongFormat (
					DateTimeFormat.forPattern (
						"yyMMddHHmm zzzz"))

				.timestampTimezoneHourLongFormat (
					DateTimeFormat.forPattern (
						"yyMMddHH zzzz"))

				// date

				.longDateFormat (
					DateTimeFormat.forPattern (
						"EEEE, d MMMM yyyy"))

				.shortDateFormat (
					DateTimeFormat.forPattern (
						"yyMMdd"))

				// time

				.timeFormat (
					DateTimeFormat.forPattern (
						"HHmmSS"))

				// timezone

				.timezoneLongFormat (
					DateTimeFormat.forPattern (
						"zzzz"))

				.timezoneShortFormat (
					DateTimeFormat.forPattern (
						"zzz"))

			;

			return new ShoppingNationTimeFormatter () {

				@Override
				public
				TimeFormatterPlugin plugin () {
					return plugin;
				}

			};

		}

	}

}
