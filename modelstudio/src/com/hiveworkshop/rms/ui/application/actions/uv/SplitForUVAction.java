package com.hiveworkshop.rms.ui.application.actions.uv;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public final class SplitForUVAction<T> implements UndoAction {

	private List<Triangle> trianglesMovedToSeparateGeo;
	private final List<Geoset> geosetsModified;
	private final EditableModel model;
	private final Collection<Triangle> trisToSeparate;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final SelectionManager<T> selectionManager;
	private final Collection<T> selection;
	private final Collection<Vec3> newVerticesToSelect;
	private final VertexSelectionHelper vertexSelectionHelper;

	public SplitForUVAction(Collection<Triangle> trisToSeparate, EditableModel model,
	                        ModelStructureChangeListener modelStructureChangeListener, SelectionManager<T> selectionManager,
	                        VertexSelectionHelper vertexSelectionHelper) {
		this.trisToSeparate = trisToSeparate;
		this.model = model;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.selectionManager = selectionManager;
		this.vertexSelectionHelper = vertexSelectionHelper;
		this.geosetsModified = new ArrayList<>();
		this.newVerticesToSelect = new ArrayList<>();
		Set<GeosetVertex> verticesInTheTriangles = new HashSet<>();
		Set<Geoset> geosetsToCopy = new HashSet<>();
		for (Triangle tri : trisToSeparate) {
			verticesInTheTriangles.addAll(Arrays.asList(tri.getVerts()));
			geosetsToCopy.add(tri.getGeoset());
		}
		Map<Geoset, Geoset> oldGeoToNewGeo = new HashMap<>();
		Map<GeosetVertex, GeosetVertex> oldVertToNewVert = new HashMap<>();
		geosetsModified.addAll(geosetsToCopy);
		for (GeosetVertex vertex : verticesInTheTriangles) {
			GeosetVertex copy = new GeosetVertex(vertex);
			Geoset newGeoset = oldGeoToNewGeo.get(vertex.getGeoset());
			copy.setGeoset(newGeoset);
			newGeoset.add(copy);
			oldVertToNewVert.put(vertex, copy);
			newVerticesToSelect.add(copy);
		}
		for (Triangle tri : trisToSeparate) {
			GeosetVertex a, b, c;
			a = oldVertToNewVert.get(tri.get(0));
			b = oldVertToNewVert.get(tri.get(1));
			c = oldVertToNewVert.get(tri.get(2));
			Geoset newGeoset = oldGeoToNewGeo.get(tri.getGeoset());
			Triangle newTriangle = new Triangle(a, b, c, newGeoset);
			newGeoset.add(newTriangle);
//			a.addTriangle(newTriangle);
//			b.addTriangle(newTriangle);
//			c.addTriangle(newTriangle);
		}
		selection = new ArrayList<>(selectionManager.getSelection());
	}

	@Override
	public void undo() {
		for (Triangle tri : trisToSeparate) {
			Geoset geoset = tri.getGeoset();
			for (GeosetVertex gv : tri.getVerts()) {
				gv.addTriangle(tri);
				if (!geoset.getVertices().contains(gv)) {
					geoset.add(gv);
				}
			}
			geoset.add(tri);
		}
		selectionManager.setSelection(selection);
	}

	@Override
	public void redo() {
		for (Triangle tri : trisToSeparate) {
			Geoset geoset = tri.getGeoset();
			for (GeosetVertex gv : tri.getVerts()) {
				gv.removeTriangle(tri);
				if (gv.getTriangles().isEmpty()) {
					geoset.remove(gv);
				}
			}
			geoset.removeTriangle(tri);
		}
		selectionManager.removeSelection(selection);
		vertexSelectionHelper.selectVertices(newVerticesToSelect);
	}

	@Override
	public String actionName() {
		return "split geoset";
	}

}
