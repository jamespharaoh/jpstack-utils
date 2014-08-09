package wbs.smsapps.subscription.model;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class SubscriptionRec
	implements MajorRecord<SubscriptionRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description = "";

	@DeletedField
	Boolean deleted = false;

	// settings

	@ReferenceField (
		nullable = true)
	RouteRec freeRoute;

	@SimpleField
	String freeNumber = "";

	@ReferenceField (
		nullable = true)
	RouteRec billedRoute;

	@SimpleField
	String billedNumber = "";

	@SimpleField
	Integer numSubscribers = 0;

	// children

	@CollectionField
	Set<SubscriptionSubRec> subscriptionSubs =
		new LinkedHashSet<SubscriptionSubRec> ();

	@CollectionField
	Set<SubscriptionAffiliateRec> subscriptionAffiliates =
		new LinkedHashSet<SubscriptionAffiliateRec> ();

	// TODO remove these

	public
	void incNumSubscribers () {
		numSubscribers ++;
	}

	public
	void decNumSubscribers () {
		numSubscribers --;
	}

	// compare to

	@Override
	public
	int compareTo (
			Record<SubscriptionRec> otherRecord) {

		SubscriptionRec other =
			(SubscriptionRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSlice (),
				other.getSlice ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
