package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Something to undo when you deleted something important.
 *
 * Eric Theller 6/11/2012
 */
public class DeleteAction implements UndoAction {
	private final List<Vec3> selection;
	private final List<Vec3> deleted;
	private final List<Triangle> deletedTris;
	private final VertexSelectionHelper vertexSelectionHelper;

	public DeleteAction(Collection<? extends Vec3> selection, List<Triangle> deletedTris,
	                    VertexSelectionHelper vertexSelectionHelper) {
		this.vertexSelectionHelper = vertexSelectionHelper;
		this.selection = new ArrayList<>(selection);
		this.deleted = new ArrayList<>(selection);
		this.deletedTris = deletedTris;
	}

	@Override
	public void redo() {
		for (Vec3 vec3 : deleted) {
			if (vec3.getClass() == GeosetVertex.class) {
				GeosetVertex gv = (GeosetVertex) vec3;
				gv.getGeoset().remove(gv);
			}
		}
		for (Triangle t : deletedTris) {
			t.getGeoset().removeTriangle(t);
			for (GeosetVertex vertex : t.getAll()) {
				vertex.removeTriangle(t);
			}
		}
		vertexSelectionHelper.selectVertices(new ArrayList<>());
	}

	@Override
	public void undo() {
		for (Vec3 vec3 : deleted) {
			if (vec3.getClass() == GeosetVertex.class) {
				GeosetVertex gv = (GeosetVertex) vec3;
				gv.getGeoset().addVertex(gv);
			}
		}
		for (Triangle t : deletedTris) {
			t.getGeoset().addTriangle(t);
			for (GeosetVertex vertex : t.getAll()) {
				vertex.addTriangle(t);
			}
		}
		vertexSelectionHelper.selectVertices(selection);
	}

	@Override
	public String actionName() {
		return "delete vertices";
	}
}
