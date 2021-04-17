package com.hiveworkshop.rms.ui.application.actions;

public enum SelectionActionType {
	SELECT, ADD, DESELECT, SELECT_ALL, INVERT_SELECTION, EXPAND_SELECTION;

    public static SelectionActionType fromLegacyId(final int selectionType) {
		return switch (selectionType) {
			case 0 -> SELECT;
			case 1 -> ADD;
			case 2 -> DESELECT;
			case 3 -> SELECT_ALL;
			case 4 -> INVERT_SELECTION;
			case 5 -> EXPAND_SELECTION;
			default -> null;
		};
	}
}
