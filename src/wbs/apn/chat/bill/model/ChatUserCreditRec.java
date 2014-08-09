package wbs.apn.chat.bill.model;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Interval;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatUserCreditRec
	implements CommonRecord<ChatUserCreditRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatUserRec chatUser;

	// TODO index?

	// details

	@SimpleField (
		column = "amount")
	Integer creditAmount;

	@SimpleField
	Integer billAmount;

	@SimpleField
	Date timestamp = new Date ();

	@ReferenceField
	UserRec user;

	@SimpleField
	Boolean gift;

	@SimpleField
	String details;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatUserCreditRec> otherRecord) {

		ChatUserCreditRec other =
			(ChatUserCreditRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public static
	interface ChatUserCreditDaoMethods {

		List<ChatUserCreditRec> findByTimestamp (
				ChatRec chat,
				Interval timestampInterval);

	}

}
