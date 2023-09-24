package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.List;
import java.util.Set;

public final class AdvancedCloneAction implements UndoAction {
	private final ModelView modelView;
	private final ModelStructureChangeListener modelStructureChangeListener;

	private final List<IdObject> selBones;
	private final List<GeosetVertex> newVertices;
	private final List<Triangle> newTriangles;
	private final List<IdObject> newBones;

	private final Set<GeosetVertex> newSelection;
	private final List<GeosetVertex> sourceNonPivots;
	private final List<IdObject> sourcePivots;
	private final Set<IdObject> newSelectionPivots;

	public AdvancedCloneAction(ModelView modelView,
	                           List<GeosetVertex> sourceNonPivots,
	                           List<IdObject> sourcePivots,
	                           ModelStructureChangeListener modelStructureChangeListener,
	                           List<IdObject> selBones,
	                           List<GeosetVertex> newVertices,
	                           List<Triangle> newTriangles,
	                           List<IdObject> newBones,
	                           Set<GeosetVertex> newSelection,
	                           Set<IdObject> newSelectionPivots) {
		this.modelView = modelView;
		this.sourceNonPivots = sourceNonPivots;
		this.sourcePivots = sourcePivots;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.selBones = selBones;
		this.newVertices = newVertices;
		this.newTriangles = newTriangles;
		this.newBones = newBones;
		this.newSelection = newSelection;
		this.newSelectionPivots = newSelectionPivots;
	}

	@Override
	public AdvancedCloneAction undo() {
		for (final GeosetVertex gv : newVertices) {
			gv.getGeoset().remove(gv);
		}
		for (final Triangle tri : newTriangles) {
			tri.getGeoset().remove(tri);
		}
		for (final IdObject b : newBones) {
			modelView.getModel().remove(b);
		}
		modelView.setSelectedVertices(sourceNonPivots);
		modelView.setSelectedIdObjects(newSelectionPivots);
		modelStructureChangeListener.nodesUpdated();
		return this;
	}

	@Override
	public AdvancedCloneAction redo() {
		for (final GeosetVertex gv : newVertices) {
			gv.getGeoset().add(gv);
		}
		for (final Triangle tri : newTriangles) {
			tri.getGeoset().add(tri);
		}
		for (final IdObject b : newBones) {
			modelView.getModel().add(b);
		}
		modelView.setSelectedVertices(sourceNonPivots);
		modelView.setSelectedIdObjects(sourcePivots);
		modelStructureChangeListener.nodesUpdated();
		return this;
	}

	@Override
	public String actionName() {
		return "clone";
	}

}
