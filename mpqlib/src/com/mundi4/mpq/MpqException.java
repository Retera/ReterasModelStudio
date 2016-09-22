package com.mundi4.mpq;

import java.io.IOException;

@SuppressWarnings("serial")
public class MpqException extends IOException {

    public MpqException() {
	super();
    }

    public MpqException(String message, Throwable cause) {
	super(message, cause);
    }

    public MpqException(String message) {
	super(message);
    }

    public MpqException(Throwable cause) {
	super(cause);
    }

}
