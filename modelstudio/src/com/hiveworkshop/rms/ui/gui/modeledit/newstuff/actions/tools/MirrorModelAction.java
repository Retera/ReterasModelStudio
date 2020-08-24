package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Normal;
import com.hiveworkshop.rms.editor.model.Vertex;

public final class MirrorModelAction implements UndoAction {
	private final char[] DIMENSION_NAMES = { 'Z', 'X', 'Y' };
	private final List<Vertex> selection;
	private final List<IdObject> idObjects;
	private final byte mirrorDim;
	private final double centerX;
	private final double centerY;
	private final double centerZ;

	public MirrorModelAction(final Collection<? extends Vertex> selection, final Collection<IdObject> idObjects,
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
		final Vertex center = new Vertex(centerX, centerY, centerZ);
		// Vertex.centerOfGroup(selection);// Calc center
		// // of mass
		for (final Vertex vert : selection) {
			vert.setCoord(mirrorDim, (2 * center.getCoord(mirrorDim)) - vert.getCoord(mirrorDim));
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				final Normal normal = gv.getNormal();
				if (normal != null) {
					// Flip normals, preserve lighting!
					normal.setCoord(mirrorDim, -normal.getCoord(mirrorDim));
					// this will inverse back if they correctly choose to flip
					// faces, otherwise we're making an inside out model now
					normal.inverse();
				}
				if (gv.getTangent() != null) {
					// TODO doesn't support flip yet
					gv.getTangent()[mirrorDim] = -gv.getTangent()[mirrorDim];
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
