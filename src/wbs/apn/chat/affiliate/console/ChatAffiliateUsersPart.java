package wbs.apn.chat.affiliate.console;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.apn.chat.affiliate.model.ChatAffiliateObjectHelper;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatAffiliateUsersPart")
public
class ChatAffiliateUsersPart
	extends AbstractPagePart {

	@Inject
	ChatAffiliateObjectHelper chatAffiliateHelper;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	List<ChatUserRec> chatUsers;

	@Override
	public
	void prepare () {

		ChatAffiliateRec chatAffiliate =
			chatAffiliateHelper.find (
				requestContext.stuffInt ("chatAffiliateId"));

		chatUsers =
			chatUserHelper.find (
				chatAffiliate);

		Collections.sort (
			chatUsers);

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"<th>Name</th>\n",
			"<th>Info</th>\n",
			"<th>Online</th>\n",
			"</tr>\n");

		for (ChatUserRec chatUser
				: chatUsers) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				chatUser.getCode (),

				"<td>%h</td>\n",
				chatUser.getName (),

				"<td>%h</td>\n",
				chatUser.getInfoText (),

				"<td>%h</td>\n",
				chatUser.getOnline () ? "yes" : "no",

				"</tr>");

		}

		printFormat (
			"</table>\n");

	}

}
