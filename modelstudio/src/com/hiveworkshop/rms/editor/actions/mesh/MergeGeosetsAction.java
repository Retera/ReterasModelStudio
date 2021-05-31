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
	Geoset recGeoset;
	Geoset donGeoset;
	Set<GeosetVertex> donVerts;
	Set<Triangle> donTris;
	ModelView modelView;
	ModelStructureChangeListener changeListener;

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
		if (donGeoset.getGeosetAnim() != null) {
			modelView.getModel().add(donGeoset.getGeosetAnim());
		}
		changeListener.geosetsUpdated();
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
		if (donGeoset.getGeosetAnim() != null) {
			modelView.getModel().remove(donGeoset.getGeosetAnim());
		}
		changeListener.geosetsUpdated();
		return this;
	}

	@Override
	public String actionName() {
		return "merge geosets";
	}
}
