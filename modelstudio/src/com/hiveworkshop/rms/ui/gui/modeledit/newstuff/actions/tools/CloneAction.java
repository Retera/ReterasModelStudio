package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;
import java.util.Set;

public final class CloneAction implements UndoAction {
	private final ModelView model;
	private final List<Vec3> source;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final VertexSelectionHelper vertexSelectionHelper;

	private final List<IdObject> selBones;
	private final List<GeosetVertex> newVertices;
	private final List<Triangle> newTriangles;
	private final List<IdObject> newBones;

	private final Set<Vec3> newSelection;

	public CloneAction(ModelView model,
	                   List<Vec3> source,
	                   ModelStructureChangeListener modelStructureChangeListener,
	                   VertexSelectionHelper vertexSelectionHelper,
	                   List<IdObject> selBones,
	                   List<GeosetVertex> newVertices,
	                   List<Triangle> newTriangles,
	                   List<IdObject> newBones,
	                   Set<Vec3> newSelection) {
		this.model = model;
		this.source = source;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.vertexSelectionHelper = vertexSelectionHelper;
		this.selBones = selBones;
		this.newVertices = newVertices;
		this.newTriangles = newTriangles;
		this.newBones = newBones;
		this.newSelection = newSelection;
	}

	@Override
	public void undo() {
		for (GeosetVertex gv : newVertices) {
			gv.getGeoset().remove(gv);
		}
		for (Triangle tri : newTriangles) {
			tri.getGeoset().remove(tri);
		}
		for (IdObject b : newBones) {
			model.getModel().remove(b);
		}
		vertexSelectionHelper.selectVertices(source);
		modelStructureChangeListener.nodesUpdated();
	}

	@Override
	public void redo() {
		for (GeosetVertex gv : newVertices) {
			gv.getGeoset().add(gv);
		}
		for (Triangle tri : newTriangles) {
			tri.getGeoset().add(tri);
		}
		for (IdObject b : newBones) {
			model.getModel().add(b);
		}
		vertexSelectionHelper.selectVertices(newSelection);
		modelStructureChangeListener.nodesUpdated();
	}

	@Override
	public String actionName() {
		return "clone";
	}

}
