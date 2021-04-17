package com.hiveworkshop.rms.ui.application.actions.uv;

public enum UVSelectionActionType {
	SELECT, ADD, DESELECT, SELECT_ALL, INVERT_SELECTION, EXPAND_SELECTION, SELECT_FROM_VIEWER;

    public static UVSelectionActionType fromLegacyId(final int selectionType) {
		return switch (selectionType) {
			case 0 -> SELECT;
			case 1 -> ADD;
			case 2 -> DESELECT;
			case 3 -> SELECT_ALL;
			case 4 -> INVERT_SELECTION;
			case 5 -> EXPAND_SELECTION;
			case 6 -> SELECT_FROM_VIEWER;
			default -> null;
		};
	}
}
