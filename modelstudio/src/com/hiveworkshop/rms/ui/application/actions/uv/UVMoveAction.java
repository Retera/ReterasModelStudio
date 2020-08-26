package com.hiveworkshop.rms.ui.application.actions.uv;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.rms.ui.application.actions.VertexActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vector2;

/**
 * MotionAction -- something for you to undo when you screw up with motion
 *
 * Eric Theller 6/8/2012
 */
public class UVMoveAction implements UndoAction {
	private List<Vector2> selection;
	private List<Vector2> moveVectors;
	private Vector2 moveVector;
	private VertexActionType actType = VertexActionType.UNKNOWN;

	public UVMoveAction(final List<Vector2> selection, final List<Vector2> moveVectors, final VertexActionType actionType) {
		this.selection = new ArrayList<Vector2>(selection);
		this.moveVectors = moveVectors;
		actType = actionType;
	}

	public UVMoveAction(final List<Vector2> selection, final Vector2 moveVector, final VertexActionType actionType) {
		this.selection = new ArrayList<Vector2>(selection);
		this.moveVector = moveVector;
		actType = actionType;
	}

	public UVMoveAction() {

	}

	public void storeSelection(final List<Vector2> selection) {
		this.selection = new ArrayList<Vector2>(selection);
	}

	public void createEmptyMoveVectors() {
		moveVectors = new ArrayList<Vector2>();
		for (int i = 0; i < selection.size(); i++) {
			moveVectors.add(new Vector2(0, 0));
		}
	}

	public void createEmptyMoveVector() {
		moveVector = new Vector2(0, 0);
	}

	@Override
	public void redo() {
		if (moveVector == null) {
			for (int i = 0; i < selection.size(); i++) {
				final Vector2 ver = selection.get(i);
				final Vector2 vect = moveVectors.get(i);
				ver.x += vect.x;
				ver.y += vect.y;
			}
		} else {
			for (int i = 0; i < selection.size(); i++) {
				final Vector2 ver = selection.get(i);
				final Vector2 vect = moveVector;
				ver.x += vect.x;
				ver.y += vect.y;
			}
		}
	}

	@Override
	public void undo() {
		if (moveVector == null) {
			for (int i = 0; i < selection.size(); i++) {
				final Vector2 ver = selection.get(i);
				final Vector2 vect = moveVectors.get(i);
				ver.x -= vect.x;
				ver.y -= vect.y;
			}
		} else {
			for (int i = 0; i < selection.size(); i++) {
				final Vector2 ver = selection.get(i);
				final Vector2 vect = moveVector;
				ver.x -= vect.x;
				ver.y -= vect.y;
			}
		}
	}

	@Override
	public String actionName() {
		String outName = "";
		switch (actType) {
		case MOVE:
			outName = "move";
			break;
		case ROTATE:
			outName = "rotate";
			break;
		case SCALE:
			outName = "scale";
			break;
		}
		if (outName.equals("")) {
			outName = "actionType_" + actType;
		}
		return outName + " TVertices";
	}

	public void setActType(final VertexActionType actType) {
		this.actType = actType;
	}

	public List<Vector2> getSelection() {
		return selection;
	}

	public List<Vector2> getMoveVectors() {
		return moveVectors;
	}

	public Vector2 getMoveVector() {
		return moveVector;
	}

	public VertexActionType getActType() {
		return actType;
	}
}
