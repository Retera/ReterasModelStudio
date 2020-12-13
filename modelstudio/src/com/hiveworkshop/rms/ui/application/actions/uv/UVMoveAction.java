package com.hiveworkshop.rms.ui.application.actions.uv;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.rms.ui.application.actions.VertexActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec2;

/**
 * MotionAction -- something for you to undo when you screw up with motion
 *
 * Eric Theller 6/8/2012
 */
public class UVMoveAction implements UndoAction {
	private List<Vec2> selection;
	private List<Vec2> moveVectors;
	private Vec2 moveVector;
	private VertexActionType actType = VertexActionType.UNKNOWN;

	public UVMoveAction(final List<Vec2> selection, final List<Vec2> moveVectors, final VertexActionType actionType) {
		this.selection = new ArrayList<>(selection);
		this.moveVectors = moveVectors;
		actType = actionType;
	}

	public UVMoveAction(final List<Vec2> selection, final Vec2 moveVector, final VertexActionType actionType) {
		this.selection = new ArrayList<>(selection);
		this.moveVector = moveVector;
		actType = actionType;
	}

	public UVMoveAction() {

	}

	public void storeSelection(final List<Vec2> selection) {
		this.selection = new ArrayList<>(selection);
	}

	public void createEmptyMoveVectors() {
		moveVectors = new ArrayList<>();
		for (int i = 0; i < selection.size(); i++) {
			moveVectors.add(new Vec2(0, 0));
		}
	}

	public void createEmptyMoveVector() {
		moveVector = new Vec2(0, 0);
	}

	@Override
	public void redo() {
		if (moveVector == null) {
			for (int i = 0; i < selection.size(); i++) {
				final Vec2 ver = selection.get(i);
				final Vec2 vect = moveVectors.get(i);
				ver.x += vect.x;
				ver.y += vect.y;
			}
		} else {
			for (final Vec2 ver : selection) {
				final Vec2 vect = moveVector;
				ver.x += vect.x;
				ver.y += vect.y;
			}
		}
	}

	@Override
	public void undo() {
		if (moveVector == null) {
			for (int i = 0; i < selection.size(); i++) {
				final Vec2 ver = selection.get(i);
				final Vec2 vect = moveVectors.get(i);
				ver.x -= vect.x;
				ver.y -= vect.y;
			}
		} else {
			for (final Vec2 ver : selection) {
				final Vec2 vect = moveVector;
				ver.x -= vect.x;
				ver.y -= vect.y;
			}
		}
	}

	@Override
	public String actionName() {
		String outName = switch (actType) {
			case MOVE -> "move";
			case ROTATE -> "rotate";
			case SCALE -> "scale";
			default -> "";
		};
		if (outName.equals("")) {
			outName = "actionType_" + actType;
		}
		return outName + " TVertices";
	}

	public void setActType(final VertexActionType actType) {
		this.actType = actType;
	}

	public List<Vec2> getSelection() {
		return selection;
	}

	public List<Vec2> getMoveVectors() {
		return moveVectors;
	}

	public Vec2 getMoveVector() {
		return moveVector;
	}

	public VertexActionType getActType() {
		return actType;
	}
}
