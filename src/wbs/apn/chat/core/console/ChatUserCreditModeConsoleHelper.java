package wbs.apn.chat.core.console;

import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.EnumConsoleHelper;

@SingletonComponent ("chatUserCreditModeConsoleHelper")
public
class ChatUserCreditModeConsoleHelper
	extends EnumConsoleHelper<ChatUserCreditMode> {

	{

		enumClass (ChatUserCreditMode.class);

		add (ChatUserCreditMode.normal, "normal");
		add (ChatUserCreditMode.strict, "strict");
		add (ChatUserCreditMode.prePay, "pre-pay");
		add (ChatUserCreditMode.barred, "barred");
		add (ChatUserCreditMode.free, "free");

	}

}
