package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec2;

import java.util.Collection;

public class AddGeometryAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final Geoset geoset;
	private final Collection<Triangle> triangles;
	private final Collection<GeosetVertex> vertices;

	public AddGeometryAction(Geoset geoset,
	                         Collection<GeosetVertex> vertices,
	                         Collection<Triangle> triangles,
	                         ModelStructureChangeListener changeListener) {
		this(geoset, vertices, triangles, false, changeListener);
	}
	public AddGeometryAction(Geoset geoset,
	                         Collection<GeosetVertex> vertices,
	                         Collection<Triangle> triangles, boolean addToGeoset,
	                         ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.geoset = geoset;
		this.vertices = vertices;
		this.triangles = triangles;
		if (addToGeoset) {
			if (1 < geoset.numUVLayers()) {
				vertices.forEach(vertex -> {
					for (int i = vertex.getTverts().size(); i <= geoset.numUVLayers(); i++)
						vertex.addTVertex(new Vec2(vertex.getTVertex(0)));
				});
			}
			vertices.forEach(vertex -> vertex.setGeoset(geoset));
			triangles.forEach(triangle -> triangle.setGeoset(geoset));
		}
	}

	@Override
	public UndoAction undo() {
		geoset.remove(vertices);
		geoset.removeTriangles(triangles);
		triangles.forEach(Triangle::removeFromVerts);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		geoset.addVerticies(vertices);
		geoset.addTriangles(triangles);
		triangles.forEach(Triangle::addToVerts);
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Add Geometry";
	}

}
