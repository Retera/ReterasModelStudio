package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Rectangle;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoManager;

public final class SelectionTypeApplicatorForModelEditor {
	private final ToolbarButtonGroup<SelectionMode> modeButtonGroup;
	private final ModelEditor<?> modelEditor;
	private final UndoManager undoManager;

	public SelectionTypeApplicatorForModelEditor(final ToolbarButtonGroup<SelectionMode> modeButtonGroup,
			final ModelEditor<?> modelEditor, final UndoManager undoManager) {
		this.modeButtonGroup = modeButtonGroup;
		this.modelEditor = modelEditor;
		this.undoManager = undoManager;
	}

	public void applySelectionToRegion(final Rectangle region, final CoordinateSystem coordinateSystem) {
		switch (modeButtonGroup.getActiveButtonType()) {
		case ADD:
			modelEditor.addSelectedRegion(region, coordinateSystem);
			break;
		case SELECT:
			modelEditor.setSelectedRegion(region, coordinateSystem);
			break;
		case DESELECT:
			modelEditor.removeSelectedRegion(region, coordinateSystem);
			break;
		}
	}

}
