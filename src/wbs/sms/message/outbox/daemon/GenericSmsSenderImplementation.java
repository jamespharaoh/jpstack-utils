package wbs.sms.message.outbox.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifElse;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.todo;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.framework.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Map;
import java.util.stream.LongStream;

import javax.inject.Inject;

import org.joda.time.Duration;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.etc.JsonUtils;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.PerformSendResult;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.PerformSendStatus;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.ProcessResponseResult;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.ProcessResponseStatus;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.SetupRequestResult;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.SetupRequestStatus;
import wbs.sms.message.outbox.logic.SmsOutboxLogic;
import wbs.sms.message.outbox.logic.SmsOutboxLogic.FailureType;
import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.message.outbox.model.SmsOutboxAttemptObjectHelper;
import wbs.sms.message.outbox.model.SmsOutboxAttemptRec;
import wbs.sms.number.blacklist.model.BlacklistObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.logic.NumberLookupManager;
import wbs.sms.route.core.model.RouteRec;

@Log4j
@Accessors (fluent = true)
@PrototypeComponent ("genericSmsSender")
public
class GenericSmsSenderImplementation<StateType>
	implements GenericSmsSender {

	// constants

	final
	long databaseRetriesMax = 1000;

	final
	Duration databaseRetryWaitMax =
		Duration.standardSeconds (1);

	// dependencies

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	ExceptionUtils exceptionUtils;

	@Inject
	NumberLookupManager numberLookupManager;

	@Inject
	BlacklistObjectHelper smsBlacklistHelper;

	@Inject
	SmsMessageLogic smsMessageLogic;

	@Inject
	SmsOutboxAttemptObjectHelper smsOutboxAttemptHelper;

	@Inject
	OutboxObjectHelper smsOutboxHelper;

	@Inject
	SmsOutboxLogic smsOutboxLogic;

	// properties

	@Getter
	SmsSenderHelper<StateType> smsSenderHelper;

	@Override
	public
	GenericSmsSenderImplementation<StateType> smsSenderHelper (
			@NonNull SmsSenderHelper<?> smsSenderHelper) {

		@SuppressWarnings ("unchecked")
		SmsSenderHelper<StateType> smsSenderHelperTemp =
			(SmsSenderHelper<StateType>)
			smsSenderHelper;

		this.smsSenderHelper =
			smsSenderHelperTemp;

		return this;

	}

	@Getter @Setter
	Long smsMessageId;

	// state

	StateType state;

	Long smsOutboxAttemptId;

	SetupRequestResult<StateType> setupRequestResult;
	PerformSendResult performSendResult;
	ProcessResponseResult processResponseResult;

	// implementation

	@Override
	public
	void send () {

		// setup request

		setupSend ();

		if (
			notEqual (
				setupRequestResult.status (),
				SetupRequestStatus.success)
		) {

			return;

		}

		state =
			setupRequestResult.state ();

		// perform send

		performSend ();

		if (
			notEqual (
				performSendResult.status (),
				PerformSendStatus.success)
		) {

			handlePerformSendError ();

			return;

		}

		// process response

		processResponse ();

	}

	void setupSend () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"GenericSmsSenderImplementation.setupSend ()",
				this);

		OutboxRec smsOutbox =
			smsOutboxHelper.findRequired (
				smsMessageId);

		MessageRec smsMessage =
			smsOutbox.getMessage ();

		RouteRec smsRoute =
			smsOutbox.getRoute ();

		// check block number list

		if (
			checkBlacklist (
				smsRoute,
				smsMessage.getNumber ())
		) {

			smsOutbox

				.setSending (
					null);

			smsMessageLogic.blackListMessage (
				smsOutbox.getMessage ());

			transaction.commit ();

			setupRequestResult =
				new SetupRequestResult<StateType> ()

				.status (
					SetupRequestStatus.blacklisted)

				.statusMessage (
					"Not sending message due to blacklist");

			return;

		}

		// call setup send hook

		try {

			setupRequestResult =
				smsSenderHelper.setupRequest (
					smsOutbox);

			if (

				isNull (
					setupRequestResult)

				|| isNull (
					setupRequestResult.status ())

			) {
				throw new NullPointerException ();
			}

		} catch (Exception exception) {

			exceptionLogger.logThrowable (
				"console",
				getClass ().getSimpleName (),
				exception,
				Optional.absent (),
				GenericExceptionResolution.fatalError);

			setupRequestResult =
				new SetupRequestResult<StateType> ()

				.status (
					SetupRequestStatus.unknownError)

				.statusMessage (
					stringFormat (
						"Error setting up send: %s: %s",
						exception.getClass ().getSimpleName (),
						ifNull (
							exception.getMessage (),
							"null")))

				.exception (
					exception);

		}

		// handle error

		if (
			notEqual (
				setupRequestResult.status (),
				SetupRequestStatus.success)
		) {

			smsOutboxLogic.messageFailure (
				smsMessage,
				setupRequestResult.statusMessage (),
				SmsOutboxLogic.FailureType.permanent);

			transaction.commit ();

			return;

		}

		// create send attempt

		SmsOutboxAttemptRec smsOutboxAttempt =
			smsOutboxLogic.beginSendAttempt (
				smsOutbox,
				optionalMapRequired (
					optionalFromNullable (
						setupRequestResult.requestTrace ()),
					JsonUtils::jsonToBytes));

		smsOutboxAttemptId =
			smsOutboxAttempt.getId ();

		// commit and return

		transaction.commit ();

	}

	void performSend () {

		// now send it

		try {

			performSendResult =
				smsSenderHelper.performSend (
					state);

			if (

				isNull (
					performSendResult)

				|| isNull (
					performSendResult.status ())

			) {
				throw new NullPointerException ();
			}

		} catch (Exception exception) {

			performSendResult =
				new PerformSendResult ()

				.status (
					PerformSendStatus.unknownError)

				.exception (
					exception);

		}

		if (
			notEqual (
				performSendResult.status (),
				PerformSendStatus.success)
		) {

			// set status message from exception if not present

			if (

				isNotNull (
					performSendResult.exception ())

				&& isNull (
					performSendResult.statusMessage ())

			) {

				Throwable exception =
					performSendResult.exception ();

				performSendResult.statusMessage (
					ifElse (
						isNotNull (
							exception.getMessage ()),

					() -> stringFormat (
						"Error sending message: %s: %s",
						exception.getClass ().getSimpleName (),
						exception.getMessage ()),

					() -> stringFormat (
						"Error sending message: %s",
						exception.getClass ().getSimpleName ())

				));

			}

			// set error trace from exception if not present

			if (

				isNotNull (
					performSendResult.exception)

				&& isNull (
					performSendResult.errorTrace ())

			) {

				performSendResult.errorTrace (
					exceptionUtils.throwableDumpJson (
						performSendResult.exception ()));

			}

		}

	}

	void handlePerformSendError () {

		boolean success =
			LongStream.range (0, databaseRetriesMax).anyMatch (
				attemptNumber ->
					attemptToHandlePerformSendError (
						attemptNumber));

		if (! success) {

			throw todo ();

		}

	}

	boolean attemptToHandlePerformSendError (
			@NonNull Long attemptNumber) {

		try {

			attemptToHandlePerformSendErrorReal (
				attemptNumber);

			return true;

		} catch (Throwable exception) {

			exceptionLogger.logThrowable (
				"daemon",
				getClass ().getSimpleName (),
				exception,
				Optional.absent (),
				GenericExceptionResolution.tryAgainLater);

			return false;

		}

	}

	void attemptToHandlePerformSendErrorReal (
			@NonNull Long attemptNumber) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s (%s)",
					"GenericSmsSenderImplementation",
					"attemptToHandlePerformSendErrorReal",
					joinWithCommaAndSpace (
						stringFormat (
							"smsMessageId = %s",
							smsMessageId),
						stringFormat (
							"smsOutboxAttemptId = %s",
							smsOutboxAttemptId),
						stringFormat (
							"attemptNUmber = %s",
							attemptNumber))),
				this);

		SmsOutboxAttemptRec smsOutboxAttempt =
			smsOutboxAttemptHelper.findRequired (
				smsOutboxAttemptId);

		smsOutboxLogic.completeSendAttemptFailure (
			smsOutboxAttempt,
			FailureType.temporary,
			performSendResult.statusMessage (),
			optionalMapRequired (
				Optional.fromNullable (
					performSendResult.requestTrace ()),
				JsonUtils::jsonToBytes),
			optionalMapRequired (
				Optional.fromNullable (
					performSendResult.responseTrace ()),
				JsonUtils::jsonToBytes),
			optionalMapRequired (
				Optional.fromNullable (
					performSendResult.errorTrace ()),
				JsonUtils::jsonToBytes));

		transaction.commit ();

	}

	void processResponse () {

		boolean success =
			LongStream.range (0, databaseRetriesMax).anyMatch (
				attemptNumber ->
					attemptToProcessResponse (
						state,
						attemptNumber));

		if (! success) {

			throw todo ();

		}

	}

	boolean attemptToProcessResponse (
			@NonNull StateType state,
			@NonNull Long attemptNumber) {

		try {

			attemptToProcessResponseReal (
				state,
				attemptNumber);

			return true;

		} catch (Throwable exception) {

			exceptionLogger.logThrowable (
				"daemon",
				getClass ().getSimpleName (),
				exception,
				Optional.absent (),
				GenericExceptionResolution.tryAgainLater);

			return false;

		}

	}

	void attemptToProcessResponseReal (
			@NonNull StateType state,
			@NonNull Long attemptNumber) {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s (%s)",
					"GenericSmsSenderImplementation",
					"attemptToProcessResponseReal",
					joinWithCommaAndSpace (
						stringFormat (
							"smsMessageId = %s",
							smsMessageId),
						stringFormat (
							"smsOutboxAttemptId = %s",
							smsOutboxAttemptId),
						stringFormat (
							"attemptNUmber = %s",
							attemptNumber))),
				this);

		SmsOutboxAttemptRec smsOutboxAttempt =
			smsOutboxAttemptHelper.findRequired (
				smsOutboxAttemptId);

		// process response

		try {

			processResponseResult =
				smsSenderHelper.processSend (
					state);

			if (

				isNull (
					processResponseResult)

				|| isNull (
					processResponseResult.status ())

			) {
				throw new NullPointerException ();
			}

		} catch (Exception exception) {

			processResponseResult =
				new ProcessResponseResult ()

				.status (
					ProcessResponseStatus.unknownError)

				.exception (
					exception);

		}

		// store result

		if (
			equal (
				processResponseResult.status (),
				ProcessResponseStatus.success)
		) {

			smsOutboxLogic.completeSendAttemptSuccess (
				smsOutboxAttempt,
				optionalFromNullable (
					processResponseResult.otherIds ()),
				optionalMapRequired (
					optionalFromNullable (
						performSendResult.requestTrace ()),
					JsonUtils::jsonToBytes),
				optionalMapRequired (
					optionalFromNullable (
						performSendResult.responseTrace ()),
					JsonUtils::jsonToBytes));

		} else {

			if (
				isNull (
					processResponseResult.failureType ())
			) {

				log.warn (
					stringFormat (
						"No failure type for send attempt %s ",
						smsOutboxAttemptId,
						"(defaulting to temporary)"));

				processResponseResult.failureType (
					FailureType.temporary);

			}

			if (
				isNull (
					processResponseResult.statusMessage ())
			) {

				processResponseResult.statusMessage (
					defaultStatusMessages.get (
						processResponseResult.failureType ()));

			}

			smsOutboxLogic.completeSendAttemptFailure (
				smsOutboxAttempt,
				processResponseResult.failureType (),
				processResponseResult.statusMessage (),
				optionalMapRequired (
					Optional.fromNullable (
						performSendResult.requestTrace ()),
					JsonUtils::jsonToBytes),
				optionalMapRequired (
					Optional.fromNullable (
						performSendResult.responseTrace ()),
					JsonUtils::jsonToBytes),
				optionalMapRequired (
					Optional.fromNullable (
						processResponseResult.errorTrace ()),
					JsonUtils::jsonToBytes));

		}

		// commit and return

		transaction.commit ();

	}

	boolean checkBlacklist (
			@NonNull RouteRec smsRoute,
			@NonNull NumberRec number) {

		// check route block list

		String numberString =
			number.getNumber ();

		if (

			isNotNull (
				smsRoute.getBlockNumberLookup ())

			&& numberLookupManager.lookupNumber (
				smsRoute.getBlockNumberLookup (),
				number)

		) {
			return true;
		}

		// check global blacklist

		// TODO aaargh

		if (
			isPresent (
				smsBlacklistHelper.findByCode (
					GlobalId.root,
					numberString))
		) {
			return true;
		}

		// not blacklisted

		return false;

	}

	public final static
	Map<FailureType,String> defaultStatusMessages =
		ImmutableMap.<FailureType,String>builder ()

		.put (
			FailureType.daily,
			"Unspecified daily failure")

		.put (
			FailureType.permanent,
			"Unspecified permanent failure")

		.put (
			FailureType.temporary,
			"Unspecified temporart failure")

		.build ();

}