package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import java.util.Collection;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;

public class AddTriangleAction implements UndoAction {
	private final Geoset geoset;
	private final Collection<Triangle> triangles;

	public AddTriangleAction(final Geoset geoset, final Collection<Triangle> triangles) {
		this.geoset = geoset;
		this.triangles = triangles;
	}

	@Override
	public void undo() {
		for (final Triangle triangle : triangles) {
			geoset.remove(triangle);
			for (final GeosetVertex geosetVertex : triangle.getVerts()) {
				geosetVertex.getTriangles().remove(triangle);
			}
		}
	}

	@Override
	public void redo() {
		for (final Triangle triangle : triangles) {
			geoset.add(triangle);
			for (final GeosetVertex geosetVertex : triangle.getVerts()) {
				geosetVertex.getTriangles().add(triangle);
			}
		}
	}

	@Override
	public String actionName() {
		return "add faces";
	}

}
