package com.etheller.collections;

public interface Map<KEY, VALUE> extends MapView<KEY, VALUE> {
	void clear();

	VALUE remove(KEY key);

	VALUE put(KEY key, VALUE value);
}
