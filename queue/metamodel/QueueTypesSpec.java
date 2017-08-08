package wbs.platform.queue.metamodel;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("queue-types")
@PrototypeComponent ("queueTypesSpec")
@ModelMetaData
public
class QueueTypesSpec {

	@DataChildren (
		direct = true)
	List<QueueTypeSpec> queueTypes =
		new ArrayList<QueueTypeSpec> ();

}
