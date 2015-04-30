package wbs.services.messagetemplate.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.Record;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.forms.TextFormFieldSpec;
import wbs.platform.console.module.ConsoleModuleBuilder;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateTypeObjectHelper;
import wbs.services.messagetemplate.model.MessageTemplateTypeRec;
import wbs.services.ticket.core.console.FieldsProvider;
import wbs.services.ticket.core.console.TicketFieldsProvider;

@PrototypeComponent ("messageTemplateSetFieldsProvider")
public
class MessageTemplateSetFieldsProvider 
	implements FieldsProvider {
	
	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;
	
	@Inject
	MessageTemplateTypeObjectHelper messageTemplateTypeHelper;
	
	@Inject
	MessageTemplateTypeConsoleHelper messageTemplateTypeConsoleHelper;
	
	@Inject
	MessageTemplateSetConsoleHelper messageTemplateSetConsoleHelper;
	
	FormFieldSet formFields;
	
	String mode;

	@Override
	public 
	FormFieldSet getFields(Record<?> parent) {
		
		List<Object> formFieldSpecs =
			new ArrayList<Object> ();
		
		// add name and description fields
		
		formFieldSpecs
		.add (new TextFormFieldSpec()
			.name ("name")					
			.label ("Name")
			.dynamic (false));
		
		formFieldSpecs
		.add (new TextFormFieldSpec()
			.name ("description")					
			.label ("Description")
			.dynamic (false));
			
		// retrieve existing message template types
		
		MessageTemplateDatabaseRec messageTemplateDatabase =
			(MessageTemplateDatabaseRec) parent;
		
		Set<MessageTemplateTypeRec> messageTemplateTypes =
				messageTemplateDatabase.getMessageTemplateTypes();
		
		// build dynamic form fields
		
		for (MessageTemplateTypeRec messageTemplateType : messageTemplateTypes) {
			
			if (mode == "list") { continue; }
								
			formFieldSpecs
				.add (new TextFormFieldSpec()
					.name (messageTemplateType.getCode ())					
					.label (messageTemplateType.getName ())
					.dynamic (true));				

		}
		
		// build field set
		
		String fieldSetName =
			stringFormat (
				"%s.%s",
				messageTemplateSetConsoleHelper.objectName(), 
				mode);
		
		return consoleModuleBuilder.buildFormFieldSet (
			messageTemplateSetConsoleHelper,
			fieldSetName,
			formFieldSpecs);
	
	}

	@Override
	public FieldsProvider setFields(FormFieldSet fields) {
		
		formFields = fields;	
		return this;
		
	}
	
	@Override
	public FieldsProvider setMode (String modeSet) {
		
		mode = modeSet;	
		return this;
		
	}

	@SingletonComponent("messageTemplateSetFieldsProviderConfig")
	public static
	class Config {

		@Inject
		Provider<TicketFieldsProvider> messageTemplateSetFieldsProvider;
		
		@PrototypeComponent ("messageTemplateSetListFieldsProvider")
		public
		FieldsProvider messageTemplateSetListFieldsProvider () {

			return messageTemplateSetFieldsProvider.get ()
				.setMode ("list");

		}
		
		@PrototypeComponent ("messageTemplateSetCreateFieldsProvider")
		public
		FieldsProvider messageTemplateSetCreateFieldsProvider () {

			return messageTemplateSetFieldsProvider.get ()
				.setMode ("create");

		}
		
		@PrototypeComponent ("messageTemplateSetSettingsFieldsProvider")
		public
		FieldsProvider messageTemplateSetSettingsFieldsProvider () {

			return messageTemplateSetFieldsProvider.get ()
				.setMode ("settings");

		}
		
		@PrototypeComponent ("messageTemplateSetSummaryFieldsProvider")
		public
		FieldsProvider messageTemplateSetSummaryFieldsProvider () {

			return messageTemplateSetFieldsProvider.get ()
				.setMode ("summary");

		}

	}

}
