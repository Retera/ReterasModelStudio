package com.hiveworkshop.rms.ui.application.actions.mesh;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.rms.ui.application.actions.VertexActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vertex3;

/**
 * MotionAction -- something for you to undo when you screw up with motion
 *
 * Eric Theller 6/8/2012
 */
public class MoveAction implements UndoAction {
	private List<Vertex3> selection;
	private List<Vertex3> moveVectors;
	private Vertex3 moveVector;
	private VertexActionType actType = VertexActionType.UNKNOWN;

	public MoveAction(final List<Vertex3> selection, final List<Vertex3> moveVectors, final VertexActionType actionType) {
		this.selection = new ArrayList<Vertex3>(selection);
		this.moveVectors = moveVectors;
		actType = actionType;
	}

	public MoveAction(final List<Vertex3> selection, final Vertex3 moveVector, final VertexActionType actionType) {
		this.selection = new ArrayList<Vertex3>(selection);
		this.moveVector = moveVector;
		actType = actionType;
	}

	public MoveAction() {

	}

	public void storeSelection(final List<Vertex3> selection) {
		this.selection = new ArrayList<Vertex3>(selection);
	}

	public void createEmptyMoveVectors() {
		moveVectors = new ArrayList<Vertex3>();
		for (int i = 0; i < selection.size(); i++) {
			moveVectors.add(new Vertex3(0, 0, 0));
		}
	}

	public void createEmptyMoveVector() {
		moveVector = new Vertex3(0, 0, 0);
	}

	@Override
	public void redo() {
		if (moveVector == null) {
			for (int i = 0; i < selection.size(); i++) {
				final Vertex3 ver = selection.get(i);
				final Vertex3 vect = moveVectors.get(i);
				ver.x += vect.x;
				ver.y += vect.y;
				ver.z += vect.z;
			}
		} else {
			for (int i = 0; i < selection.size(); i++) {
				final Vertex3 ver = selection.get(i);
				final Vertex3 vect = moveVector;
				ver.x += vect.x;
				ver.y += vect.y;
				ver.z += vect.z;
			}
		}
	}

	@Override
	public void undo() {
		if (moveVector == null) {
			for (int i = 0; i < selection.size(); i++) {
				final Vertex3 ver = selection.get(i);
				final Vertex3 vect = moveVectors.get(i);
				ver.x -= vect.x;
				ver.y -= vect.y;
				ver.z -= vect.z;
			}
		} else {
			for (int i = 0; i < selection.size(); i++) {
				final Vertex3 ver = selection.get(i);
				final Vertex3 vect = moveVector;
				ver.x -= vect.x;
				ver.y -= vect.y;
				ver.z -= vect.z;
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
		case UNKNOWN:
			outName = "unknown error-type action";
			break;
		}
		if (outName.equals("")) {
			outName = "actionType_" + actType;
		}
		return outName + " vertices";
	}

	public List<Vertex3> getMoveVectors() {
		return moveVectors;
	}

	public void setMoveVectors(final List<Vertex3> moveVectors) {
		this.moveVectors = moveVectors;
	}

	public Vertex3 getMoveVector() {
		return moveVector;
	}

	public void setMoveVector(final Vertex3 moveVector) {
		this.moveVector = moveVector;
	}

	public void setActType(final VertexActionType actType) {
		this.actType = actType;
	}
}
