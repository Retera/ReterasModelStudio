package com.hiveworkshop.rms.filesystem.util;

public class CodebaseReadOnlyException extends Exception {

	/**
	 * default ID
	 */
	private static final long serialVersionUID = 1L;

	public CodebaseReadOnlyException(final String msg) {
		super(msg);
	}
}
