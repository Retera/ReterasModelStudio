package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class MirrorModelAction implements UndoAction {
	private final char[] DIMENSION_NAMES = { 'Z', 'X', 'Y' };
	private final List<Vertex> selection;
	private final List<IdObject> idObjects;
	private final byte mirrorDim;

	public MirrorModelAction(final Collection<Vertex> selection, final Collection<IdObject> idObjects,
			final byte mirrorDim) {
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
		final Vertex center = Vertex.centerOfGroup(selection);// Calc center
																// of mass
		for (final Vertex vert : selection) {
			vert.setCoord(mirrorDim, 2 * center.getCoord(mirrorDim) - vert.getCoord(mirrorDim));
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
			}
		}
		final ArrayList<IdObject> selBones = new ArrayList<>();
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
