package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;

/**
 * Write a description of class Helper here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Helper extends Bone { // Haha blizz
	public Helper(final String name) {
		super();
		this.name = name;
	}

	public Helper(final Helper h) {
		super(h);
	}

	public Helper() {
	}

	// printTo is already written as a part of bone; these two things are stupidly the same
	@Override
	public Helper copy() {
		return new Helper(this);
	}


	@Override
	public double getClickRadius() {
		return ProgramGlobals.getPrefs().getNodeBoxSize();
	}
}
