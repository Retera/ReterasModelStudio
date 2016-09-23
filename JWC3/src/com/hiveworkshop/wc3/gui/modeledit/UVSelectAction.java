package com.hiveworkshop.wc3.gui.modeledit;

import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.TVertex;

public class UVSelectAction implements UndoAction {
	ArrayList<TVertex> oldSel;
	ArrayList<TVertex> newSel;
	MDLDisplay mdl;
	int selectionType;

	public UVSelectAction(final ArrayList<TVertex> oldSelection, final ArrayList<TVertex> newSelection,
			final MDLDisplay mdld, final int selectType) {
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
		mdl.uvselection = newSel;
	}

	@Override
	public void undo() {
		mdl.uvselection = oldSel;
	}

	@Override
	public String actionName() {
		String outName = "";
		switch (selectionType) {
		case 0:
			outName = "UV selection: select";
			break;
		case 1:
			outName = "UV selection: add";
			break;
		case 2:
			outName = "UV selection: deselect";
			break;
		case 3:
			outName = "UV Select All";
			break;
		case 4:
			outName = "UV Invert Selection";
			break;
		case 5:
			outName = "UV Expand Selection";
			break;
		case 6:
			outName = "UV Select from Viewer";
			break;
		}
		return outName;
	}

}
