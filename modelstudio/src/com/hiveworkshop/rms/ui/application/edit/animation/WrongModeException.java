package com.hiveworkshop.rms.ui.application.edit.animation;

public class WrongModeException extends RuntimeException {

	public WrongModeException() {
		super();
	}

	public WrongModeException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public WrongModeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public WrongModeException(final String message) {
		super(message);
	}

	public WrongModeException(final Throwable cause) {
		super(cause);
	}

}
