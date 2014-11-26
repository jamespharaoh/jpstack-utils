package wbs.platform.media.console;

import java.io.IOException;

import javax.inject.Inject;

import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.ConsoleResponder;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;

public abstract
class AbstractMediaImageResponder
	extends ConsoleResponder {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	MediaObjectHelper mediaHelper;

	@Inject
	MediaLogic mediaLogic;

	private
	byte[] data;

	private
	String mimeType;

	protected abstract
	byte[] getData (
			MediaRec media);

	protected abstract
	String getMimeType (
			MediaRec media);

	@Override
	protected
	void prepare () {

		int mediaId =
			requestContext.stuffInt ("mediaId");

		MediaRec media =
			mediaHelper.find (mediaId);

		data =
			getData (media);

		mimeType =
			getMimeType (media);

		transform ();

	}

	protected
	void transform () {

		String rotate =
			requestContext.parameter ("rotate");

		if ("90".equals (rotate)) {

			data =
				mediaLogic.writeImage (
					mediaLogic.rotateImage90 (
						mediaLogic.readImage (
							data,
							mimeType)),
					mimeType);

		} else if ("180".equals (rotate)) {

			data =
				mediaLogic.writeImage (
					mediaLogic.rotateImage180 (
						mediaLogic.readImage (
							data,
							mimeType)),
					mimeType);

		} else if ("270".equals (rotate)) {

			data =
				mediaLogic.writeImage (
					mediaLogic.rotateImage270 (
						mediaLogic.readImage (
							data,
							mimeType)),
					mimeType);

		}

	}

	@Override
	protected
	void goHeaders () {

		requestContext.setHeader (
			"Content-Type",
			mimeType);

		requestContext.setHeader (
			"Content-Length",
			Integer.toString (data.length));

	}

	@Override
	protected void goContent ()
		throws IOException {

		requestContext.outputStream ()
			.write (data);

	}

}