package wbs.clients.apn.chat.contact.hibernate;

import java.sql.Types;

import wbs.clients.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.framework.hibernate.EnumUserType;

public
class ChatUserInitiationReasonType
	extends EnumUserType<String,ChatUserInitiationReason> {

	{

		sqlType (Types.VARCHAR);
		enumClass (ChatUserInitiationReason.class);

		add ("join", ChatUserInitiationReason.joinUser);
		add ("quiet", ChatUserInitiationReason.quietUser);
		add ("alarm", ChatUserInitiationReason.alarm);
		add ("alarm-set", ChatUserInitiationReason.alarmSet);
		add ("alarm-cancel", ChatUserInitiationReason.alarmCancel);
		add ("alarm-ignore", ChatUserInitiationReason.alarmIgnore);

	}

}