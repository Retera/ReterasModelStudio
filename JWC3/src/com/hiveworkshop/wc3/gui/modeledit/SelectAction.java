package com.hiveworkshop.wc3.gui.modeledit;

import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.Vertex;

public class SelectAction implements UndoAction {
	ArrayList<Vertex> oldSel;
	ArrayList<Vertex> newSel;
	MDLDisplay mdl;
	int selectionType;

	public SelectAction(final ArrayList<Vertex> oldSelection, final ArrayList<Vertex> newSelection,
			final MDLDisplay mdld, final int selectType) {
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
		mdl.selection = newSel;
	}

	@Override
	public void undo() {
		mdl.selection = oldSel;
	}

	@Override
	public String actionName() {
		String outName = "";
		switch (selectionType) {
		case 0:
			outName = "selection: select";
			break;
		case 1:
			outName = "selection: add";
			break;
		case 2:
			outName = "selection: deselect";
			break;
		case 3:
			outName = "Select All";
			break;
		case 4:
			outName = "Invert Selection";
			break;
		case 5:
			outName = "Expand Selection";
			break;
		}
		return outName;
	}

}
