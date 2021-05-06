package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
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
	private final List<GeosetVertex> affectedVerts;
	private final List<Triangle> affectedTris;
	private final List<Geoset> emptyGeosets;
	private final VertexSelectionHelper vertexSelectionHelper;
	private final ModelStructureChangeListener structureChangeListener;
	private final EditableModel model;


	public DeleteAction(EditableModel model, Collection<? extends Vec3> selection, ModelStructureChangeListener structureChangeListener, VertexSelectionHelper vertexSelectionHelper) {
		this.model = model;
		this.structureChangeListener = structureChangeListener;
		this.vertexSelectionHelper = vertexSelectionHelper;
		this.selection = new ArrayList<>(selection);

		this.affectedVerts = new ArrayList<>();
		selection.forEach(vert -> affectedVerts.add((GeosetVertex) vert));

		this.affectedTris = getTrisToRemove(affectedVerts);

		this.emptyGeosets = new ArrayList<>();
	}

	@Override
	public void redo() {
		for (GeosetVertex gv : affectedVerts) {
			gv.getGeoset().remove(gv);
		}
		for (Triangle t : affectedTris) {
			t.getGeoset().removeTriangle(t);
			for (GeosetVertex vertex : t.getAll()) {
				vertex.removeTriangle(t);
			}
		}
		checkForEmptyGeosets();

		for (Geoset geoset : emptyGeosets) {
			model.remove(geoset);
			if (geoset.getGeosetAnim() != null) {
				model.remove(geoset.getGeosetAnim());
			}
		}

		if (!emptyGeosets.isEmpty()) {
			structureChangeListener.geosetsRemoved(emptyGeosets);
		}

		vertexSelectionHelper.selectVertices(new ArrayList<>());
	}

	@Override
	public void undo() {
		for (GeosetVertex gv : affectedVerts) {
			gv.getGeoset().addVertex(gv);
		}
		for (Triangle t : affectedTris) {
			t.getGeoset().addTriangle(t);
			for (GeosetVertex vertex : t.getAll()) {
				vertex.addTriangle(t);
			}
		}
		for (Geoset geoset : emptyGeosets) {
			model.add(geoset);
			if (geoset.getGeosetAnim() != null) {
				model.add(geoset.getGeosetAnim());
			}
		}
		if (!emptyGeosets.isEmpty()) {
			structureChangeListener.geosetsAdded(emptyGeosets);
		}

		vertexSelectionHelper.selectVertices(selection);
	}

	private void checkForEmptyGeosets() {
		if (emptyGeosets.isEmpty()) {
			for (Geoset geoset : model.getGeosets()) {
				if (geoset.isEmpty()) {
					emptyGeosets.add(geoset);
				}
			}
		}
	}


	private List<Triangle> getTrisToRemove(List<GeosetVertex> selection) {
		List<Triangle> trisToDelete = new ArrayList<>();
		for (GeosetVertex vertex : selection) {
			for (Triangle t : vertex.getTriangles()) {
				if (!trisToDelete.contains(t)) {
					trisToDelete.add(t);
				}
			}
		}
		return trisToDelete;
	}

	@Override
	public String actionName() {
		return "delete vertices";
	}
}
