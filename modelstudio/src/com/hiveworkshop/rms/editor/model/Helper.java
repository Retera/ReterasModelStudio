package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;

public class Helper extends IdObject {
	public Helper(final String name) {
		super();
		this.name = name;
	}

	public Helper(final Helper h) {
		super(h);
	}

	public Helper() {
	}

	@Override
	public Helper copy() {
		return new Helper(this);
	}


	@Override
	public double getClickRadius() {
		return ProgramGlobals.getPrefs().getNodeBoxSize();
	}
}
