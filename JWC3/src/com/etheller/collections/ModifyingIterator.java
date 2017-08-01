package com.etheller.collections;

import java.util.Iterator;

public interface ModifyingIterator<TYPE> extends Iterator<TYPE> {
	void delete();
}
