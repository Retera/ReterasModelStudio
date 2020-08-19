package com.hiveworkshop.wc3.gui.modeledit.actions;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.Vertex;

/**
 * MotionAction -- something for you to undo when you screw up with motion
 *
 * Eric Theller 6/8/2012
 */
public class RotateAction extends MoveAction {
	ArrayList<Normal> normals;
	ArrayList<Vertex> normalMoveVectors;

	public RotateAction(final List<Vertex> selection, final List<Vertex> moveVectors,
			final VertexActionType actionType) {
		super(selection, moveVectors, actionType);
		normals = new ArrayList<>();
		for (final Vertex ver : selection) {
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
	public void storeSelection(final List<Vertex> selection) {
		super.storeSelection(selection);
		for (final Vertex ver : selection) {
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
			normalMoveVectors.add(new Vertex(0, 0, 0));
		}
	}

	@Override
	public void redo() {
		super.redo();
		for (int i = 0; i < normals.size(); i++) {
			final Normal ver = normals.get(i);
			final Vertex vect = normalMoveVectors.get(i);
			ver.x += vect.x;
			ver.y += vect.y;
			ver.z += vect.z;
		}
	}

	@Override
	public void undo() {
		super.undo();
		for (int i = 0; i < normals.size(); i++) {
			final Normal ver = normals.get(i);
			final Vertex vect = normalMoveVectors.get(i);
			ver.x -= vect.x;
			ver.y -= vect.y;
			ver.z -= vect.z;
		}
	}

	public ArrayList<Normal> getNormals() {
		return normals;
	}

	public ArrayList<Vertex> getNormalMoveVectors() {
		return normalMoveVectors;
	}
}
