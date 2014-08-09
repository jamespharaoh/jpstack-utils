package wbs.apn.chat.infosite.api;

import java.io.IOException;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.PrintResponder;
import wbs.framework.web.RequestContext;

@PrototypeComponent ("chatInfoSiteMessageSentResponder")
public
class ChatInfoSiteMessageSentResponder
	extends PrintResponder {

	@Inject
	RequestContext requestContext;

	@Override
	protected
	void goHeaders ()
		throws IOException {

		requestContext.setHeader (
			"Content-Type",
			"text/html");

	}

	@Override
	protected
	void goContent ()
		throws IOException {

		printFormat (
			"<p>Your message has been sent.</p>\n");

	}

}
