package com.mundi4.mpq.helper;

import java.util.Iterator;

public class ReadOnlyIterator<E> implements Iterator<E> {

    private Iterator<E> iterator;

    public ReadOnlyIterator(Iterator<E> iterator) {
	this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
	return iterator.hasNext();
    }

    @Override
    public E next() {
	return iterator.next();
    }

    @Override
    public void remove() {
	throw new UnsupportedOperationException("This iterator is read-only");
    }

}
