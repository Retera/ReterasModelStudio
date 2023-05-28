package com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells;

public class AbstractShell {
	protected final boolean isFromDonating;
	AbstractShell(boolean isFromDonating) {
		this.isFromDonating = isFromDonating;
	}
	public boolean isFromDonating() {
		return isFromDonating;
	}
}
