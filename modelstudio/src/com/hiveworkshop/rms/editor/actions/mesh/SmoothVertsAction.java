package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class SmoothVertsAction implements UndoAction {
	Set<GeosetVertex> affectedVertices = new HashSet<>();
	Map<GeosetVertex, Vec3> oldPosMap = new HashMap<>();
	Map<GeosetVertex, Vec3> smoothPosMap = new HashMap<>();
	boolean onlyUseSelected;
	float weight;

	public SmoothVertsAction(Collection<GeosetVertex> selection, boolean onlyUseSelected, float weight) {
		affectedVertices.addAll(selection);
		this.onlyUseSelected = onlyUseSelected;
		this.weight = weight;

		for (GeosetVertex vertex : affectedVertices) {
			oldPosMap.put(vertex, new Vec3(vertex));
			Set<Vec3> smoothVerts = new HashSet<>();
			for (Triangle triangle : vertex.getTriangles()) {
				for (GeosetVertex v : triangle.getVerts()) {
					if (v != vertex && (!onlyUseSelected || affectedVertices.contains(v))) {
						smoothVerts.add(v);
					}
				}
			}
			smoothPosMap.put(vertex, Vec3.centerOfGroup(smoothVerts));
		}
	}


	@Override
	public UndoAction redo() {
		for (GeosetVertex vertex : affectedVertices) {
			vertex.scale(1 - weight).addScaled(smoothPosMap.get(vertex), weight);
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		for (GeosetVertex vertex : affectedVertices) {
			vertex.set(oldPosMap.get(vertex));
		}
		return this;
	}

	@Override
	public String actionName() {
		return "smooth";
	}
}
