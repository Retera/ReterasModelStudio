package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.*;

/**
 * Something to undo when you deleted something important.
 *
 * Eric Theller 6/11/2012
 */
public class DeleteAction implements UndoAction {
	private final Set<GeosetVertex> affectedVerts;
	private final Set<Triangle> affectedTris;
	private final List<Geoset> emptyGeosets;
	private final ModelView modelView;
	private final ModelStructureChangeListener structureChangeListener;
	private final EditableModel model;


	public DeleteAction(Collection<GeosetVertex> selection, ModelStructureChangeListener structureChangeListener, ModelView modelView) {
		this.model = modelView.getModel();
		this.structureChangeListener = structureChangeListener;
		this.modelView = modelView;

		this.affectedVerts = new HashSet<>(selection);

		this.affectedTris = getTrisToRemove(affectedVerts);

		this.emptyGeosets = new ArrayList<>();
	}

	@Override
	public UndoAction redo() {
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
		structureChangeListener.geosetsUpdated();
		return this;
	}

	@Override
	public UndoAction undo() {
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
		structureChangeListener.geosetsUpdated();

		modelView.setSelectedVertices(affectedVerts);
		return this;
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


	private Set<Triangle> getTrisToRemove(Set<GeosetVertex> selection) {
		Set<Triangle> trisToDelete = new HashSet<>();
		for (GeosetVertex vertex : selection) {
			trisToDelete.addAll(vertex.getTriangles());
		}
		return trisToDelete;
	}

	@Override
	public String actionName() {
		return "delete vertices";
	}
}
