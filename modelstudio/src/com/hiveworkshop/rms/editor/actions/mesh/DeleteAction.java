package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

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
	private final ModelStructureChangeListener changeListener;
	private final EditableModel model;

	public DeleteAction(Collection<GeosetVertex> selection, ModelView modelView, boolean onlyTriangles, ModelStructureChangeListener changeListener) {
		this.model = modelView.getModel();
		this.changeListener = changeListener;
		this.modelView = modelView;

		if(onlyTriangles){
			Set<GeosetVertex> verts = new HashSet<>(selection);
			this.affectedTris = getFullySelectedTris(getAllAffectedTris(verts), verts);
			this.affectedVerts = getVertsOnlyInFullySelectedTris(affectedTris);
		} else {
			this.affectedVerts = new HashSet<>(selection);

			this.affectedTris = getAllAffectedTris(affectedVerts);

		}
		this.emptyGeosets = new ArrayList<>();
	}


	private Set<Triangle> getAllAffectedTris(Set<GeosetVertex> selection) {
		Set<Triangle> affectedTris = new HashSet<>();
		for (GeosetVertex vertex : selection) {
			affectedTris.addAll(vertex.getTriangles());
		}
		return affectedTris;
	}
	private Set<Triangle> getFullySelectedTris(Set<Triangle> affectedTris, Set<GeosetVertex> selection) {
		Set<Triangle> fullySelectedTris = new HashSet<>();
		for (Triangle triangle : affectedTris) {
			if(selection.contains(triangle.get(0))
					&& selection.contains(triangle.get(1))
					&& selection.contains(triangle.get(2))){
				fullySelectedTris.add(triangle);
			}
		}
		return fullySelectedTris;
	}
	private Set<GeosetVertex> getVertsOnlyInFullySelectedTris(Set<Triangle> fullySelectedTris) {
		Set<GeosetVertex> verts = new HashSet<>();
		for (Triangle triangle : fullySelectedTris) {
			for(GeosetVertex vertex : triangle.getVerts()){
				if(fullySelectedTris.containsAll(vertex.getTriangles())){
					verts.add(vertex);
				}
			}
		}
		return verts;
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
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
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
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}

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

	@Override
	public String actionName() {
		return "delete vertices";
	}
}
