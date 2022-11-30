package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.HashSet;
import java.util.Set;

public class MergeGeosetsAction implements UndoAction {
	private final Geoset recGeoset;
	private final Geoset donGeoset;
	private final Set<GeosetVertex> donVerts;
	private final Set<Triangle> donTris;
	private final ModelView modelView;
	private final ModelStructureChangeListener changeListener;

	public MergeGeosetsAction(Geoset recGeoset, Geoset donGeoset, ModelView modelView, ModelStructureChangeListener changeListener) {
		this.modelView = modelView;
		this.recGeoset = recGeoset;
		this.donGeoset = donGeoset;
		donVerts = new HashSet<>(donGeoset.getVertices());
		donTris = new HashSet<>(donGeoset.getTriangles());
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		for (GeosetVertex vertex : donVerts) {
			vertex.setGeoset(donGeoset);
			recGeoset.remove(vertex);
		}
		for (Triangle triangle : donTris) {
			triangle.setGeoset(donGeoset);
		}
		recGeoset.remove(donVerts);
		recGeoset.removeTriangles(donTris);

		modelView.getModel().add(donGeoset);

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (GeosetVertex vertex : donVerts) {
			vertex.setGeoset(recGeoset);
		}
		for (Triangle triangle : donTris) {
			triangle.setGeoset(recGeoset);
		}
		recGeoset.addVerticies(donVerts);
		recGeoset.addTriangles(donTris);

		modelView.getModel().remove(donGeoset);

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Merge Geosets";
	}
}
