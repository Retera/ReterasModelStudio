package com.hiveworkshop.rms.ui.application.model.nodepanels;

public enum EventType {
	FPT,
	SND,
	SPL,
	UBR,
	SPN,
	UNK;

	public String getPrefix() {
		return name() + "x";
	}

}
