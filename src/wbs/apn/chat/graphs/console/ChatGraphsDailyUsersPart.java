package wbs.apn.chat.graphs.console;

import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("chatGraphsDailyUsersPart")
public
class ChatGraphsDailyUsersPart
	extends AbstractMonthlyGraphPart {

	{

		myLocalPart (
			"/chat.graphs.dailyUsers");

		imageLocalPart (
			"/chat.graphs.dailyUsersImage");

	}

}
