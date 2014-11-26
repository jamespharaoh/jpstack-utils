package wbs.sms.number.core.logic;

import java.util.Date;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.number.core.model.ChatUserNumberReportObjectHelper;
import wbs.sms.number.core.model.ChatUserNumberReportRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;

@Log4j
@SingletonComponent ("numberLogicImpl")
public
class NumberLogicImpl
	implements NumberLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	ChatUserNumberReportObjectHelper chatUserNumberReportHelper;

	@Inject
	NumberObjectHelper numberHelper;

	// implementation

	@Override
	public
	NumberRec objectToNumber (
			Object object) {

		if (object instanceof NumberRec) {

			return
				(NumberRec)
				object;

		}

		if (object instanceof String) {

			return numberHelper.findOrCreate (
				(String) object);

		}

		throw new IllegalArgumentException ();

	}

	@Override
	public
	void updateDeliveryStatusForNumber (
			String numTo,
			MessageStatus status) {

		NumberRec number =
			numberHelper.findOrCreate (
				numTo);

		// TODO should not be here

		ChatUserNumberReportRec chatUserNumberReportRec =
			chatUserNumberReportHelper.find (
				number.getId ());

		if (chatUserNumberReportRec == null) {

			chatUserNumberReportRec =
				chatUserNumberReportHelper.insert (
					new ChatUserNumberReportRec ()
						.setNumber (number));

		}

		if (status.isGoodType()) {

			chatUserNumberReportRec
				.setLastSuccess (new Date ());

		} else if (status.isBadType () || status.isPending ()) {

			if (chatUserNumberReportRec.getFirstFailure () == null) {

				chatUserNumberReportRec
					.setFirstFailure (new Date ());

			}

		}

	}

	@Override
	public
	NumberRec archiveNumberFromMessage (
			MessageRec message) {

		// TODO i don't like this at all

		NumberRec oldNumber =
			message.getNumber ();

		String currentNumber =
			oldNumber.getNumber ();

		// re-name old number

		oldNumber
			.setArchiveDate (new Date ())
			.setNumber (currentNumber + "." + oldNumber.getId ());

		database.flush ();

		// create new number and save

		NumberRec newNumber =
			numberHelper.insert (
				new NumberRec ()
					.setNumber (currentNumber)
					.setNetwork (oldNumber.getNetwork ()));

		// assign message to new number

		message.setNumber (newNumber);

		database.flush ();

		log.warn ("Archived number " + currentNumber + " as " + oldNumber.getNumber ());

		return newNumber;

	}

}