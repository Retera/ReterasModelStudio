package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.LinkedHashSet;
import java.util.Set;

public class MergeGeosetsAction implements UndoAction {
	private final EditableModel model;
	private final Geoset recGeoset;
	private final Geoset donGeoset;
	private final int orgNum;
	private final Set<GeosetVertex> donVerts;
	private final Set<Triangle> donTris;
	private final ModelStructureChangeListener changeListener;
	private final String actionName;

	public MergeGeosetsAction(Geoset recGeoset, Geoset donGeoset, EditableModel model, ModelStructureChangeListener changeListener) {
		this(recGeoset, donGeoset, model.getGeosetId(donGeoset), model, changeListener);
	}
	public MergeGeosetsAction(Geoset recGeoset, Geoset donGeoset, int addNum, EditableModel model, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.model = model;
		this.recGeoset = recGeoset;
		this.donGeoset = donGeoset;
		this.orgNum = addNum;
		donVerts = new LinkedHashSet<>(donGeoset.getVertices());
		donTris = new LinkedHashSet<>(donGeoset.getTriangles());

		actionName = "Merge Geoset #" + model.getGeosetId(donGeoset) + " into Geoset #" + model.getGeosetId(recGeoset);
	}

	@Override
	public MergeGeosetsAction undo() {
		for (GeosetVertex vertex : donVerts) {
			vertex.setGeoset(donGeoset);
			recGeoset.remove(vertex);
		}
		for (Triangle triangle : donTris) {
			triangle.setGeoset(donGeoset);
		}
		recGeoset.remove(donVerts);
		recGeoset.removeTriangles(donTris);

		model.add(donGeoset, orgNum);

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public MergeGeosetsAction redo() {
		for (GeosetVertex vertex : donVerts) {
			vertex.setGeoset(recGeoset);
		}
		for (Triangle triangle : donTris) {
			triangle.setGeoset(recGeoset);
		}
		recGeoset.addVerticies(donVerts);
		recGeoset.addTriangles(donTris);

		model.remove(donGeoset);

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
