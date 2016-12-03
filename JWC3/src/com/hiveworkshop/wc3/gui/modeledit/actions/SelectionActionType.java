package com.hiveworkshop.wc3.gui.modeledit.actions;

public enum SelectionActionType {
	SELECT, ADD, DESELECT, SELECT_ALL, INVERT_SELECTION, EXPAND_SELECTION;
	;

	public static SelectionActionType fromLegacyId(final int selectionType) {
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
		}
		return null;
	}
}
