package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.*;

public final class TeamColorAddAction implements UndoAction {

	private final List<Geoset> geosetsCreated;
	private final EditableModel model;
	private final Set<Triangle> trisToSeparate;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final Collection<GeosetVertex> selection;
	private final Collection<GeosetVertex> newVerticesToSelect;
	private final ModelView modelView;

	public TeamColorAddAction(Collection<GeosetVertex> geosetVertsToSep,
	                          ModelView modelView,
	                          ModelStructureChangeListener modelStructureChangeListener) {
		this.trisToSeparate = new HashSet<>();
		this.modelView = modelView;
		this.model = modelView.getModel();
		this.modelStructureChangeListener = modelStructureChangeListener;
		geosetsCreated = new ArrayList<>();
		newVerticesToSelect = new ArrayList<>();
		Set<GeosetVertex> verticesInTheTriangles = new HashSet<>(geosetVertsToSep);
		Set<Geoset> geosetsToCopy = new HashSet<>();

		for (GeosetVertex vert : verticesInTheTriangles) {
			for (Triangle triangle : vert.getTriangles()) {
				if (verticesInTheTriangles.containsAll(Arrays.asList(triangle.getVerts()))) {
					trisToSeparate.add(triangle);
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
		selection = new ArrayList<>(modelView.getSelectedVertices());
	}

	@Override
	public UndoAction undo() {
		for (Geoset geoset : geosetsCreated) {
			model.remove(geoset);
		}
		modelStructureChangeListener.geosetsUpdated();
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
		modelView.setSelectedVertices(selection);
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Geoset geoset : geosetsCreated) {
			model.add(geoset);
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
		modelStructureChangeListener.geosetsUpdated();
		modelView.setSelectedVertices(newVerticesToSelect);
		return this;
	}

	@Override
	public String actionName() {
		return "add team color layer";
	}

}
