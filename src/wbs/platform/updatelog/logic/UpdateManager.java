package wbs.platform.updatelog.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.updatelog.model.UpdateLogObjectHelper;
import wbs.platform.updatelog.model.UpdateLogRec;

/**
 * Provides an efficient signalling system which works using the database.
 * Signals are categorised according to a name, and a reference integer (eg a
 * hash code). To cause a signal use signalUpdate (...). To receive signals
 * create a Watcher using makeWatcher (...) then call isUpdated (...) to check
 * for changes. This always returns true the first time for a given ref.
 *
 * The idea is that you can cache large chunks of a table and, so long as all
 * updaters are using the signalling system, ensure that the cache is always
 * kept up to date.
 *
 * The underlying mechanism is efficient requiring (assuming no updates) only a
 * single database hit for a single row, every few seconds (this time period is
 * configurable). On update three rows must be looked up and updated/created,
 * then an extra hit is needed for each named category being watched and every
 * unique ref in the affected named category being watched.
 *
 * TODO is this really the best way to do this?
 */
@Log4j
@SingletonComponent ("updateManager")
public
class UpdateManager {

	@Inject
	Database database;

	@Inject
	UpdateLogObjectHelper updateLogHelper;

	@Getter @Setter
	long intervalMs = 3000;

	long reloadTime = 0;

	int masterVersion = -2;

	Map<String,UpdateStuff> secondaryVersions =
		new HashMap<String,UpdateStuff> ();

	Map<String,Map<Integer,UpdateStuff>> tertiaryVersions =
		new HashMap<String,Map<Integer,UpdateStuff>> ();

	int getDatabaseVersion (
			String table,
			int ref) {

		UpdateLogRec updateLog =
			updateLogHelper.findByTableAndRef (
				table,
				ref);

		int ret =
			updateLog != null
				? updateLog.getVersion ()
				: -1;

		log.debug (
			stringFormat (
				"getDatabaseVersion (\"%s\", %d) = %d",
				table,
				ref,
				ret));

		return ret;

	}

	void refreshMaster () {

		// check it is time

		long now =
			System.currentTimeMillis ();

		if (now < reloadTime)
			return;

		reloadTime =
			+ now
			+ intervalMs;

		// hit the db

		int newMasterVersion =
			getDatabaseVersion (
				"master",
				0);

		// if the version hasn't changed don't bother

		if (equal (
				masterVersion,
				newMasterVersion))
			return;

		// remember the new version

		masterVersion =
			newMasterVersion;

		// markall secondary versions as dirty

		for (UpdateStuff updateStuff
				: secondaryVersions.values ()) {

			updateStuff.dirty = true;

		}

	}

	void refreshSecondary (
			String table) {

		// if it isn't marked dirty don't bother

		UpdateStuff stuff1 =
			secondaryVersions.get (table);

		if (stuff1 == null) {

			stuff1 =
				new UpdateStuff ();

			stuff1.version = -2;
			stuff1.dirty = true;

			secondaryVersions.put (
				table,
				stuff1);

		}

		if (! stuff1.dirty)
			return;

		stuff1.dirty = false;

		// hit the db

		int newSecondaryVersion =
			getDatabaseVersion (
				table,
				0);

		// if the version hasn't changed don't bother

		if (equal (
				stuff1.version,
				newSecondaryVersion))
			return;

		// remember the new version

		stuff1.version =
			newSecondaryVersion;

		// mark all this table's tertiary versions as dirty

		Map<Integer,UpdateStuff> map =
			tertiaryVersions.get (table);

		if (map == null)
			return;

		for (UpdateStuff stuff2 : map.values ())
			stuff2.dirty = true;

	}

	public
	int refreshTertiary (
			String table,
			int ref) {

		// if it's not dirty don't bother

		Map<Integer,UpdateStuff> map =
			tertiaryVersions.get (table);

		if (map == null) {

			map =
				new HashMap<Integer,UpdateStuff> ();

			tertiaryVersions.put (
				table,
				map);

		}

		UpdateStuff stuff =
			map.get (ref);

		if (stuff == null) {

			stuff =
				new UpdateStuff ();

			stuff.version = -2;
			stuff.dirty = true;

			map.put (
				ref,
				stuff);

		}

		if (! stuff.dirty)
			return stuff.version;

		stuff.dirty = false;

		// hit the db

		int newTertiaryVersion =
			getDatabaseVersion (
				table,
				ref);

		// if the version hasn't changed don't bother

		if (equal (
				stuff.version,
				newTertiaryVersion))
			return stuff.version;

		// remember the new version

		return stuff.version =
			newTertiaryVersion;

	}

	public
	int getVersionDb (
			String table,
			int ref) {

		@Cleanup Transaction transaction =
			database.beginReadOnly ();

		refreshMaster ();

		refreshSecondary (
			table);

		refreshTertiary (
			table,
			ref);

		Map<Integer,UpdateStuff> map =
			tertiaryVersions.get (table);

		UpdateStuff stuff2 =
			map.get (ref);

		return stuff2.version;

	}

	public synchronized
	int getVersion (
			String table,
			int ref) {

		// check master

		long now =
			System.currentTimeMillis ();

		if (reloadTime < now) {

			return getVersionDb (
				table,
				ref);

		}

		// check secondary

		UpdateStuff stuff1 =
			secondaryVersions.get (
				table);

		if (stuff1 == null || stuff1.dirty) {

			return getVersionDb (
				table,
				ref);

		}

		// check tertiary

		Map<Integer, UpdateStuff> map =
			tertiaryVersions.get (table);

		if (map == null)
			return getVersionDb (
				table,
				ref);

		UpdateStuff stuff2 =
			map.get (ref);

		if (stuff2 == null || stuff2.dirty) {

			return getVersionDb (
				table,
				ref);

		}

		return stuff2.version;

	}

	void realSignalUpdate (
			String table,
			int ref) {

		UpdateLogRec updateLog =
			updateLogHelper.findByTableAndRef (
				table,
				ref);

		if (updateLog == null) {

			updateLog =
				updateLogHelper.insert (
					new UpdateLogRec ()
						.setCode (table)
						.setRef (ref)
						.setVersion (0));

		}

		updateLog.setVersion (
			updateLog.getVersion () + 1);

	}

	public
	void signalUpdate (
			String table,
			int ref) {

		realSignalUpdate (
			table,
			ref);

		realSignalUpdate (
			table,
			0);

		realSignalUpdate (
			"master",
			0);

	}

	public
	Watcher makeWatcher (
			String table) {

		return new Watcher (
			table);

	}

	static
	class UpdateStuff {

		int version;
		boolean dirty;

	}

	public
	class Watcher {

		String table;

		Map<Integer,Integer> versions =
			new HashMap<Integer,Integer> ();

		Watcher (
				String newTable) {

			table = newTable;

		}

		public
		boolean isUpdated (
				int ref) {

			log.debug (
				"Watcher (\"" + table + "\").isUpdated (" + ref + ")");

			int newVersion =
				getVersion (
					table,
					ref);

			if (versions.containsKey (ref)) {

				int oldVersion =
					versions.get (ref);

				if (oldVersion == newVersion)
					return false;

			}

			versions.put (
				ref,
				newVersion);

			return true;

		}

	}

	public <T>
	UpdateGetter<T> makeUpdateGetterAdaptor (
			Provider<? extends T> getter,
			long reloadTimeMs,
			String table,
			int ref) {

		return new UpdateGetterAdaptor<T> (
			getter,
			reloadTimeMs,
			table,
			ref);

	}

	public static
	interface UpdateGetter<T>
		extends Provider<T> {

		void forceUpdate ();

	}

	private
	class UpdateGetterAdaptor<T>
		implements UpdateGetter<T> {

		Provider<? extends T> getter;
		long reloadTimeMs;
		String table;
		int ref;

		boolean forceUpdate = false;

		@Override
		public synchronized
		void forceUpdate () {
			forceUpdate = true;
		}

		public
		UpdateGetterAdaptor (
				Provider<? extends T> newGetter,
				long newReloadTimeMs,
				String newTable,
				int newRef) {

			getter = newGetter;
			reloadTimeMs = newReloadTimeMs;
			table = newTable;
			ref = newRef;

		}

		T value;
		long lastReload = 0;
		int oldVersion = -1;

		@Override
		public synchronized
		T get () {

			long now =
				System.currentTimeMillis ();

			// check for a forced update

			if (forceUpdate) {

				value =
					getter.get ();

				lastReload = now;
				forceUpdate = false;

				return value;

			}

			// check for an update trigger

			int newVersion =
				getVersion (
					table,
					ref);

			if (oldVersion != newVersion) {

				value =
					getter.get ();

				lastReload = now;
				oldVersion = newVersion;

				return value;

			}

			// if not check for a timed update

			if (lastReload + reloadTimeMs < now) {

				value =
					getter.get ();

				lastReload = now;
				oldVersion = newVersion;

				return value;

			}

			// or just return the cached value

			return value;

		}

	}

}