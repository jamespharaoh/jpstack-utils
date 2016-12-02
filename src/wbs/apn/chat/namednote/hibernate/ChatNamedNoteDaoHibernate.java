package wbs.apn.chat.namednote.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.apn.chat.namednote.model.ChatNamedNoteDao;
import wbs.apn.chat.namednote.model.ChatNamedNoteRec;
import wbs.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;

@SingletonComponent ("chatNamedNoteDaoHibernate")
public
 class ChatNamedNoteDaoHibernate
	extends HibernateDao
	implements ChatNamedNoteDao {

	@Override
	public
	ChatNamedNoteRec find (
			@NonNull ChatUserRec thisChatUser,
			@NonNull ChatUserRec otherChatUser,
			@NonNull ChatNoteNameRec chatNoteName) {

		return findOneOrNull (
			"find (thisChatUser, otherChatUser, chatNoteName",
			ChatNamedNoteRec.class,

			createCriteria (
				ChatNamedNoteRec.class,
				"_chatNamedNote")

			.add (
				Restrictions.eq (
					"_chatNamedNote.thisUser",
					thisChatUser))

			.add (
				Restrictions.eq (
					"_chatNamedNote.otherUser",
					otherChatUser))

			.add (
				Restrictions.eq (
					"_chatNamedNote.chatNoteName",
					chatNoteName))

		);

	}

	@Override
	public
	List<ChatNamedNoteRec> find (
			@NonNull ChatUserRec thisChatUser,
			@NonNull ChatUserRec otherChatUser) {

		return findMany (
			"find (thisChatUser, otherChatUser)",
			ChatNamedNoteRec.class,

			createCriteria (
				ChatNamedNoteRec.class,
				"_chatNamedNote")

			.add (
				Restrictions.eq (
					"_chatNamedNote.thisUser",
					thisChatUser))

			.add (
				Restrictions.eq (
					"_chatNamedNote.otherUser",
					otherChatUser))

		);

	}

}
