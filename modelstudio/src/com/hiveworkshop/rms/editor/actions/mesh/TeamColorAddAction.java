package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public final class TeamColorAddAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final List<Geoset> geosetsCreated;
	private final EditableModel model;
	private final Set<Triangle> trisToSeparate;
	private final Collection<GeosetVertex> selection;
	private final Collection<GeosetVertex> newVerticesToSelect;
	private final ModelView modelView;

	public TeamColorAddAction(Collection<GeosetVertex> geosetVertsToSep,
	                          ModelView modelView,
	                          ModelStructureChangeListener changeListener) {
		this.trisToSeparate = new HashSet<>();
		this.modelView = modelView;
		this.model = modelView.getModel();
		this.changeListener = changeListener;
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
		for (Geoset geoset : geosetsToCopy) {
			Geoset geosetCreated = geoset.emptyCopy();
			geosetCreated.setMaterial(getTCMaterial(geoset.getMaterial()));
			oldGeoToNewGeo.put(geoset, geosetCreated);
			geosetsCreated.add(geosetCreated);
		}

		Map<GeosetVertex, GeosetVertex> oldVertToNewVert = new HashMap<>();
		for (GeosetVertex vertex : verticesInTheTriangles) {
			//ToDo VertexCopy code duplication
			GeosetVertex copy = vertex.deepCopy();
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
		}
		selection = new ArrayList<>(modelView.getSelectedVertices());
	}

	private Material getTCMaterial(Material material) {
		Material newMaterial = material.deepCopy();
		if (newMaterial.getLayers().get(0).getFilterMode() == FilterMode.NONE) {
			newMaterial.getLayers().get(0).setFilterMode(FilterMode.BLEND);
		}
		Layer teamColorLayer = new Layer(new Bitmap("", 1));
		teamColorLayer.setUnshaded(true);
		if (material.firstLayer().getTwoSided()) {
			teamColorLayer.setTwoSided(true);
		}
		newMaterial.addLayer(0, teamColorLayer);
		return newMaterial;
	}

	@Override
	public UndoAction undo() {
		for (Geoset geoset : geosetsCreated) {
			model.remove(geoset);
		}
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
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
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
		modelView.setSelectedVertices(newVerticesToSelect);
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Split Geoset For Team Color";
	}

}
