package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import java.awt.Color;

public class UnitEditorSettings {
	boolean displayAsRawData;
	Color editedValueColor = new Color(192, 0, 192);
	Color selectedValueColor = Color.WHITE;
	Color selectedUnfocusedValueColor = new Color(240, 240, 240);

	public boolean isDisplayAsRawData() {
		return displayAsRawData;
	}

	public void setDisplayAsRawData(final boolean displayAsRawData) {
		this.displayAsRawData = displayAsRawData;
	}

	public Color getEditedValueColor() {
		return editedValueColor;
	}

	public void setEditedValueColor(final Color editedValueColor) {
		this.editedValueColor = editedValueColor;
	}

	public Color getSelectedValueColor() {
		return selectedValueColor;
	}

	public void setSelectedValueColor(final Color selectedValueColor) {
		this.selectedValueColor = selectedValueColor;
	}

	public Color getSelectedUnfocusedValueColor() {
		return selectedUnfocusedValueColor;
	}

	public void setSelectedUnfocusedValueColor(final Color selectedUnfocusedValueColor) {
		this.selectedUnfocusedValueColor = selectedUnfocusedValueColor;
	}
}
