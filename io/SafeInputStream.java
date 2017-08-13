package wbs.utils.io;

import java.io.IOException;
import java.io.InputStream;

import lombok.NonNull;

import wbs.utils.etc.SafeCloseable;

public
class SafeInputStream
	extends InputStream
	implements SafeCloseable {

	// state

	private final
	InputStream delegate;

	// constructor

	public
	SafeInputStream (
			@NonNull InputStream delegate) {

		this.delegate =
			delegate;

	}

	// input stream implementation

	@Override
	public
	int read () {

		try {

			return delegate.read ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

    @Override
	public
    int read (
    		@NonNull byte buffer []) {

		try {

			return delegate.read (
				buffer);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

    }

    @Override
	public
    int read (
    		@NonNull byte[] buffer,
    		int offset,
    		int length) {

		try {

			return delegate.read (
				buffer,
				offset,
				length);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

    }

    @Override
	public
    long skip (
    		long length) {

		try {

			return delegate.skip (
				length);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

    }

    @Override
	public
    int available () {

		try {

			return delegate.available ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

    }

    @Override
	public synchronized
    void mark (
    		int readlimit) {

		delegate.mark (
			readlimit);

    }

    @Override
	public synchronized
    void reset () {

		try {

			delegate.reset ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

    }

	@Override
    public
    boolean markSupported () {

		return delegate.markSupported ();

    }

	@Override
	public
	void close () {

		try {

			delegate.close ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
