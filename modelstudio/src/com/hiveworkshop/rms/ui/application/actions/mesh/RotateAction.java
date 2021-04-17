package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.actions.VertexActionType;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * MotionAction -- something for you to undo when you screw up with motion
 *
 * Eric Theller 6/8/2012
 */
public class RotateAction extends MoveAction {
	ArrayList<Vec3> normals;
	ArrayList<Vec3> normalMoveVectors;

	public RotateAction(final List<Vec3> selection, final List<Vec3> moveVectors,
			final VertexActionType actionType) {
		super(selection, moveVectors, actionType);
		normals = new ArrayList<>();
		for (final Vec3 ver : selection) {
			if (ver instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) ver;
				if (gv.getNormal() != null) {
					normals.add(gv.getNormal());
				}
			}
		}
	}

	public RotateAction() {
		super();
		normals = new ArrayList<>();
	}

	@Override
	public void storeSelection(final List<Vec3> selection) {
		super.storeSelection(selection);
		for (final Vec3 ver : selection) {
			if (ver instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) ver;
				if (gv.getNormal() != null) {
					normals.add(gv.getNormal());
				}
			}
		}
	}

	@Override
	public void createEmptyMoveVectors() {
		super.createEmptyMoveVectors();

		normalMoveVectors = new ArrayList<>();
		for (int i = 0; i < normals.size(); i++) {
			normalMoveVectors.add(new Vec3(0, 0, 0));
		}
	}

	@Override
	public void redo() {
		super.redo();
		for (int i = 0; i < normals.size(); i++) {
			final Vec3 ver = normals.get(i);
			final Vec3 vect = normalMoveVectors.get(i);
			ver.add(vect);
		}
	}

	@Override
	public void undo() {
		super.undo();
		for (int i = 0; i < normals.size(); i++) {
			final Vec3 ver = normals.get(i);
			final Vec3 vect = normalMoveVectors.get(i);
			ver.sub(vect);
		}
	}

	public ArrayList<Vec3> getNormals() {
		return normals;
	}

	public ArrayList<Vec3> getNormalMoveVectors() {
		return normalMoveVectors;
	}
}
