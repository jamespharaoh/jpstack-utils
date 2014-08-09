package wbs.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import wbs.apn.chat.user.core.model.ChatUserNoteObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserNoteRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatUserNotesPart")
public
class ChatUserNotesPart
	extends AbstractPagePart {

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserNoteObjectHelper chatUserNoteHelper;

	@Inject
	TimeFormatter timeFormatter;

	List<ChatUserNoteRec> chatUserNotes;

	@Override
	public
	void prepare () {

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		chatUserNotes =
			chatUserNoteHelper.find (
				chatUser);

	}

	@Override
	public
	void goBodyStuff () {

		// create note

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.notes"),
			">\n");

		printFormat (
			"<h2>Create note</h2>\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",

			"<th>Note</th>\n",

			"<td>%s</td>\n",
			stringFormat (
				"<textarea name=\"note\">%h</textarea>",
				requestContext.getForm ("note")),

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"createNote\"",
			" value=\"create note\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		// note history

		printFormat (
			"<h2>Existing notes</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Timestamp</th>\n",
			"<th>Note</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		for (ChatUserNoteRec chatUserNote
				: chatUserNotes) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					chatUserNote.getTimestamp ()),

				"<td>%h</td> ",
				chatUserNote.getText ().getText (),

				"%s",
				consoleObjectManager.tdForObject (
					chatUserNote.getUser (),
					null,
					true,
					true),

				"</tr>\n");
		}

		printFormat (
			"</table>\n");

	}

}
