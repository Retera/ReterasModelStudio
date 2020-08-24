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

	public void setId(final War3ID id) {
		this.id = id;
	}

	public int getVartype() {
		return vartype;
	}

	public void setVartype(final int vartype) {
		this.vartype = vartype;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(final int level) {
		this.level = level;
	}

	public int getDataptr() {
		return dataptr;
	}

	public void setDataptr(final int dataptr) {
		this.dataptr = dataptr;
	}

	public int getLongval() {
		return longval;
	}

	public void setLongval(final int longval) {
		this.longval = longval;
	}

	public float getRealval() {
		return realval;
	}

	public void setRealval(final float realval) {
		this.realval = realval;
	}

	public String getStrval() {
		return strval;
	}

	public void setStrval(final String strval) {
		this.strval = strval;
	}

	public boolean isBoolval() {
		return boolval;
	}

	public void setBoolval(final boolean boolval) {
		this.boolval = boolval;
	}

	public void setJunkDNA(final War3ID junkDNA) {
		this.junkDNA = junkDNA;
	}

	public War3ID getJunkDNA() {
		return junkDNA;
	}

	public void copyFrom(final Change other) {
		id = other.id;
		level = other.level;
		dataptr = other.dataptr;
		vartype = other.vartype;
		longval = other.longval;
		realval = other.realval;
		strval = other.strval;
		boolval = other.boolval;
		junkDNA = other.junkDNA;
	}

	@Override
	public Change clone() {
		final Change copy = new Change();
		copy.copyFrom(this);
		return copy;
	}
}
