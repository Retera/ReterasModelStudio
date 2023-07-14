package com.hiveworkshop.rms.util.sound;

public abstract class EventTarget {
	public abstract String getName();
	public abstract String getTag();
	public abstract String[] getFileNames();


	protected int getInt(String s) {
		return Integer.parseInt(s.split("K")[1]);
	}

	protected float getFloat(String s) {
		return Float.parseFloat(s.split("K")[1]);
	}

	protected String getString(String s) {
		return s.split("\"")[1];
	}
	@Override
	public String toString() {
		return getTag() + " " + getName();
	}
}
