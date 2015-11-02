package wbs.platform.object.search;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import com.google.common.collect.ImmutableSet;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.html.JqueryScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchPart")
public
class ObjectSearchPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	FormFieldLogic formFieldLogic;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	Class<?> searchClass;

	@Getter @Setter
	String sessionKey;

	@Getter @Setter
	FormFieldSet formFieldSet;

	// state

	Object search;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.build ();

	}

	// implementation

	@Override
	@SneakyThrows ({
		IllegalAccessException.class,
		InstantiationException.class
	})
	public
	void prepare () {

		search =
			requestContext.session (
				sessionKey + "Fields");

		if (search == null) {

			search =
				searchClass.newInstance ();

			requestContext.session (
				sessionKey + "Fields",
				search);

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				stringFormat (
					"/%s.search",
					consoleHelper.objectName ())),
			">\n");

		printFormat (
			"<table",
			" class=\"details\"",
			">\n");

		formFieldLogic.outputFormRows (
			formatWriter,
			formFieldSet,
			search);

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"search\"",
			">\n");

		printFormat (
			"<input",
			" type=\"button\"",
			" value=\"reset form\"",
			" onclick=\"resetSearchForm (); return false;\"",
			">\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<script type=\"text/javascript\">\n");

		printFormat (
			"\tfunction resetSearchForm () {\n");

		formFieldLogic.outputFormReset (
			formatWriter,
			"\t\t",
			formFieldSet,
			search);

		printFormat (
			"\t}\n");

		printFormat (
			"</script>\n");

	}

}
