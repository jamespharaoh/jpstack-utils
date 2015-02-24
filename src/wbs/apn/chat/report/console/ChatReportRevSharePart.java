package wbs.apn.chat.report.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.NonNull;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import wbs.apn.chat.affiliate.console.ChatAffiliateConsoleHelper;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.bill.console.ChatRouteConsoleHelper;
import wbs.apn.chat.bill.console.ChatUserCreditConsoleHelper;
import wbs.apn.chat.bill.model.ChatRouteNetworkRec;
import wbs.apn.chat.bill.model.ChatRouteRec;
import wbs.apn.chat.bill.model.ChatUserCreditRec;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageSearch;
import wbs.apn.chat.core.console.ChatMonthCostConsoleHelper;
import wbs.apn.chat.core.console.ChatReportConsoleHelper;
import wbs.apn.chat.core.model.ChatMonthCostRec;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.report.model.ChatReportRevShareRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.hibernate.HibernateDatabase;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.console.AffiliateConsoleHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.module.ConsoleModule;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.stats.console.MessageStatsConsoleHelper;
import wbs.sms.message.stats.model.MessageStats;
import wbs.sms.message.stats.model.MessageStatsRec;
import wbs.sms.message.stats.model.MessageStatsRec.MessageStatsSearch;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@PrototypeComponent ("chatReportRevSharePart")
public
class ChatReportRevSharePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	AffiliateConsoleHelper affiliateHelper;

	@Inject
	ChatAffiliateConsoleHelper chatAffiliateHelper;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatMonthCostConsoleHelper chatMonthCostHelper;

	@Inject @Named
	ConsoleModule chatReportConsoleModule;

	@Inject
	ChatRouteConsoleHelper chatRouteHelper;

	@Inject
	ChatReportConsoleHelper chatReportHelper;

	@Inject
	ChatUserCreditConsoleHelper chatUserCreditHelper;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	ConsoleRequestContext consoleRequestContext;

	@Inject
	HibernateDatabase database;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	MessageStatsConsoleHelper messageStatsHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	TimeFormatter timeFormatter;

	// state

	FormFieldSet searchFields;
	FormFieldSet resultsFields;

	ChatReportRevShareForm form;

	LocalDate startDate;
	LocalDate endDate;

	ChatRec chat;

	Map<AffiliateRec,ChatReportRevShareRec> chatReportsByAffiliate;
	ChatReportRevShareRec totalReport;

	List<ChatReportRevShareRec> chatReportsSorted;

	String outputTypeParam;

	@Override
	public
	void prepare () {

		searchFields =
			chatReportConsoleModule.formFieldSets ().get (
				"monthReportSearch");

		resultsFields =
			chatReportConsoleModule.formFieldSets ().get (
				"simpleReportResults");

		// get search form

		LocalDate today =
			LocalDate.now ();

		form =
			new ChatReportRevShareForm ()

			.month (
				today.toString (
					"YYYY-MM"));

		formFieldLogic.update (
			searchFields,
			form);

		chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		totalReport =
			new ChatReportRevShareRec ()

			.setCurrency (
				chat.getCurrency ())

			.setPath (
				"TOTAL");

		startDate =
			LocalDate.parse (
				stringFormat (
					"%s-01",
					form.month ()));

		endDate =
			startDate.plusMonths (1);

		// add stat sources

		addSmsMessages ();
		addCredits ();
		addJoiners ();
		addChatMessages ();

		// sort chat reports

		List<ChatReportRevShareRec> chatReportsTemp =
			new ArrayList<ChatReportRevShareRec> (
				chatReportsByAffiliate.values ());

		Collections.sort (
			chatReportsTemp);

		chatReportsSorted =
			ImmutableList.copyOf (
				chatReportsTemp);

	}

	void addSmsMessages () {

		List<ServiceRec> services =
			objectManager.getChildren (
				chat,
				ServiceRec.class);

		ArrayList<Integer> serviceIds =
			new ArrayList<Integer> ();

		for (
			ServiceRec service
				: services
		) {

			serviceIds.add (
				service.getId ());

		}

		List<MessageStatsRec> allMessageStats =
			messageStatsHelper.search (
				new MessageStatsSearch ()

			.serviceIdIn (
				serviceIds)

			.dateAfter (
				startDate)

			.dateBefore (
				endDate)

		);

		// aggregate by affiliate

		chatReportsByAffiliate =
			new HashMap<AffiliateRec,ChatReportRevShareRec> ();

		ArrayList<Integer> errorRoutes =
			new ArrayList<Integer> ();

		for (
			MessageStatsRec messageStats
				: allMessageStats
		) {

			// find report

			AffiliateRec affiliate =
				messageStats.getMessageStatsId ().getAffiliate ();

			ChatReportRevShareRec currentReport =
				getReport (
					affiliate);

			// find chat route

			RouteRec route =
				messageStats.getMessageStatsId ().getRoute ();

			ChatRouteRec chatRoute =
				chat.getChatRoutes ().get (
					route.getId ());

			if (chatRoute == null) {

				if (! errorRoutes.contains (route.getId ())) {

				    errorRoutes.add (
						route.getId ());

					consoleRequestContext.addError (
						stringFormat (
							"Unknown route: %s (%d)",
							route.getCode (),
							route.getId ()));

				}

				continue;

			}

			// find chat route network

			NetworkRec network =
				messageStats.getMessageStatsId ().getNetwork ();

			Optional<ChatRouteNetworkRec> chatRouteNetwork =
				Optional.fromNullable (
					chatRoute.getChatRouteNetworks ().get (
						network.getId ()));

			// collect stats

			MessageStats statsValue =
				messageStats.getStats ();

			addToReport (
				currentReport,
				chatRoute,
				chatRouteNetwork,
				statsValue);

			addToReport (
				totalReport,
				chatRoute,
				chatRouteNetwork,
				statsValue);

		}

	}

	void addCredits () {

		List<ChatUserCreditRec> chatUserCredits =
			chatUserCreditHelper.findByTimestamp (
				chat,
				new Interval (
					startDate.toDateTimeAtStartOfDay (),
					endDate.toDateTimeAtStartOfDay ()));

		for (
			ChatUserCreditRec chatUserCredit
				: chatUserCredits
		) {

			ChatUserRec chatUser =
				chatUserCredit.getChatUser ();

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					chatUser);

			ChatReportRevShareRec affiliateReport =
				getReport (
					affiliate);

			addToReport (
				affiliateReport,
				chatUserCredit);

			addToReport (
				totalReport,
				chatUserCredit);

		}

	}

	void addJoiners () {

		List<ChatUserRec> joiners =
			chatUserHelper.search (
				ImmutableMap.<String,Object>builder ()

			.put (
				"chatId",
				chat.getId ())

			.put (
				"firstJoinAfter",
				startDate.toDate ())

			.put (
				"firstJoinBefore",
				endDate.toDate ())

			.build ()

		);

		for (
			ChatUserRec chatUser
				: joiners
		) {

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					chatUser);

			ChatReportRevShareRec affiliateReport =
				getReport (
					affiliate);

			affiliateReport.setJoiners (
				affiliateReport.getJoiners () + 1);

			totalReport.setJoiners (
				totalReport.getJoiners () + 1);

		}

	}

	void addToReport (
			@NonNull ChatReportRevShareRec report,
			@NonNull ChatRouteRec chatRoute,
			@NonNull Optional<ChatRouteNetworkRec> chatRouteNetwork,
			@NonNull MessageStats statsValue) {

		if (
			chatRouteNetwork.isPresent ()
			&& chatRouteNetwork.get ().getOutRev () > 0
			&& statsValue.getOutDelivered () > 0
		) {

			report

				.setOutRev (
					report.getOutRev ()
					+ statsValue.getOutDelivered ()
						* chatRouteNetwork.get ().getOutRev ())

				.setOutRevNum (
					report.getOutRevNum ()
					+ statsValue.getOutDelivered ());

		} else if (
			chatRoute.getOutRev () > 0
			&& statsValue.getOutDelivered () > 0
		) {

			report

				.setOutRev (
					report.getOutRev ()
					+ statsValue.getOutDelivered ()
						* chatRoute.getOutRev ())

				.setOutRevNum (
					report.getOutRevNum ()
					+ statsValue.getOutDelivered ());

		}

		if (
			chatRoute.getInRev () > 0
			&& statsValue.getInTotal () > 0
		) {

			report

				.setInRev (
					report.getInRev ()
					+ statsValue.getInTotal ()
						* chatRoute.getInRev ())

				.setInRevNum (
					report.getInRevNum ()
					+ statsValue.getInTotal ());
		}

		if (
			chatRoute.getSmsCost () > 0
			&& statsValue.getOutTotal () > 0
		) {

			report

				.setSmsCost (
					report.getSmsCost ()
					+ statsValue.getOutTotal ()
						* chatRoute.getSmsCost ())

				.setSmsCostNum (
					report.getSmsCostNum ()
					+ statsValue.getOutTotal ());

		}

		if (
			chatRoute.getMmsCost () > 0
			&& statsValue.getOutTotal () > 0
		) {

			report

				.setMmsCost (
					report.getMmsCost ()
					+ statsValue.getOutTotal ()
						* chatRoute.getMmsCost ())

				.setMmsCostNum (
					report.getMmsCostNum ()
					+ statsValue.getOutTotal ());

		}

	}

	void addToReport (
			@NonNull ChatReportRevShareRec report,
			@NonNull ChatUserCreditRec credit) {

		report

			.setCreditRev (
				report.getCreditRev ()
				+ credit.getBillAmount () * 100);

	}

	ChatReportRevShareRec getReport (
			@NonNull AffiliateRec affiliate) {

		ChatReportRevShareRec existingReport =
			chatReportsByAffiliate.get (
				affiliate);

		if (existingReport != null)
			return existingReport;

		Object affiliateParent =
			objectManager.getParent (
				affiliate);

		ChatReportRevShareRec newReport;

		if (affiliateParent instanceof ChatAffiliateRec) {

			ChatAffiliateRec chatAffiliate =
				(ChatAffiliateRec)
				affiliateParent;

			newReport =
				new ChatReportRevShareRec ()

				.setAffiliate (
					affiliate)

				.setPath (
					objectManager.objectPathMini (
						chatAffiliate,
						chat))

				.setDescription (
					chatAffiliate.getDescription ())

				.setCurrency (
					chat.getCurrency ());

		} else if (affiliateParent instanceof ChatSchemeRec) {

			ChatSchemeRec chatScheme =
				(ChatSchemeRec)
				affiliateParent;

			newReport =
				new ChatReportRevShareRec ()

				.setAffiliate (
					affiliate)

				.setPath (
					objectManager.objectPathMini (
						chatScheme,
						chat))

				.setDescription (
					chatScheme.getDescription ())

				.setCurrency (
					chat.getCurrency ());

		} else if (affiliateParent instanceof RootRec) {

			newReport =
				new ChatReportRevShareRec ()

				.setAffiliate (
					affiliate)

				.setPath (
					"system")

				.setDescription (
					"")

				.setCurrency (
					chat.getCurrency ());

		} else {

			throw new RuntimeException ();

		}

		chatReportsByAffiliate.put (
			affiliate,
			newReport);

		return newReport;

	}

	void addChatMessages () {

        ChatMonthCostRec chatMonthCost =
			chatMonthCostHelper.findByCode (
				chat,
				form.month ());

		if (chatMonthCost == null)
			return;

		List<ChatMessageRec> chatMessages =
			chatMessageHelper.search (
				new ChatMessageSearch ()

			.hasSender (
				true)

			.timestampAfter (
				startDate.toDateTimeAtStartOfDay ().toInstant ())

			.timestampBefore (
				startDate.toDateTimeAtStartOfDay ().toInstant ())

		);

		long staffCostPerMessage =
			chatMonthCost.getStaffCost ()
			/ chatMessages.size ();

		for (
			ChatMessageRec chatMessage
				: chatMessages
		) {

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					chatMessage.getToUser ());

			ChatReportRevShareRec affiliateReport =
				getReport (
					affiliate);

			affiliateReport.setStaffCost (
				affiliateReport.getStaffCost () + staffCostPerMessage);

			totalReport.setStaffCost (
				totalReport.getStaffCost () + staffCostPerMessage);

		}

	}

	@Override
	public
	void goBodyStuff () {

		goSearchForm ();
		goReport ();

	}

	void goSearchForm () {

		printFormat (
			"<form method=\"get\">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			out,
			searchFields,
			form);

		printFormat (
			"<tr>\n",
			"<th>Actions</th>\n",
			"<td><input",
			" type=\"submit\"",
			" value=\"search\"",
			"></td>\n",
			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

	void goReport () {

		printFormat (
			"<h2>Stats</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		// header

		printFormat (
			"<tr>");

		formFieldLogic.outputTableHeadings (
			out,
			resultsFields);

		printFormat (
			"</tr>\n");

		// row

		for (
			ChatReportRevShareRec chatReport
				: chatReportsSorted
		) {

			printFormat (
				"<tr>\n");

			formFieldLogic.outputTableCells (
				out,
			    resultsFields,
				chatReport,
				true);

		    printFormat (
			    "</tr>\n");

		}

		printFormat (
			"<tr>\n");

		// total

		printFormat (
			"<tr>\n");
			formFieldLogic.outputTableCells (
				out,
				resultsFields,
				totalReport,
				true);

		printFormat (
			"</tr>\n");

		printFormat (
			"</table>\n");

	}

	void goRates () {

		printFormat (
			"<h2>Rates</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"</table>\n");

	}

}