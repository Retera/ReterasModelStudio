package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public final class SplitGeosetAction<T> implements UndoAction {

	private List<Triangle> trianglesMovedToSeparateGeo;
	private final List<Geoset> geosetsCreated;
	private final EditableModel model;
	private final Collection<Triangle> trisToSeparate;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final SelectionManager<T> selectionManager;
	private final Collection<T> selection;
	private final Collection<Vec3> newVerticesToSelect;
	private final VertexSelectionHelper vertexSelectionHelper;

	public SplitGeosetAction(Collection<Triangle> trisToSeparate, EditableModel model,
	                         ModelStructureChangeListener modelStructureChangeListener, SelectionManager<T> selectionManager,
	                         VertexSelectionHelper vertexSelectionHelper) {
		this.trisToSeparate = trisToSeparate;
		this.model = model;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.selectionManager = selectionManager;
		this.vertexSelectionHelper = vertexSelectionHelper;
		this.geosetsCreated = new ArrayList<>();
		this.newVerticesToSelect = new ArrayList<>();
		Set<GeosetVertex> verticesInTheTriangles = new HashSet<>();
		Set<Geoset> geosetsToCopy = new HashSet<>();
		for (Triangle tri : trisToSeparate) {
			verticesInTheTriangles.addAll(Arrays.asList(tri.getVerts()));
			geosetsToCopy.add(tri.getGeoset());
		}
		Map<Geoset, Geoset> oldGeoToNewGeo = new HashMap<>();
		Map<GeosetVertex, GeosetVertex> oldVertToNewVert = new HashMap<>();
		for (Geoset geoset : geosetsToCopy) {
			Geoset geosetCreated = new Geoset();
			if (geoset.getExtents() != null) {
				geosetCreated.setExtents(new ExtLog(geoset.getExtents()));
			}
			for (Animation anim : geoset.getAnims()) {
				geosetCreated.add(new Animation(anim));
			}
			geosetCreated.setUnselectable(geoset.getUnselectable());
			geosetCreated.setSelectionGroup(geoset.getSelectionGroup());
			GeosetAnim geosetAnim = geoset.getGeosetAnim();
			if (geosetAnim != null) {
				GeosetAnim createdGeosetAnim = new GeosetAnim(geosetCreated, geosetAnim);
				geosetCreated.setGeosetAnim(createdGeosetAnim);
			}
			geosetCreated.setParentModel(model);
			geosetCreated.setMaterial(geoset.getMaterial());
			oldGeoToNewGeo.put(geoset, geosetCreated);
			geosetsCreated.add(geosetCreated);
		}
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
		for (Geoset geoset : geosetsCreated) {
			model.remove(geoset);
			if (geoset.getGeosetAnim() != null) {
				model.remove(geoset.getGeosetAnim());
			}
		}
		modelStructureChangeListener.geosetsRemoved(geosetsCreated);
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
		for (Geoset geoset : geosetsCreated) {
			model.add(geoset);
			if (geoset.getGeosetAnim() != null) {
				model.add(geoset.getGeosetAnim());
			}
		}
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
		modelStructureChangeListener.geosetsAdded(geosetsCreated);
		selectionManager.removeSelection(selection);
		vertexSelectionHelper.selectVertices(newVerticesToSelect);
	}

	@Override
	public String actionName() {
		return "split geoset";
	}

}
