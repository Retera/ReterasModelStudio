package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.BiMap;

import java.util.*;

public final class TeamColorAddAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final EditableModel model;
	private final Set<Triangle> notSelectedEdgeTriangles;
	private final BiMap<GeosetVertex, GeosetVertex> oldToNew;

	private final BiMap<Geoset, Geoset> oldGeoToNewGeo;

	private final Set<Triangle> trisToSeparate;
	private final Set<GeosetVertex> vertsToSeparate;
	private final Set<Material> newMaterials;

	public TeamColorAddAction(Collection<GeosetVertex> geosetVertsToSep,
	                          EditableModel model,
	                          ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.model = model;
		Set<GeosetVertex> affectedVertices = new HashSet<>(geosetVertsToSep);

		this.trisToSeparate = getFullySelectedTris(affectedVertices);
		this.vertsToSeparate = getVertsToSeparate(trisToSeparate);
		this.oldGeoToNewGeo = getCreatedGeosets(vertsToSeparate);
		this.oldToNew = getVertsToReplace2(vertsToSeparate, trisToSeparate);
		this.notSelectedEdgeTriangles = getAffectedRemainingTris(oldToNew.keySet(), trisToSeparate);

		Map<Material, Material> newMaterialMap = new HashMap<>();
		for (Geoset geoset : oldGeoToNewGeo.values()) {
			String shaderString = geoset.getMaterial().getShaderString();
			if (!"".equals(shaderString)) {
				Material tcMaterial = newMaterialMap.computeIfAbsent(geoset.getMaterial(), k -> getTCMaterial(geoset.getMaterial()));
				geoset.setMaterial(tcMaterial);
			}
		}

		newMaterials = new LinkedHashSet<>();
		for (Material material : newMaterialMap.values()) {
			if (!model.contains(material)) {
				newMaterials.add(material);
			}
		}

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
		int materialID = model.computeMaterialID(newMaterial);
		if (materialID < 0) {
			return newMaterial;
		}
		return model.getMaterial(materialID);
	}

	private Set<Triangle> getFullySelectedTris(Set<GeosetVertex> affectedVertices) {
		Set<Triangle> trisToSeparate = new LinkedHashSet<>();
		for (GeosetVertex vertex : affectedVertices) {
			for (Triangle triangle : vertex.getTriangles()) {
				if (!trisToSeparate.contains(triangle) && affectedVertices.containsAll(Arrays.asList(triangle.getVerts()))) {
					trisToSeparate.add(triangle);
				}
			}
		}
		return trisToSeparate;
	}
	private Set<Triangle> getAffectedRemainingTris(Set<GeosetVertex> vertsToReplace, Set<Triangle> fullySelectedTris) {
		LinkedHashSet<Triangle> notSelectedEdgeTriangles = new LinkedHashSet<>();
		for (GeosetVertex vertex : vertsToReplace) {
			if (vertex.getTriangles().stream().anyMatch(fullySelectedTris::contains)) {
				notSelectedEdgeTriangles.addAll(vertex.getTriangles());
			}
		}
		notSelectedEdgeTriangles.removeAll(fullySelectedTris);
		return notSelectedEdgeTriangles;
	}
	private Set<GeosetVertex> getVertsToSeparate(Set<Triangle> fullySelectedTris) {
		LinkedHashSet<GeosetVertex> vertsToSep = new LinkedHashSet<>();
		for (Triangle triangle : fullySelectedTris) {
			vertsToSep.add(triangle.get(0));
			vertsToSep.add(triangle.get(1));
			vertsToSep.add(triangle.get(2));
		}
		return vertsToSep;
	}

	private BiMap<GeosetVertex, GeosetVertex> getVertsToReplace2(Set<GeosetVertex> vertsToSep, Set<Triangle> fullySelectedTris) {
		BiMap<GeosetVertex, GeosetVertex> vertsToReplace = new BiMap<>();
		for (GeosetVertex vertex : vertsToSep) {
			if (!fullySelectedTris.containsAll(vertex.getTriangles())) {
				vertsToReplace.put(vertex, vertex.deepCopy());
			}
		}
		return vertsToReplace;
	}

	private BiMap<Geoset, Geoset> getCreatedGeosets(Set<GeosetVertex> vertsToSep) {
		BiMap<Geoset, Geoset> geosetBiMap = new BiMap<>();
		for (GeosetVertex vertex : vertsToSep) {
			geosetBiMap.computeIfAbsent(vertex.getGeoset(), k -> vertex.getGeoset().emptyCopy());
		}
		return geosetBiMap;
	}

	private void moveTriToNewGeoset() {
		for (Triangle tri : trisToSeparate) {
			Geoset oldGeoset = tri.getGeoset();
			Geoset newGeoset = oldGeoToNewGeo.get(oldGeoset);

			oldGeoset.remove(tri);
			newGeoset.add(tri);
			tri.setGeoset(newGeoset);
		}
	}
	private void moveTriToOldGeoset() {
		for (Triangle tri : trisToSeparate) {
			Geoset newGeoset = tri.getGeoset();
			Geoset oldGeoset = oldGeoToNewGeo.getByValue(newGeoset);

			newGeoset.remove(tri);
			oldGeoset.add(tri);
			tri.setGeoset(oldGeoset);
		}
	}

	private void moveVertToNewGeoset() {
		for (GeosetVertex vert : vertsToSeparate) {
			Geoset oldGeoset = vert.getGeoset().remove(vert);
			Geoset newGeoset = oldGeoToNewGeo.get(oldGeoset).add(vert);

			oldGeoset.remove(vert);
			newGeoset.add(vert);
			vert.setGeoset(newGeoset);
		}
	}
	private void moveVertToOldGeoset() {
		for (GeosetVertex vert : vertsToSeparate) {
			Geoset newGeoset = vert.getGeoset();
			Geoset oldGeoset = oldGeoToNewGeo.getByValue(newGeoset);

			newGeoset.remove(vert);
			oldGeoset.add(vert);
			vert.setGeoset(oldGeoset);
		}
	}

	private void replaceVertsInTris() {
		for (Triangle triangle : notSelectedEdgeTriangles) {
			replaceTriVert(triangle, triangle.get(0));
			replaceTriVert(triangle, triangle.get(1));
			replaceTriVert(triangle, triangle.get(2));
		}
	}

	private void replaceTriVert(Triangle triangle, GeosetVertex oldVert) {
		GeosetVertex newV = oldToNew.get(oldVert);
		if (newV != null) {
			oldVert.removeTriangle(triangle);
			triangle.replace(oldVert, newV);
			newV.addTriangle(triangle);
		}
	}

	private void putBackVertsInTris() {
		for (Triangle triangle : notSelectedEdgeTriangles) {
			putBackTriVert(triangle, triangle.get(0));
			putBackTriVert(triangle, triangle.get(1));
			putBackTriVert(triangle, triangle.get(2));
		}
	}

	private void putBackTriVert(Triangle triangle, GeosetVertex newVert) {
		GeosetVertex oldVert = oldToNew.getByValue(newVert);
		if (oldVert != null) {
			newVert.removeTriangle(triangle);
			triangle.replace(newVert, oldVert);
			oldVert.addTriangle(triangle);
		}
	}

	@Override
	public TeamColorAddAction undo() {
		for (GeosetVertex vertex : oldToNew.values()) {
			vertex.getGeoset().remove(vertex);
		}

		putBackVertsInTris();
		moveVertToOldGeoset();
		moveTriToOldGeoset();

		for (Geoset geoset : oldGeoToNewGeo.values()) {
			model.remove(geoset);
		}

		for (Material material : newMaterials) {
			model.remove(material);
		}

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}


	@Override
	public TeamColorAddAction redo() {
		for (Geoset geoset : oldGeoToNewGeo.values()) {
			model.add(geoset);
		}

		for (Material material : newMaterials) {
			model.add(material);
		}

		moveTriToNewGeoset();
		moveVertToNewGeoset();
		replaceVertsInTris();

		for (GeosetVertex vertex : oldToNew.values()) {
			vertex.getGeoset().add(vertex);
		}

		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Split Geoset For Team Color";
	}

}
