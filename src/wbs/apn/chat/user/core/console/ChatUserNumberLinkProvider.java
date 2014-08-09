package wbs.apn.chat.user.core.console;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.priv.console.PrivChecker;
import wbs.sms.number.core.console.NumberPlugin;
import wbs.sms.number.core.model.NumberRec;

import com.google.common.collect.ImmutableMap;

@SingletonComponent ("chatUserNumberLinkProvider")
public
class ChatUserNumberLinkProvider
	implements NumberPlugin {

	@Inject
	ChatUserDao chatUserDao;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	PrivChecker privChecker;

	@Override
	public
	String getName () {
		return "chatUser";
	}

	@Override
	public
	List<Link> findLinks (
			NumberRec number,
			boolean active) {

		// find relevant subs

		Map<String,Object> searchMap =
			ImmutableMap.<String,Object>builder ()

				.put (
					"numberId",
					number.getId ())

				.build ();

		List<Integer> chatUserIds =
			chatUserHelper.searchIds (
				searchMap);

		// create advices

		List<Link> advices =
			new ArrayList<Link> ();

		for (Integer chatUserId : chatUserIds) {

			final ChatUserRec chatUser =
				chatUserHelper.find (
					chatUserId);

			advices.add (
				new Link () {

				@Override
				public
				NumberPlugin getProvider () {
					return ChatUserNumberLinkProvider.this;
				}

				@Override
				public
				NumberRec getNumber () {
					return chatUser.getNumber ();
				}

				@Override
				public
				Boolean getActive () {
					return !chatUser.getBlockAll ();
				}

				@Override
				public
				Date getStartTime () {
					return chatUser.getFirstJoin ();
				}

				@Override
				public
				Date getEndTime () {
					return null;
				}

				@Override
				public
				Record<?> getParentObject () {
					return chatUser.getChat ();
				}

				@Override
				public
				Record<?> getSubscriptionObject () {
					return chatUser;
				}

				@Override
				public
				String getType () {
					return "registered chat user";
				}

				@Override
				public
				boolean canView () {

					return privChecker.can (
						chatUser.getChat (),
						"user_create",
						"chat_user_view",
						"user_admin",
						"user_credit");

				}

			});

			if (chatUser.getOnline ()) {

				advices.add (
					new Link () {

					@Override
					public
					NumberPlugin getProvider () {
						return ChatUserNumberLinkProvider.this;
					}

					@Override
					public
					NumberRec getNumber () {
						return chatUser.getNumber ();
					}

					@Override
					public
					Boolean getActive () {
						return ! chatUser.getBlockAll ();
					}

					@Override
					public
					Date getStartTime () {
						return chatUser.getLastJoin ();
					}

					@Override
					public
					Date getEndTime () {
						return null;
					}

					@Override
					public
					Record<?> getParentObject () {
						return chatUser.getChat ();
					}

					@Override
					public
					Record<?> getSubscriptionObject () {
						return chatUser;
					}

					@Override
					public
					String getType () {
						return "online chat user";
					}

					@Override
					public
					boolean canView () {

						return privChecker.can (
							chatUser.getChat (),
							"user_create",
							"user_view",
							"user_admin",
							"user_credit");

					}

				});

			}

			if (chatUser.getDateMode() != ChatUserDateMode.none) {

				advices.add (
					new Link () {

					@Override
					public
					NumberPlugin getProvider () {
						return ChatUserNumberLinkProvider.this;
					}

					@Override
					public
					NumberRec getNumber () {
						return chatUser.getNumber ();
					}

					@Override
					public
					Boolean getActive () {
						return true;
					}

					@Override
					public
					Date getStartTime () {
						return chatUser.getLastJoin ();
					}

					@Override
					public
					Date getEndTime () {
						return null;
					}

					@Override
					public
					Record<?> getParentObject () {
						return chatUser.getChat ();
					}

					@Override
					public
					Record<?> getSubscriptionObject () {
						return chatUser;
					}

					@Override
					public
					String getType () {

						switch (chatUser.getDateMode ()) {

						case text:
							return "text dating user";

						case photo:
							return "photo dating user";

						default:
							throw new RuntimeException (
								chatUser.getDateMode ().toString ());

						}

					}

					@Override
					public
					boolean canView () {

						return privChecker.can (
							chatUser.getChat (),
							"user_create",
							"user_view",
							"user_admin",
							"user_credit");

					}

				});

			}

		}

		return advices;

	}

}
