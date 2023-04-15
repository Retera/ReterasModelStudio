package com.hiveworkshop.rms.ui.application.viewer;

public enum LoopType {
	DEFAULT_LOOP("Default Loop"),
	ALWAYS_LOOP("Always Loop"),
	NEVER_LOOP("Never Loop"),
	;
	private final String string;
	LoopType(String string){
		this.string = string;
	}

	@Override
	public String toString() {
		return string;
	}
}
