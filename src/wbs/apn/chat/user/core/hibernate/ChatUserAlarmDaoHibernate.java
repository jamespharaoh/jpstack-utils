package wbs.apn.chat.user.core.hibernate;

import java.util.Date;
import java.util.List;

import wbs.apn.chat.user.core.model.ChatUserAlarmDao;
import wbs.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserAlarmDaoHibernate
	extends HibernateDao
	implements ChatUserAlarmDao {

	@Override
	public
	List<ChatUserAlarmRec> findPending () {

		return findMany (
			ChatUserAlarmRec.class,

			createQuery (
				"FROM ChatUserAlarmRec cua " +
				"WHERE cua.alarmTime <= :now")

			.setTimestamp ("now", new Date ())

			.list ());

	}

	@Override
	public
	ChatUserAlarmRec find (
			ChatUserRec chatUser,
			ChatUserRec monitorChatUser) {

		return findOne (
			ChatUserAlarmRec.class,

			createQuery (
				"FROM ChatUserAlarmRec alarm " +
				"WHERE alarm.chatUser = :chatUser " +
				"AND alarm.monitorChatUser = :monitorChatUser")

			.setEntity (
				"chatUser",
				chatUser)

			.setEntity (
				"monitorChatUser",
				monitorChatUser)

			.list ());

	}

}
