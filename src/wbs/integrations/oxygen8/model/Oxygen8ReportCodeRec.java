package wbs.integrations.oxygen8.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageStatus;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class Oxygen8ReportCodeRec
	implements MajorRecord<Oxygen8ReportCodeRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	Oxygen8ConfigRec oxygen8Config;

	@CodeField
	String code;

	// details

	@DescriptionField
	String description;

	@DeletedField
	Boolean deleted = false;

	// settings

	@SimpleField
	MessageStatus messageStatus;

	@SimpleField
	String additionalInformation;

	// compare to

	@Override
	public
	int compareTo (
			Record<Oxygen8ReportCodeRec> otherRecord) {

		Oxygen8ReportCodeRec other =
			(Oxygen8ReportCodeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getOxygen8Config (),
				other.getOxygen8Config ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}