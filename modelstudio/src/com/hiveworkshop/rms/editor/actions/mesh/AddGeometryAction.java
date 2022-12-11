package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

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
		if(addToGeoset){
			vertices.forEach(vertex -> vertex.setGeoset(geoset));
			triangles.forEach(triangle -> triangle.setGeoset(geoset));
		}
	}

	@Override
	public UndoAction undo() {
		geoset.remove(vertices);
		geoset.removeTriangles(triangles);
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		geoset.addVerticies(vertices);
		geoset.addTriangles(triangles);
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Add Geometry";
	}

}
