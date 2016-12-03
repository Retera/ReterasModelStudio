package com.hiveworkshop.wc3.mpq;

public class CodebaseReadOnlyException extends Exception {

	/**
	 * default ID
	 */
	private static final long serialVersionUID = 1L;
	public CodebaseReadOnlyException(String msg) {
		super(msg);
	}
}
