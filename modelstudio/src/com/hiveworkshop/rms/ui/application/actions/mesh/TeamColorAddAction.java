package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public final class TeamColorAddAction<T> implements UndoAction {

	private List<Geoset> geosetsCreated;
	private EditableModel model;
	//	private Collection<Triangle> trisToSeparate;
	private Set<Triangle> trisToSeparate1;
	private ModelStructureChangeListener modelStructureChangeListener;
	private SelectionManager<T> selectionManager;
	private Collection<T> selection;
	private Collection<Vec3> newVerticesToSelect;
	private VertexSelectionHelper vertexSelectionHelper;

	public TeamColorAddAction(Collection<Triangle> trisToSeparate, EditableModel model,
	                          ModelStructureChangeListener modelStructureChangeListener, SelectionManager<T> selectionManager,
	                          VertexSelectionHelper vertexSelectionHelper) {
		this.trisToSeparate1 = new HashSet<>(trisToSeparate);
		this.model = model;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.selectionManager = selectionManager;
		this.vertexSelectionHelper = vertexSelectionHelper;
		geosetsCreated = new ArrayList<>();
		newVerticesToSelect = new ArrayList<>();
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
				geosetCreated.setGeosetAnim(new GeosetAnim(geosetCreated, geosetAnim));
			}
			geosetCreated.setParentModel(model);
			Material newMaterial = new Material(geoset.getMaterial());
			if (newMaterial.getLayers().get(0).getFilterMode() == FilterMode.NONE) {
				newMaterial.getLayers().get(0).setFilterMode(FilterMode.BLEND);
			}
			Layer teamColorLayer = new Layer(FilterMode.NONE.toString(), new Bitmap("", 1));
			teamColorLayer.setUnshaded(true);
			if (geoset.getMaterial().firstLayer().getTwoSided()) {
				teamColorLayer.setTwoSided(true);
			}
			newMaterial.addLayer(0, teamColorLayer);
			geosetCreated.setMaterial(newMaterial);
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


	public TeamColorAddAction(Collection<GeosetVertex> geosetVertsToSep, EditableModel model,
	                          ModelStructureChangeListener modelStructureChangeListener, SelectionManager<T> selectionManager,
	                          VertexSelectionHelper vertexSelectionHelper, ModelView modelView) {
		this.trisToSeparate1 = new HashSet<>();
		this.model = model;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.selectionManager = selectionManager;
		this.vertexSelectionHelper = vertexSelectionHelper;
		geosetsCreated = new ArrayList<>();
		newVerticesToSelect = new ArrayList<>();
		Set<GeosetVertex> verticesInTheTriangles = new HashSet<>(geosetVertsToSep);
		Set<Geoset> geosetsToCopy = new HashSet<>();

		for (GeosetVertex vert : verticesInTheTriangles) {
			for (Triangle triangle : vert.getTriangles()) {
				if (verticesInTheTriangles.containsAll(Arrays.asList(triangle.getVerts()))) {
					trisToSeparate1.add(triangle);
					geosetsToCopy.add(triangle.getGeoset());
				}
			}
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
				geosetCreated.setGeosetAnim(new GeosetAnim(geosetCreated, geosetAnim));
			}
			geosetCreated.setParentModel(model);
			Material newMaterial = new Material(geoset.getMaterial());
			if (newMaterial.getLayers().get(0).getFilterMode() == FilterMode.NONE) {
				newMaterial.getLayers().get(0).setFilterMode(FilterMode.BLEND);
			}
			Layer teamColorLayer = new Layer(FilterMode.NONE.toString(), new Bitmap("", 1));
			teamColorLayer.setUnshaded(true);
			if (geoset.getMaterial().firstLayer().getTwoSided()) {
				teamColorLayer.setTwoSided(true);
			}
			newMaterial.addLayer(0, teamColorLayer);
			geosetCreated.setMaterial(newMaterial);
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
		for (Triangle tri : trisToSeparate1) {
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
		}
		modelStructureChangeListener.geosetsRemoved(geosetsCreated);
		for (Triangle tri : trisToSeparate1) {
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
		}
		for (Triangle tri : trisToSeparate1) {
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
		return "add team color layer";
	}

}
