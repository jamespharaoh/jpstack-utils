package wbs.apn.chat.supervisor.console;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.reporting.console.StatsDataSet;
import wbs.platform.reporting.console.StatsDatum;
import wbs.platform.reporting.console.StatsGranularity;
import wbs.platform.reporting.console.StatsPeriod;
import wbs.platform.reporting.console.StatsProvider;

@SingletonComponent ("chatUserInitiationStatsProvider")
public
class ChatUserInitiationStatsProvider
	implements StatsProvider {

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserInitiationLogObjectHelper chatUserInitiationLogHelper;

	@Override
	public
	StatsDataSet getStats (
			@NonNull StatsPeriod period,
			@NonNull Map<String,Object> conditions) {

		if (period.granularity () != StatsGranularity.hour)
			throw new IllegalArgumentException ();

		if (! conditions.containsKey ("chatId"))
			throw new IllegalArgumentException ();

		// setup data structures

		Map<Integer,int[]> alarmsPerUser =
			new TreeMap<Integer,int[]> ();

		Set<Object> userIds =
			new HashSet<Object> ();

		// retrieve messages

		ChatRec chat =
			chatHelper.find (
				(Integer) conditions.get ("chatId"));

		List<ChatUserInitiationLogRec> logs =
			chatUserInitiationLogHelper.findByTimestamp (
				chat,
				period.toInterval ());

		// aggregate stats

		for (ChatUserInitiationLogRec log : logs) {

			if (log.getReason () != ChatUserInitiationReason.alarmSet)
				continue;

			if (log.getMonitorUser () == null)
				continue;

			// work out which hour

			Instant timestamp =
				new Instant (log.getTimestamp ());

			int hour =
				period.assign (timestamp);

			// count alarms per user

			if (! userIds.contains (
					log.getMonitorUser ().getId ())) {

				userIds.add (
					log.getMonitorUser ().getId ());

				alarmsPerUser.put (
					log.getMonitorUser ().getId (),
					new int [period.size ()]);

			}

			int[] userAlarms =
				alarmsPerUser.get (
					log.getMonitorUser ().getId ());

			userAlarms [hour] ++;

		}

		// create return value

		StatsDataSet statsDataSet =
			new StatsDataSet ();

		statsDataSet.indexValues ()
			.put ("userId", userIds);

		for (
			int hour = 0;
			hour < period.size ();
			hour ++
		) {

			for (Object userIdObject : userIds) {

				Integer userId =
					(Integer) userIdObject;

				statsDataSet.data ().add (
					new StatsDatum ()

					.startTime (
						period.step (hour))

					.addIndex (
						"chatId",
						conditions.get ("chatId"))

					.addIndex (
						"userId",
						userId)

					.addValue (
						"alarmsSet",
						alarmsPerUser.get (userId) [hour]));

			}

		}

		return statsDataSet;

	}

}
