package wbs.framework.entity.meta.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("record")
@PrototypeComponent ("recordSpec")
@ModelMetaData
public
class RecordSpec {

	@DataParent
	PluginSpec plugin;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String oldName;

	@DataAttribute (
		required = true)
	ModelMetaType type;

	@DataAttribute (
		name = "table")
	String tableName;

	@DataAttribute
	Boolean create;

	@DataAttribute
	Boolean mutable;

	// children

	@DataChildren (
		childrenElement = "fields")
	List <ModelFieldSpec> fields =
		new ArrayList<> ();

	@DataChildren (
		childrenElement = "collections")
	List <ModelCollectionSpec> collections =
		new ArrayList<> ();

	@DataChildren (
		childrenElement = "dao-interfaces")
	List <ModelInterfaceSpec> daoInterfaces =
		new ArrayList<> ();

	@DataChildren (
		childrenElement = "record-interfaces")
	List <ModelInterfaceSpec> recordInterfaces =
		new ArrayList<> ();

	@DataChildren (
		childrenElement = "object-helper-interfaces")
	List <ModelInterfaceSpec> objectHelperInterfaces =
		new ArrayList<> ();

	@DataChildren (
		direct = true,
		excludeChildren = { "fields", "collections" })
	List <Object> children =
		new ArrayList<> ();

}
