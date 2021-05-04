package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener;

import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;

import java.util.Collection;

public class EditabilityToggleHandler {
	private final Collection<CheckableDisplayElement<?>> elements;

	public EditabilityToggleHandler(final Collection<CheckableDisplayElement<?>> elements) {
		this.elements = elements;
	}

	public void makeEditable() {
		for (final CheckableDisplayElement<?> element : elements) {
			element.setChecked(true);
		}
	}

	public void makeNotEditable() {
		for (final CheckableDisplayElement<?> element : elements) {
			element.setChecked(false);
		}
	}
}
