package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import java.util.List;
import java.util.Set;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Vertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public final class AdvancedCloneAction implements UndoAction {
	private final ModelView model;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final VertexSelectionHelper vertexSelectionHelper;

	private final List<IdObject> selBones;
	private final List<GeosetVertex> newVertices;
	private final List<Triangle> newTriangles;
	private final List<IdObject> newBones;

	private final Set<Vertex> newSelection;
	private final List<Vertex> sourceNonPivots;
	private final List<Vertex> sourcePivots;
	private final VertexSelectionHelper pivotSelectionHelper;
	private final Set<Vertex> newSelectionPivots;

	public AdvancedCloneAction(final ModelView model, final List<Vertex> sourceNonPivots,
			final List<Vertex> sourcePivots, final ModelStructureChangeListener modelStructureChangeListener,
			final VertexSelectionHelper vertexSelectionHelper, final VertexSelectionHelper pivotSelectionHelper,
			final List<IdObject> selBones, final List<GeosetVertex> newVertices, final List<Triangle> newTriangles,
			final List<IdObject> newBones, final Set<Vertex> newSelection, final Set<Vertex> newSelectionPivots) {
		this.model = model;
		this.sourceNonPivots = sourceNonPivots;
		this.sourcePivots = sourcePivots;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.vertexSelectionHelper = vertexSelectionHelper;
		this.pivotSelectionHelper = pivotSelectionHelper;
		this.selBones = selBones;
		this.newVertices = newVertices;
		this.newTriangles = newTriangles;
		this.newBones = newBones;
		this.newSelection = newSelection;
		this.newSelectionPivots = newSelectionPivots;
	}

	@Override
	public void undo() {
		for (final GeosetVertex gv : newVertices) {
			gv.getGeoset().remove(gv);
		}
		for (final Triangle tri : newTriangles) {
			tri.getGeoset().remove(tri);
		}
		for (final IdObject b : newBones) {
			model.getModel().remove(b);
		}
		vertexSelectionHelper.selectVertices(sourceNonPivots);
		pivotSelectionHelper.selectVertices(sourcePivots);
		modelStructureChangeListener.nodesRemoved(newBones);
	}

	@Override
	public void redo() {
		for (final GeosetVertex gv : newVertices) {
			gv.getGeoset().add(gv);
		}
		for (final Triangle tri : newTriangles) {
			tri.getGeoset().add(tri);
		}
		for (final IdObject b : newBones) {
			model.getModel().add(b);
		}
		modelStructureChangeListener.nodesAdded(newBones);
		vertexSelectionHelper.selectVertices(newSelection);
		pivotSelectionHelper.selectVertices(newSelectionPivots);
	}

	@Override
	public String actionName() {
		return "clone";
	}

}
