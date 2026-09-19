package com.hiveworkshop.wc3.mdl.v2;

/**
 * How a geoset, node or camera takes part in the editor views.
 * <p>
 * EDITABLE implies visible. VISIBLE means drawn but locked: it cannot be
 * selected or moved. HIDDEN is drawn nowhere.
 */
public enum ComponentVisibility {
	HIDDEN, VISIBLE, EDITABLE;

	public boolean isVisible() {
		return this != HIDDEN;
	}

	public boolean isEditable() {
		return this == EDITABLE;
	}
}
