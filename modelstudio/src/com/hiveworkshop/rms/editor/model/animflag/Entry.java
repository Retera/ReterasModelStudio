package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Objects;

public class Entry<T> {
	public Integer time;
	public T value, inTan, outTan;

	public Entry(Integer time, T value, T inTan, T outTan) {
		super();
		this.time = time;
		this.value = value;
		this.inTan = inTan;
		this.outTan = outTan;
	}

	public Entry(Integer time, T value) {
		super();
		this.time = time;
		this.value = value;
	}

	public Entry(Entry<T> other) {
		super();
		this.time = other.time;
		this.value = cloneEntryValue(other.value);
		this.inTan = cloneEntryValue(other.inTan);
		this.outTan = cloneEntryValue(other.outTan);
	}

	public void set(Entry<T> other) {
		time = other.time;
		value = other.value;
		inTan = other.inTan;
		outTan = other.outTan;
	}

	private T cloneEntryValue(T value) {
		if (value == null) {
			return null;
		}

		if (value instanceof Integer || value instanceof Float) {
			return value;
		} else if (value instanceof Vec3) {
			return (T) new Vec3((Vec3) value);
		} else if (value instanceof Quat) {
			return (T) new Quat((Quat) value);
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
	}

	public Integer getTime() {
		return time;
	}

	public Entry<T> setTime(Integer time) {
		this.time = time;
		return this;
	}

	public T getValue() {
		return value;
	}

	public Entry<T> setValue(T value) {
		this.value = value;
		return this;
	}

	public T getInTan() {
		return inTan;
	}

	public Entry<T> setInTan(T inTan) {
		this.inTan = inTan;
		return this;
	}

	public T getOutTan() {
		return outTan;
	}

	public Entry<T> setOutTan(T outTan) {
		this.outTan = outTan;
		return this;
	}

	public Entry<T> linearize() {
		inTan = null;
		outTan = null;
		return this;
	}

	public Entry<T> unLinearize() {
		inTan = cloneEntryValue(value);
		outTan = cloneEntryValue(value);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Entry<?> entry = (Entry<?>) o;
		return time.equals(entry.time) && value.equals(entry.value) && Objects.equals(inTan, entry.inTan) && Objects.equals(outTan, entry.outTan);
	}

	@Override
	public int hashCode() {
		return Objects.hash(time, value, inTan, outTan);
	}
}
