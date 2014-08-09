package wbs.sms.number.list.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;
import wbs.sms.number.list.model.NumberListNumberRec;
import wbs.sms.number.list.model.NumberListRec;
import wbs.sms.number.list.model.NumberListUpdateRec;

@Accessors (fluent = true)
@PrototypeComponent ("numberListNumbersAction")
public
class NumberListNumbersAction
	extends ConsoleAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	NumberFormatLogic numberFormatLogic;

	@Inject
	NumberListConsoleHelper numberListHelper;

	@Inject
	NumberListNumberConsoleHelper numberListNumberHelper;

	@Inject
	NumberListUpdateConsoleHelper numberListUpdateHelper;

	@Inject
	NumberLogic numberLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {
		return responder ("numberListNumbersResponder");
	}

	// implementation

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		int loop = 0;

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		NumberListRec numberList =
			numberListHelper.find (
				requestContext.stuffInt ("numberListId"));

		NumberListUpdateRec numberListUpdate =
			new NumberListUpdateRec ()
				.setNumberList (numberList)
				.setTimestamp (transaction.now ())
				.setUser (myUser)
				.setNumberCount (0);

		// parse numbers

		List<String> numbers;

		try {

			numbers =
				numberFormatLogic.parseLines (
					numberList.getNumberFormat (),
					requestContext.parameter ("numbers"));

		} catch (WbsNumberFormatException exception) {

			requestContext.addNotice (
				"Invalid number format");

			return null;

		}

		// add numbers

		int numAdded = 0;
		int numAlreadyAdded = 0;

		if (requestContext.parameter ("add") != null) {

			numberListUpdate.setPresent (true);

			for (String numberString : numbers) {

				if (++ loop % 1000 == 0)
					transaction.flush ();

				NumberRec number =
					numberLogic.findOrCreateNumber (
						numberString);

				NumberListNumberRec numberListNumber =
					numberListNumberHelper.findOrCreate (
						numberList,
						number);

				if (numberListNumber.getPresent ()) {

					numAlreadyAdded ++;

					continue;

				}

				numberListNumber.setPresent (
					true);

				numberListUpdate.setNumberCount (
					numberListUpdate.getNumberCount () + 1);

				numberListUpdate.getNumbers ().add (
					number);

				numberList.setNumberCount (
					numberList.getNumberCount () + 1);

				numAdded ++;

			}

		}

		// remove numbers

		int numRemoved = 0;
		int numAlreadyRemoved = 0;

		if (requestContext.parameter ("remove") != null) {

			numberListUpdate.setPresent (false);

			for (String numberString : numbers) {

				if (++ loop % 1000 == 0)
					transaction.flush ();

				NumberRec number =
					numberLogic.findOrCreateNumber (
						numberString);

				NumberListNumberRec numberListNumber =
					numberListNumberHelper.find (
						numberList,
						number);

				if (numberListNumber == null
						|| ! numberListNumber.getPresent ()) {

					numAlreadyRemoved ++;

					continue;

				}

				numberListNumber.setPresent (
					false);

				numberListUpdate.setNumberCount (
					numberListUpdate.getNumberCount () - 1);

				numberListUpdate.getNumbers ().add (
					number);

				numberList.setNumberCount (
					numberList.getNumberCount () - 1);

				numRemoved ++;

			}

		}

		// insert update

		if (numberListUpdate.getNumberCount () > 0) {

			numberListUpdateHelper.insert (
				numberListUpdate);

		}

		// events

		if (numAdded > 0) {

			eventLogic.createEvent (
				"number_list_numbers_added",
				myUser,
				numAdded,
				numberList);

		}

		if (numRemoved > 0) {

			eventLogic.createEvent (
				"number_list_numbers_removed",
				myUser,
				numRemoved,
				numberList);

		}

		// commit transaction

		transaction.commit ();

		// messages

		if (numAdded > 0) {

			requestContext.addNotice (
				stringFormat (
					"%s numbers added",
					numAdded));

		}

		if (numAlreadyAdded > 0) {

			requestContext.addWarning (
				stringFormat (
					"%s numbers already added",
					numAlreadyAdded));

		}

		if (numRemoved > 0) {

			requestContext.addNotice (
				stringFormat (
					"%s numbers removed",
					numRemoved));

		}

		if (numAlreadyRemoved > 0) {

			requestContext.addWarning (
				stringFormat (
					"%s numbers already removed",
					numAlreadyRemoved));

		}

		return null;

	}

}
