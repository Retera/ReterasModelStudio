package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MirrorModelAction implements UndoAction {
	private final char[] DIMENSION_NAMES = { 'Z', 'X', 'Y' };
	private final List<Vec3> selection;
	private final List<IdObject> idObjects;
	private final byte mirrorDim;
	private final double centerX;
	private final double centerY;
	private final double centerZ;

	public MirrorModelAction(final Collection<? extends Vec3> selection, final Collection<IdObject> idObjects,
			final byte mirrorDim, final double centerX, final double centerY, final double centerZ) {
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		this.selection = new ArrayList<>(selection);
		this.idObjects = new ArrayList<>(idObjects);
		this.mirrorDim = mirrorDim;
	}

	@Override
	public void undo() {
		doMirror();
	}

	@Override
	public void redo() {
		doMirror();
	}

	private void doMirror() {
		final Vec3 center = new Vec3(centerX, centerY, centerZ);
		// Vertex.centerOfGroup(selection);// Calc center
		// // of mass
		for (final Vec3 vert : selection) {
			vert.setCoord(mirrorDim, (2 * center.getCoord(mirrorDim)) - vert.getCoord(mirrorDim));
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				final Vec3 normal = gv.getNormal();
				if (normal != null) {
					// Flip normals, preserve lighting!
					normal.setCoord(mirrorDim, -normal.getCoord(mirrorDim));
					// this will inverse back if they correctly choose to flip
					// faces, otherwise we're making an inside out model now
					normal.negate();
				}
				if (gv.getTangent() != null) {
					// TODO doesn't support flip yet
					gv.getTangent().setCoord(mirrorDim, -gv.getTangent().getCoord(mirrorDim));
				}
			}
		}
		final List<IdObject> selBones = new ArrayList<>();
		for (final IdObject b : idObjects) {
			if (selection.contains(b.getPivotPoint()) && !selBones.contains(b)) {
				selBones.add(b);
			}
		}
		for (final IdObject obj : selBones) {
			obj.flipOver(mirrorDim);
		}
	}

	@Override
	public String actionName() {
		return "mirror " + DIMENSION_NAMES[mirrorDim];
	}

}
