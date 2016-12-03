package com.hiveworkshop.wc3.gui.modeledit.actions;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.Vertex;

public class SelectAction implements UndoAction {
	private List<Vertex> oldSel;
	private List<Vertex> newSel;
	private MDLDisplay mdl;
	private SelectionActionType selectionType;

	public SelectAction(final List<Vertex> oldSelection, final List<Vertex> newSelection, final MDLDisplay mdld,
			final SelectionActionType selectType) {
		oldSel = oldSelection;
		newSel = new ArrayList<Vertex>(newSelection);
		mdl = mdld;
		selectionType = selectType;
	}

	public SelectAction() {

	}

	public void storeOldSelection(final ArrayList<Vertex> oldSelection) {
		oldSel = new ArrayList<Vertex>(oldSelection);
	}

	public void storeNewSelection(final ArrayList<Vertex> newSelection) {
		newSel = new ArrayList<Vertex>(newSelection);
	}

	@Override
	public void redo() {
		mdl.setSelection(newSel);
	}

	@Override
	public void undo() {
		mdl.setSelection(oldSel);
	}

	@Override
	public String actionName() {
		String outName = "";
		switch (selectionType) {
		case SELECT:
			outName = "selection: select";
			break;
		case ADD:
			outName = "selection: add";
			break;
		case DESELECT:
			outName = "selection: deselect";
			break;
		case SELECT_ALL:
			outName = "Select All";
			break;
		case INVERT_SELECTION:
			outName = "Invert Selection";
			break;
		case EXPAND_SELECTION:
			outName = "Expand Selection";
			break;
		}
		return outName;
	}

}
