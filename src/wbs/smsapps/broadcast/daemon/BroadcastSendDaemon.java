package wbs.smsapps.broadcast.daemon;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.send.GenericSendDaemon;
import wbs.platform.send.GenericSendHelper;
import wbs.smsapps.broadcast.logic.BroadcastSendHelper;
import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastRec;

@SingletonComponent ("broadcastSendDaemon")
public
class BroadcastSendDaemon
	extends
		GenericSendDaemon<
			BroadcastConfigRec,
			BroadcastRec,
			BroadcastNumberRec
		> {

	// dependencies

	@Inject
	BroadcastSendHelper broadcastSendHelper;

	// implementation

	@Override
	protected
	GenericSendHelper<
		BroadcastConfigRec,
		BroadcastRec,
		BroadcastNumberRec
	> helper () {

		return broadcastSendHelper;

	}

}