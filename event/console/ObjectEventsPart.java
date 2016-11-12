package wbs.platform.event.console;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.platform.event.model.EventLinkObjectHelper;
import wbs.platform.event.model.EventRec;
import wbs.platform.user.console.UserConsoleLogic;

@Accessors (fluent = true)
@PrototypeComponent ("objectEventsPart")
public
class ObjectEventsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	EventConsoleLogic eventConsoleLogic;

	@SingletonDependency
	EventLinkObjectHelper eventLinkHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	Collection <GlobalId> dataObjectIds;

	// state

	List <EventRec> events;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		events =
			dataObjectIds.stream ()

			.map (
				objectId ->
					eventLinkHelper.findByTypeAndRef (
						objectId.typeId (),
						objectId.objectId ()))

			.flatMap (
				eventLinks ->
					eventLinks.stream ())

			.map (
				eventLink ->
					eventLink.getEvent ())

			.sorted ()

			.collect (
				Collectors.toList ());

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		eventConsoleLogic.writeEventsTable (
			taskLogger,
			formatWriter,
			events);

	}

}
