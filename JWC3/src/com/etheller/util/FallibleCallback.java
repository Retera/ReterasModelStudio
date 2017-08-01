package com.etheller.util;

public interface FallibleCallback<TYPE> {
	void success(TYPE type);

	void failure(String message);
}
