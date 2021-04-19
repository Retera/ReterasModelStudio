package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.Collection;

public class AddTriangleAction implements UndoAction {
	private final Geoset geoset;
	private final Collection<Triangle> triangles;

	public AddTriangleAction(Geoset geoset, Collection<Triangle> triangles) {
		this.geoset = geoset;
		this.triangles = triangles;
	}

	@Override
	public void undo() {
		for (Triangle triangle : triangles) {
			geoset.remove(triangle);
			for (GeosetVertex geosetVertex : triangle.getVerts()) {
				geosetVertex.removeTriangle(triangle);
			}
		}
	}

	@Override
	public void redo() {
		for (Triangle triangle : triangles) {
			geoset.add(triangle);
			for (GeosetVertex geosetVertex : triangle.getVerts()) {
				if (!geosetVertex.hasTriangle(triangle)) {
					geosetVertex.addTriangle(triangle);
				}
			}
		}
	}

	@Override
	public String actionName() {
		return "add faces";
	}

}
