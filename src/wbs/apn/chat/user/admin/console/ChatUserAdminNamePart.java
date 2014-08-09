package wbs.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.ifNull;

import javax.inject.Inject;

import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.model.ChatUserNameRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatUserAdminNamePart")
public
class ChatUserAdminNamePart
	extends AbstractPagePart {

	@Inject
	ChatConsoleLogic chatConsoleLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	TimeFormatter timeFormatter;

	ChatUserRec chatUser;

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.admin.name"),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"name\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("name"),
				chatUser.getName (),
				""),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Reason</th>\n",

			"<td>%s</td>\n",
			chatConsoleLogic.selectForChatUserEditReason (
				"editReason",
				requestContext.getForm ("editReason")),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Action</th>\n",

			"<td><input",
			" type=\"submit\"",
			" value=\"update name\"",
			"></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<h2>History</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Timestamp</th>\n",
			"<th>Original</th>\n",
			"<th>Edited</th>\n",
			"<th>Status</th>\n",
			"<th>Reason</th>\n",
			"<th>Moderator</th>\n",
			"</tr>\n");

		for (ChatUserNameRec chatUserName
				: chatUser.getChatUserNames ()) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					dateToInstant (
						chatUserName.getCreationTime ())),

				"<td>%h</td>\n",
				chatUserName.getOriginalName (),

				"<td>%h</td>\n",
				chatUserName.getEditedName (),

				"<td>%h</td>\n",
				chatConsoleLogic.textForChatUserInfoStatus (
					chatUserName.getStatus ()),

				"<td>%h</td>\n",
				chatConsoleLogic.textForChatUserEditReason (
					chatUserName.getEditReason ()),

				"%s\n",
				objectManager.tdForObject (
					chatUserName.getModerator (),
					null,
					true,
					true),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
