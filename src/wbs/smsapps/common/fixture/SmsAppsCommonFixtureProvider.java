package wbs.smsapps.common.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("smsAppsCommonFixtureProvider")
public
class SmsAppsCommonFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		menuGroupHelper.insert (
			menuGroupHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"facility")

			.setName (
				"Facility")

			.setDescription (
				"")

			.setLabel (
				"Facilities")

			.setOrder (
				20l)

		);

	}

}
