package wbs.platform.core.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lombok.Cleanup;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.user.logic.UserLogic;
import wbs.platform.user.model.UserOnlineObjectHelper;
import wbs.platform.user.model.UserOnlineRec;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("coreAuthFilter")
public
class CoreAuthFilter
	implements Filter {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Provider<ConsoleManager> consoleManagerProvider;

	@Inject
	Database database;

	@Inject
	PrivChecker userPrivChecker;

	@Inject
	UserLogic userLogic;

	@Inject
	UserOnlineObjectHelper userOnlineHelper;

	final static
	int reloadTime = 10 * 1000;

	final static
	int logoffTime = 60 * 1000;

	long lastReload = 0;

	Map<Integer,String> onlineSessionIdsByUserId;

	Map<String,Date> activeSessions =
		new HashMap<String,Date> ();

	private synchronized
	void reload () {

		Date now =
			new Date ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		onlineSessionIdsByUserId =
			new HashMap<Integer,String> ();

		for (UserOnlineRec online
				: userOnlineHelper.findAll ()) {

			UserRec user =
				online.getUser ();

			// update his timestamp if appropriate

			if (activeSessions.containsKey (
					online.getSessionId ()))

				online.setTimestamp (
					activeSessions.get (
						online.getSessionId ()));

			// check if he has been disabled or timed out

			if (! user.getActive ()
					|| online .getTimestamp ().getTime () + logoffTime
						< now.getTime ()) {

				userLogic.userLogoff (
					user);

				continue;

			}

			// ok put him in the ok list

			onlineSessionIdsByUserId.put (
				user.getId (),
				online.getSessionId ());

		}

		transaction.commit ();

		activeSessions =
			new HashMap<String,Date> ();

	}

	/**
	 * Performs the main authorisation. Calls reload () to refresh caches if
	 * necessary then checks the user's session against them. Also adds us to an
	 * "active" list which updates our user record with a timestamp
	 * automatically when the caches are updated.
	 */
	private synchronized boolean checkUser () {

		// check there is a user id

		if (requestContext.userId () == null)
			return false;

		// reload if overdue or not done yet, always for root path

		Date now =
			new Date ();

		boolean reloaded = false;

		if (onlineSessionIdsByUserId == null
				|| lastReload + reloadTime < now.getTime ()
				|| requestContext.servletPath ().equals ("/")) {

			reload ();

			lastReload =
				now.getTime ();

			reloaded = true;

		}

		// check his session is valid, reload if it doesn't look right still

		if (! equal (
				onlineSessionIdsByUserId.get (
					requestContext.userId ()),
				requestContext.sessionId ())) {

			if (reloaded)
				return false;

			reload ();

			lastReload =
				now.getTime ();

			if (! equal (
					onlineSessionIdsByUserId.get (
						requestContext.userId ()),
					requestContext.sessionId ()))
				return false;

		}

		// update his timestamp next time round

		activeSessions.put (
			requestContext.sessionId (),
			now);

		return true;

	}

	@Override
	public
	void doFilter (
			ServletRequest request,
			ServletResponse response,
			FilterChain chain)
		throws
			ServletException,
			IOException {

		String path =
			requestContext.servletPath ();

		// check the user is ok

		boolean userOk =
			checkUser ();

		if (userOk) {

			// and show the page

			chain.doFilter (
				request,
				response);

		} else {

			// user not ok, either....

			if (path.equals ("/")) {

				// root path, either show logon page or process logon request

				if (requestContext.post ()) {

					chain.doFilter (
						request,
						response);

				} else {

					Provider<Responder> logonResponder =
						consoleManagerProvider.get ().responder (
							"coreLogonResponder",
							true);

					logonResponder
						.get ()
						.execute ();

				}

			} else if (in (path,
					"/style/basic.css",
					"/favicon.ico",
					"/status.update")) {

				// these paths are available before login

				chain.doFilter (
					request,
					response);

			} else {

				// unauthorised access, redirect to the logon page

				requestContext.sendRedirect (
					requestContext.resolveApplicationUrl (
						"/"));

			}

		}

	}

	@Override
	public
	void destroy () {
	}

	@Override
	public
	void init (
			FilterConfig filterConfig)
		throws ServletException {

	}

}