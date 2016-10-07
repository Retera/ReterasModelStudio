package com.hiveworkshop.wc3.gui.modeledit.actions;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.TVertex;

public class UVSelectAction implements UndoAction {
	private List<TVertex> oldSel;
	private List<TVertex> newSel;
	private MDLDisplay mdl;
	private UVSelectionActionType selectionType;

	public UVSelectAction(final List<TVertex> oldSelection, final List<TVertex> newSelection, final MDLDisplay mdld,
			final UVSelectionActionType selectType) {
		oldSel = oldSelection;
		newSel = new ArrayList<TVertex>(newSelection);
		mdl = mdld;
		selectionType = selectType;
	}

	public UVSelectAction() {

	}

	public void storeOldSelection(final ArrayList<TVertex> oldSelection) {
		oldSel = new ArrayList<TVertex>(oldSelection);
	}

	public void storeNewSelection(final ArrayList<TVertex> newSelection) {
		newSel = new ArrayList<TVertex>(newSelection);
	}

	@Override
	public void redo() {
		mdl.setUvselection(newSel);
	}

	@Override
	public void undo() {
		mdl.setUvselection(oldSel);
	}

	@Override
	public String actionName() {
		String outName = "";
		switch (selectionType) {
		case SELECT:
			outName = "UV selection: select";
			break;
		case ADD:
			outName = "UV selection: add";
			break;
		case DESELECT:
			outName = "UV selection: deselect";
			break;
		case SELECT_ALL:
			outName = "UV Select All";
			break;
		case INVERT_SELECTION:
			outName = "UV Invert Selection";
			break;
		case EXPAND_SELECTION:
			outName = "UV Expand Selection";
			break;
		case SELECT_FROM_VIEWER:
			outName = "UV Select from Viewer";
			break;
		}
		return outName;
	}

}
