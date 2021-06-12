package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.InexactHashVector;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class SnapCloseVertsAction implements UndoAction {
	Map<InexactHashVector, HashSet<GeosetVertex>> locationToGVs = new HashMap<>();
	Map<GeosetVertex, Vec3> oldLocs = new HashMap<>();
	float distance;
	ModelStructureChangeListener changeListener;

	public SnapCloseVertsAction(Collection<GeosetVertex> selection, float distance, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.distance = distance;
		float precision = .5f / distance; // ~ 1/(distance * sqrt(3))

		for (GeosetVertex vertex : selection) {
//			InexactHashVector location = new InexactHashVector(vertex, precision);
//			InexactHashVector location = new InexactHashVector(vertex, 1f);
			InexactHashVector location = new InexactHashVector(vertex, 1);
			locationToGVs.computeIfAbsent(location, gvSet -> new HashSet<>()).add(vertex);
			oldLocs.put(vertex, new Vec3(vertex));
		}

	}

	@Override
	public UndoAction redo() {

		for (InexactHashVector location : locationToGVs.keySet()) {
			Set<GeosetVertex> verticesAtLocation = locationToGVs.get(location);
			System.out.println("snapping " + verticesAtLocation.size() + "verts");
//			Vec3 center = Vec3.centerOfGroup(verticesAtLocation);
			float[] locationValues = location.getValues();
			for (GeosetVertex vertex : verticesAtLocation) {
//				vertex.set(center);
				System.out.print("vertex bf: " + vertex);
				vertex.set(locationValues);
				System.out.println(", af: " + vertex);
			}
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		for (GeosetVertex vertex : oldLocs.keySet()) {
			vertex.set(oldLocs.get(vertex));
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Snap Close Vertices";
	}
}
