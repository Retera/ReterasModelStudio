package com.hiveworkshop.rms.ui.application.actions.uv;

public enum UVSelectionActionType {
	SELECT, ADD, DESELECT, SELECT_ALL, INVERT_SELECTION, EXPAND_SELECTION, SELECT_FROM_VIEWER;

    public static UVSelectionActionType fromLegacyId(final int selectionType) {
		switch (selectionType) {
		case 0:
			return SELECT;
		case 1:
			return ADD;
		case 2:
			return DESELECT;
		case 3:
			return SELECT_ALL;
		case 4:
			return INVERT_SELECTION;
		case 5:
			return EXPAND_SELECTION;
		case 6:
			return SELECT_FROM_VIEWER;
		}
		return null;
	}
}
