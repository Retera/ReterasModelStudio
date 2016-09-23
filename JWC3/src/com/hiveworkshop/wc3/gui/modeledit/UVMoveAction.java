package com.hiveworkshop.wc3.gui.modeledit;

import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.TVertex;

/**
 * MotionAction -- something for you to undo when you screw up with motion
 * 
 * Eric Theller 6/8/2012
 */
public class UVMoveAction implements UndoAction {
	ArrayList<TVertex> selection;
	ArrayList<TVertex> moveVectors;
	TVertex moveVector;
	int actType = 0;

	public UVMoveAction(final ArrayList<TVertex> selection, final ArrayList<TVertex> moveVectors,
			final int actionType) {
		this.selection = new ArrayList<TVertex>(selection);
		this.moveVectors = moveVectors;
		actType = actionType;
	}

	public UVMoveAction(final ArrayList<TVertex> selection, final TVertex moveVector, final int actionType) {
		this.selection = new ArrayList<TVertex>(selection);
		this.moveVector = moveVector;
		actType = actionType;
	}

	public UVMoveAction() {

	}

	public void storeSelection(final ArrayList<TVertex> selection) {
		this.selection = new ArrayList<TVertex>(selection);
	}

	public void createEmptyMoveVectors() {
		moveVectors = new ArrayList<TVertex>();
		for (int i = 0; i < selection.size(); i++) {
			moveVectors.add(new TVertex(0, 0));
		}
	}

	public void createEmptyMoveVector() {
		moveVector = new TVertex(0, 0);
	}

	@Override
	public void redo() {
		if (moveVector == null) {
			for (int i = 0; i < selection.size(); i++) {
				final TVertex ver = selection.get(i);
				final TVertex vect = moveVectors.get(i);
				ver.x += vect.x;
				ver.y += vect.y;
			}
		} else {
			for (int i = 0; i < selection.size(); i++) {
				final TVertex ver = selection.get(i);
				final TVertex vect = moveVector;
				ver.x += vect.x;
				ver.y += vect.y;
			}
		}
	}

	@Override
	public void undo() {
		if (moveVector == null) {
			for (int i = 0; i < selection.size(); i++) {
				final TVertex ver = selection.get(i);
				final TVertex vect = moveVectors.get(i);
				ver.x -= vect.x;
				ver.y -= vect.y;
			}
		} else {
			for (int i = 0; i < selection.size(); i++) {
				final TVertex ver = selection.get(i);
				final TVertex vect = moveVector;
				ver.x -= vect.x;
				ver.y -= vect.y;
			}
		}
	}

	@Override
	public String actionName() {
		String outName = "";
		switch (actType) {
		case 3:
			outName = "move";
			break;
		case 4:
			outName = "rotate";
			break;
		case 5:
			outName = "scale";
			break;
		}
		if (outName.equals("")) {
			outName = "actionType_" + actType;
		}
		return outName + " TVertices";
	}
}
