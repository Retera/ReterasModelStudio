package com.hiveworkshop.rms.ui.application.actions.uv;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.rms.ui.application.actions.VertexActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vertex2;

/**
 * MotionAction -- something for you to undo when you screw up with motion
 *
 * Eric Theller 6/8/2012
 */
public class UVMoveAction implements UndoAction {
	private List<Vertex2> selection;
	private List<Vertex2> moveVectors;
	private Vertex2 moveVector;
	private VertexActionType actType = VertexActionType.UNKNOWN;

	public UVMoveAction(final List<Vertex2> selection, final List<Vertex2> moveVectors, final VertexActionType actionType) {
		this.selection = new ArrayList<Vertex2>(selection);
		this.moveVectors = moveVectors;
		actType = actionType;
	}

	public UVMoveAction(final List<Vertex2> selection, final Vertex2 moveVector, final VertexActionType actionType) {
		this.selection = new ArrayList<Vertex2>(selection);
		this.moveVector = moveVector;
		actType = actionType;
	}

	public UVMoveAction() {

	}

	public void storeSelection(final List<Vertex2> selection) {
		this.selection = new ArrayList<Vertex2>(selection);
	}

	public void createEmptyMoveVectors() {
		moveVectors = new ArrayList<Vertex2>();
		for (int i = 0; i < selection.size(); i++) {
			moveVectors.add(new Vertex2(0, 0));
		}
	}

	public void createEmptyMoveVector() {
		moveVector = new Vertex2(0, 0);
	}

	@Override
	public void redo() {
		if (moveVector == null) {
			for (int i = 0; i < selection.size(); i++) {
				final Vertex2 ver = selection.get(i);
				final Vertex2 vect = moveVectors.get(i);
				ver.x += vect.x;
				ver.y += vect.y;
			}
		} else {
			for (int i = 0; i < selection.size(); i++) {
				final Vertex2 ver = selection.get(i);
				final Vertex2 vect = moveVector;
				ver.x += vect.x;
				ver.y += vect.y;
			}
		}
	}

	@Override
	public void undo() {
		if (moveVector == null) {
			for (int i = 0; i < selection.size(); i++) {
				final Vertex2 ver = selection.get(i);
				final Vertex2 vect = moveVectors.get(i);
				ver.x -= vect.x;
				ver.y -= vect.y;
			}
		} else {
			for (int i = 0; i < selection.size(); i++) {
				final Vertex2 ver = selection.get(i);
				final Vertex2 vect = moveVector;
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

	public List<Vertex2> getSelection() {
		return selection;
	}

	public List<Vertex2> getMoveVectors() {
		return moveVectors;
	}

	public Vertex2 getMoveVector() {
		return moveVector;
	}

	public VertexActionType getActType() {
		return actType;
	}
}
