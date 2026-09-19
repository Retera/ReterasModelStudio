package com.hiveworkshop.wc3.pkb.vm;

/** A fatal VM or decoder error; the reference runtime stops the scope run on these. */
public class VmException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public VmException(final String message) {
		super(message);
	}
}
