package wbs.platform.object.settings;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.lookup.ObjectLookup;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;
import wbs.framework.web.Responder;
import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("objectSettingsAction")
public
class ObjectSettingsAction <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	ObjectLookup <ObjectType> objectLookup;

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	Provider <Responder> detailsResponder;

	@Getter @Setter
	Provider <Responder> accessDeniedResponder;

	@Getter @Setter
	String editPrivKey;

	@Getter @Setter
	String objectRefName;

	@Getter @Setter
	String objectType;

	@Getter @Setter
	FormFieldSet <ObjectType> formFieldSet;

	@Getter @Setter
	FieldsProvider <ObjectType, ParentType> formFieldsProvider;

	// state

	ObjectType object;
	ParentType parent;

	// details

	@Override
	public
	Responder backupResponder () {
		return detailsResponder.get ();
	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		// check access

		if (! requestContext.canContext (editPrivKey)) {

			requestContext.addError (
				"Access denied");

			return accessDeniedResponder
				.get ();

		}

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ObjectSettingsAction.goReal ()",
				this);

		object =
			objectLookup.lookupObject (
				requestContext.contextStuff ());

		// perform update

		if (formFieldsProvider != null) {

			prepareParent ();

			prepareFieldSet ();

		}

		UpdateResultSet updateResultSet =
			formFieldLogic.update (
				requestContext,
				formFieldSet,
				object,
				ImmutableMap.of (),
				"settings");

		if (updateResultSet.errorCount () > 0) {

			formFieldLogic.reportErrors (
				requestContext,
				updateResultSet,
				"settings");

			requestContext.request (
				"objectSettingsUpdateResultSet",
				updateResultSet);

			return null;

		}

		if (updateResultSet.updateCount () == 0) {

			requestContext.addWarning (
				"No changes made");

			return null;

		}

		// create events

		if (object instanceof PermanentRecord) {

			formFieldLogic.runUpdateHooks (
				formFieldSet,
				updateResultSet,
				object,
				(PermanentRecord <?>) object,
				optionalAbsent (),
				optionalAbsent (),
				"settings");

		} else {

			PermanentRecord <?> linkObject =
				(PermanentRecord <?>)
				objectManager.getParent (
					object);

			Object objectRef =
				PropertyUtils.getProperty (
					object,
					objectRefName);

			formFieldLogic.runUpdateHooks (
				formFieldSet,
				updateResultSet,
				object,
				linkObject,
				Optional.of (
					objectRef),
				Optional.of (
					objectType),
				"settings");

		}

		// commit

		transaction.commit ();

		requestContext.addNotice (
			"Details updated");

		return detailsResponder.get ();

	}

	void prepareParent () {

		@SuppressWarnings ("unchecked")
		ConsoleHelper <ParentType> parentHelper =
			(ConsoleHelper <ParentType>)
			objectManager.findConsoleHelper (
				consoleHelper.parentClass ());

		if (parentHelper.isRoot ()) {

			parent =
				parentHelper.findRequired (
					0l);

			return;

		}

		Long parentId =
			requestContext.stuffInteger (
				parentHelper.idKey ());

		if (parentId != null) {

			// use specific parent

			parent =
				parentHelper.findRequired (
					parentId);

			return;

		}

	}

	void prepareFieldSet () {

		formFieldSet =
			formFieldsProvider.getFieldsForObject (
				object);

	}

}
