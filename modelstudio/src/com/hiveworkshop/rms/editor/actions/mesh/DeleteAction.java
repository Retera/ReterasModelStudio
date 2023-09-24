package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Something to undo when you deleted something important.
 *
 * Eric Theller 6/11/2012
 */
public class DeleteAction implements UndoAction {
	private final Set<GeosetVertex> affectedVerts;
	private final Set<GeosetVertex> selectedVerts;
	private final Set<Triangle> affectedTris;
	private Map<Geoset, Integer> emptyGeosets;
	private final ModelView modelView;
	private final ModelStructureChangeListener changeListener;
	private final EditableModel model;
	private final String actionName;

	public DeleteAction(Collection<GeosetVertex> selection, ModelView modelView, boolean onlyTriangles, ModelStructureChangeListener changeListener) {
		this.model = modelView.getModel();
		this.changeListener = changeListener;
		this.modelView = modelView;

		if (onlyTriangles) {
			Set<GeosetVertex> verts = new HashSet<>(selection);
			this.affectedTris = getFullySelectedTris(verts);
			this.affectedVerts = verts.stream().filter(gv -> affectedTris.containsAll(gv.getTriangles())).collect(Collectors.toSet());
			actionName = "Delete "
					+ affectedTris.size()
					+ (affectedTris.size() == 1 ? " Triangle" : " Triangles");
		} else {
			this.affectedVerts = new HashSet<>(selection);
			this.affectedTris = getAllAffectedTris(affectedVerts);
			actionName = "Delete "
					+ affectedVerts.size()
					+ (affectedVerts.size() == 1 ? " Vertex" : " Vertices");

		}
		this.selectedVerts = affectedVerts.stream().filter(modelView::isSelected).collect(Collectors.toSet());
	}


	private Set<Triangle> getAllAffectedTris(Set<GeosetVertex> selection) {
		Set<Triangle> affectedTris = new HashSet<>();
		selection.forEach(gv -> affectedTris.addAll(gv.getTriangles()));
		return affectedTris;
	}
	private Set<Triangle> getFullySelectedTris(Set<GeosetVertex> selection) {
		Set<Triangle> affectedTris = getAllAffectedTris(selection);
		return affectedTris.stream().filter(t -> triSelected(t, selection)).collect(Collectors.toSet());
	}

	private boolean triSelected(Triangle triangle, Set<GeosetVertex> selection) {
		return selection.contains(triangle.get(0))
				&& selection.contains(triangle.get(1))
				&& selection.contains(triangle.get(2));
	}


	@Override
	public DeleteAction redo() {
		for (GeosetVertex gv : affectedVerts) {
			gv.getGeoset().remove(gv);
		}
		for (Triangle t : affectedTris) {
			t.getGeoset().removeTriangle(t);
			for (GeosetVertex vertex : t.getVerts()) {
				vertex.removeTriangle(t);
			}
		}
		checkForEmptyGeosets();

		for (Geoset geoset : emptyGeosets.keySet()) {
			model.remove(geoset);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public DeleteAction undo() {
		for (GeosetVertex gv : affectedVerts) {
			gv.getGeoset().addVertex(gv);
		}
		for (Triangle t : affectedTris) {
			t.getGeoset().addTriangle(t);
			for (GeosetVertex vertex : t.getVerts()) {
				vertex.addTriangle(t);
			}
		}
		for (Geoset geoset : emptyGeosets.keySet()) {
			model.add(geoset, emptyGeosets.get(geoset));
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}

		modelView.addSelectedVertices(selectedVerts);
		return this;
	}

	private void checkForEmptyGeosets() {
		if (emptyGeosets == null) {
			emptyGeosets = model.getGeosets().stream()
					.filter(Geoset::isEmpty)
					.collect(Collectors.toMap(geoset -> geoset, model::getGeosetId));
		}
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
