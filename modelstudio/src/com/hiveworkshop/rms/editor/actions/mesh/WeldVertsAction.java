package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.HashableVector;

import java.util.*;
import java.util.stream.Collectors;

public class WeldVertsAction implements UndoAction {
	Set<GeosetVertex> affectedVertices = new HashSet<>();
	Map<GeosetVertex, GeosetVertex> oldToNew = new HashMap<>();
	Map<GeosetVertex, List<Triangle>> orgVertToAffTris = new HashMap<>();
	Map<HashableVector, List<GeosetVertex>> locationToGVs = new HashMap<>();
	Set<Geoset> geosets = new HashSet<>();
	ModelStructureChangeListener changeListener;
	//	boolean onlyUseSelected;
	float weight;

	public WeldVertsAction(Collection<GeosetVertex> selection, float weight, ModelStructureChangeListener changeListener) {
		affectedVertices.addAll(selection);
//		this.onlyUseSelected = onlyUseSelected;
		this.changeListener = changeListener;
		this.weight = weight;

		for (GeosetVertex vertex : affectedVertices) {
			HashableVector location = new HashableVector(vertex);
			List<GeosetVertex> gvAtLocation = locationToGVs.computeIfAbsent(location, gvSet -> new ArrayList<>());
			gvAtLocation.add(vertex);
			geosets.add(vertex.getGeoset());

			List<Triangle> vertTris = orgVertToAffTris.computeIfAbsent(vertex, triSet -> new ArrayList<>());
			vertTris.addAll(vertex.getTriangles());
		}

	}

	@Override
	public UndoAction redo() {
		for (HashableVector location : locationToGVs.keySet()) {
			List<GeosetVertex> verticesAtLocation = locationToGVs.get(location);
			for (Geoset geoset : geosets) {
				List<GeosetVertex> vertices = verticesAtLocation.stream()
						.filter(v -> v.getGeoset() == geoset)
						.collect(Collectors.toList());
				if (!vertices.isEmpty()) {
					GeosetVertex vertexToKeep = vertices.get(0);
					for (GeosetVertex vertex : vertices) {
						oldToNew.put(vertex, vertexToKeep);
						vertex.getGeoset().remove(vertex);
						for (Triangle triangle : vertex.getTriangles()) {
							triangle.replace(vertex, vertexToKeep);
							vertexToKeep.addTriangle(triangle);
						}

					}
					vertexToKeep.getGeoset().add(vertexToKeep);
				}
			}
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		for (GeosetVertex vertex : orgVertToAffTris.keySet()) {
			vertex.clearTriangles();
			for (Triangle triangle : orgVertToAffTris.get(vertex)) {
				triangle.replace(oldToNew.get(vertex), vertex);
				vertex.addTriangle(triangle);
			}
			vertex.getGeoset().add(vertex);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Weld vertices";
	}
}
