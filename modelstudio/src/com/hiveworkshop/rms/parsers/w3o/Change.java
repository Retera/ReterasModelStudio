package com.hiveworkshop.rms.parsers.w3o;

import com.hiveworkshop.rms.util.War3ID;

public final class Change {
	private War3ID id;
	private int vartype, level, dataptr;
	private int longval;
	private float realval;
	private String strval;

	private boolean boolval;
	private War3ID junkDNA;

	public War3ID getId() {
		return id;
	}

	public Change setId(final War3ID id) {
		this.id = id;
		return this;
	}

	public int getVartype() {
		return vartype;
	}

	public Change setVartype(final int vartype) {
		this.vartype = vartype;
		return this;
	}

	public int getLevel() {
		return level;
	}

	public Change setLevel(final int level) {
		this.level = level;
		return this;
	}

	public int getDataptr() {
		return dataptr;
	}

	public Change setDataptr(final int dataptr) {
		this.dataptr = dataptr;
		return this;
	}

	public int getLongval() {
		return longval;
	}

	public Change setLongval(final int longval) {
		this.longval = longval;
		return this;
	}

	public float getRealval() {
		return realval;
	}

	public Change setRealval(final float realval) {
		this.realval = realval;
		return this;
	}

	public String getStrval() {
		return strval;
	}

	public Change setStrval(final String strval) {
		this.strval = strval;
		return this;
	}

	public boolean isBoolval() {
		return boolval;
	}

	public Change setBoolval(final boolean boolval) {
		this.boolval = boolval;
		return this;
	}

	public Change setJunkDNA(final War3ID junkDNA) {
		this.junkDNA = junkDNA;
		return this;
	}

	public War3ID getJunkDNA() {
		return junkDNA;
	}

	public Change copyFrom(final Change other) {
		id = other.id;
		level = other.level;
		dataptr = other.dataptr;
		vartype = other.vartype;
		longval = other.longval;
		realval = other.realval;
		strval = other.strval;
		boolval = other.boolval;
		junkDNA = other.junkDNA;
		return this;
	}

	@Override
	public Change clone() {
		final Change copy = new Change();
		copy.copyFrom(this);
		return copy;
	}
}
