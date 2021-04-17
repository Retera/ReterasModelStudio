package com.hiveworkshop.rms.ui.browsers.jworldedit;

import java.awt.Color;

public class WorldEditorSettings {
	private Color selectedValueColor = Color.WHITE;
	private Color selectedUnfocusedValueColor = new Color(240, 240, 240);
	private Color triggerCommentColor = new Color(0, 128, 0);

	public Color getSelectedValueColor() {
		return selectedValueColor;
	}

	public Color getSelectedUnfocusedValueColor() {
		return selectedUnfocusedValueColor;
	}

	public Color getTriggerCommentColor() {
		return triggerCommentColor;
	}

	public void setSelectedValueColor(final Color selectedValueColor) {
		this.selectedValueColor = selectedValueColor;
	}

	public void setSelectedUnfocusedValueColor(final Color selectedUnfocusedValueColor) {
		this.selectedUnfocusedValueColor = selectedUnfocusedValueColor;
	}

	public void setTriggerCommentColor(final Color triggerCommentColor) {
		this.triggerCommentColor = triggerCommentColor;
	}
}
