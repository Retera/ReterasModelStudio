package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MirrorModelAction implements UndoAction {
	private char[] DIMENSION_NAMES = { 'Z', 'X', 'Y' };
	private List<GeosetVertex> selection;
	private final List<IdObject> idObjects;
	private final byte mirrorDim;
	private final Vec3 center;

	public MirrorModelAction(Collection<GeosetVertex> selection, Collection<IdObject> idObjects, byte mirrorDim, Vec3 center) {
		this.center = center;
		this.selection = new ArrayList<>(selection);
		this.idObjects = new ArrayList<>(idObjects);
		this.mirrorDim = mirrorDim;
	}

	@Override
	public UndoAction undo() {
		doMirror();
		return this;
	}

	@Override
	public UndoAction redo() {
		doMirror();
		return this;
	}

	private void doMirror() {
		Vec3 center = new Vec3(this.center);
		// Vertex.centerOfGroup(selection);// Calc center
		// // of mass
		for (GeosetVertex vert : selection) {
			vert.setCoord(mirrorDim, (2 * center.getCoord(mirrorDim)) - vert.getCoord(mirrorDim));
			Vec3 normal = vert.getNormal();
			if (normal != null) {
				// Flip normals, preserve lighting!
				normal.setCoord(mirrorDim, -normal.getCoord(mirrorDim));
				// this will inverse back if they correctly choose to flip
				// faces, otherwise we're making an inside out model now
				normal.negate();
			}
			if (vert.getTangent() != null) {
				// TODO doesn't support flip yet
				vert.getTangent().setCoord(mirrorDim, -vert.getTangent().getCoord(mirrorDim));
			}
		}
		List<IdObject> selBones = new ArrayList<>();
		for (IdObject b : idObjects) {
			if (selection.contains(b.getPivotPoint()) && !selBones.contains(b)) {
				selBones.add(b);
			}
		}
		for (IdObject obj : selBones) {
			obj.flipOver(mirrorDim);
		}
	}

	@Override
	public String actionName() {
		return "mirror " + DIMENSION_NAMES[mirrorDim];
	}

}
