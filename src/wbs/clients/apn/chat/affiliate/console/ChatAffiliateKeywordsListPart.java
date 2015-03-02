package wbs.clients.apn.chat.affiliate.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateObjectHelper;
import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeKeywordObjectHelper;
import wbs.clients.apn.chat.scheme.model.ChatSchemeKeywordRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatAffiliateKeywordsListPart")
public
class ChatAffiliateKeywordsListPart
	extends AbstractPagePart {

	@Inject
	ChatAffiliateObjectHelper chatAffiliateHelper;

	@Inject
	ChatSchemeKeywordObjectHelper chatSchemeKeywordHelper;

	List<ChatSchemeKeywordRec> chatSchemeKeywords;

	@Override
	public
	void prepare () {

		ChatAffiliateRec chatAffiliate =
			chatAffiliateHelper.find (
				requestContext.stuffInt ("chatAffiliateId"));

		chatSchemeKeywords =
			new ArrayList<ChatSchemeKeywordRec> (
				chatAffiliate.getChatSchemeKeywords ());

		Collections.sort (
			chatSchemeKeywords);

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Scheme</th>\n",
			"<th>Keyword</th>\n",
			"<th>Join type</th>\n",
			"<th>Gender</th>\n",
			"<th>Orient</th>\n",
			"</tr>\n");

		for (ChatSchemeKeywordRec chatSchemeKeyword
				: chatSchemeKeywords) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				chatSchemeKeyword.getChatScheme ().getCode (),

				"<td>%h</td>\n",
				chatSchemeKeyword.getKeyword (),

				"<td>%h</td>\n",
				chatSchemeKeyword.getJoinType (),

				"<td>%h</td>\n",
				chatSchemeKeyword.getJoinGender (),

				"<td>%h</td>\n",
				chatSchemeKeyword.getJoinOrient (),

				"</tr>\n");
		}

		printFormat (
			"</table>\n");

	}

}