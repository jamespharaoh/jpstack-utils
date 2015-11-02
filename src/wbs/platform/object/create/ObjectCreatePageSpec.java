package wbs.platform.object.create;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("object-create-page")
@PrototypeComponent ("objectCreatePageSpec")
@ConsoleModuleData
public
class ObjectCreatePageSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute
	String typeCode;

	@DataAttribute (
		name = "tab")
	String tabName;

	@DataAttribute
	String localFile;

	@DataAttribute (
		name = "responder")
	String responderName;

	@DataAttribute (
		name = "target-context-type")
	String targetContextTypeName;

	@DataAttribute (
		name = "target-responder")
	String targetResponderName;

	@DataAttribute (
		name = "fields")
	String fieldsName;

	@DataAttribute (
		name = "fields-provider")
	String fieldsProviderName;

	@DataAttribute (
		name = "create-time")
	String createTimeFieldName;

	@DataAttribute (
		name = "create-user")
	String createUserFieldName;

	@DataAttribute
	String createPrivDelegate;

	@DataAttribute
	String createPrivCode;

	@DataAttribute
	String privKey;

}
