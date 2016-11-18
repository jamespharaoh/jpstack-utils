package wbs.platform.send;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

import wbs.platform.daemon.SleepingDaemonService;

@Accessors (fluent = true)
public abstract
class GenericSendDaemon <
	Service extends Record <Service>,
	Job extends Record <Job>,
	Item extends Record <Item>
>
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	Database database;

	// hooks

	protected abstract
	Logger log ();

	protected abstract
	GenericSendHelper <Service, Job, Item> helper ();

	// details

	@Override
	protected
	String getThreadName () {

		return stringFormat (
			"%sSend",
			capitalise (
				helper ().name ()));

	}

	@Override
	protected
	Duration getSleepDuration () {

		return Duration.standardSeconds (
			5);

	}

	@Override
	protected
	String generalErrorSource () {

		return stringFormat (
			"%s send daemon",
			helper ().name ());

	}

	@Override
	protected
	String generalErrorSummary () {

		return stringFormat (
			"error sending %s in background",
			helper ().itemNamePlural ());

	}

	// implementation

	@Override
	protected
	void runOnce () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"GenericSendDaemon.runOnce ()",
				this);

		log ().debug (
			stringFormat (
				"Looking for jobs in sending state"));

		List <Job> jobs =
			helper ().findSendingJobs ();

		List <Long> jobIds =
			jobs.stream ()

			.map (
				Job::getId)

			.collect (
				Collectors.toList ());

		transaction.close ();

		jobIds.forEach (
			this::runJob);

	}

	void runJob (
			@NonNull Long jobId) {

		log ().debug (
			stringFormat (
				"Performing send for %s",
				integerToDecimalString (
					jobId)));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"GenericSendDaemon.runJob (jobId)",
				this);

		Job job =
			helper ().jobHelper ().findRequired (
				jobId);

		Service service =
			helper ().getService (
				job);

		if (
			! helper ().jobSending (
				service,
				job)
		) {

			log ().debug (
				stringFormat (
					"Not sending job %s",
					integerToDecimalString (
						jobId)));

			return;

		}

		if (
			! helper ().jobConfigured (
				service,
				job)
		) {

			log ().warn (
				stringFormat (
					"Not configured job %s",
					integerToDecimalString (
						jobId)));

			return;

		}

		List <Item> items =
			helper ().findItemsLimit (
				service,
				job,
				100);

		if (
			items.isEmpty ()
		) {

			// handle completion

			log ().debug (
				stringFormat (
					"Triggering completion for job %s",
					integerToDecimalString (
						jobId)));

			helper ().sendComplete (
				service,
				job);

		} else {

			// perform send

			for (
				Item item
					: items
			) {

				// verify the item

				boolean itemVerified =
					helper ().verifyItem (
						service,
						job,
						item);

				if (itemVerified) {

					log ().debug (
						stringFormat (
							"Sending item %s",
							integerToDecimalString (
								item.getId ())));

					// send item

					helper ().sendItem (
						service,
						job,
						item);

				} else {

					log ().debug (
						stringFormat (
							"Rejecting item %s",
							integerToDecimalString (
								item.getId ())));

					// reject it

					helper ().rejectItem (
						service,
						job,
						item);

				}

			}

		}

		// commit transaction

		transaction.commit ();

		log ().debug (
			stringFormat (
				"Finished send for job %s",
				integerToDecimalString (
					jobId)));

	}

}
