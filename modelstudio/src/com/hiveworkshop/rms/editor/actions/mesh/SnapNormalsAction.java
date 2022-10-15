package com.hiveworkshop.rms.editor.actions.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.InexactHashVector;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class SnapNormalsAction implements UndoAction {
	private final List<GeosetVertex> selectedVertices = new ArrayList<>();
	private final Map<GeosetVertex, Vec3> gvToOldNorm = new HashMap<>();
	private final Map<GeosetVertex, Vec3> gvToNewNorm;

	public SnapNormalsAction(Collection<GeosetVertex> vertices, Vec3 snapPoint) {
		this.selectedVertices.addAll(vertices);
		Vec3 snapPoint1 = new Vec3(snapPoint);

		for (GeosetVertex vertex : selectedVertices) {
			if (vertex.getNormal() != null) {
				gvToOldNorm.put(vertex, new Vec3(vertex.getNormal()));
			} // else no normal to snap!!!
		}
		gvToNewNorm = makeNewNormals(selectedVertices);
	}

	private Map<GeosetVertex, Vec3> makeNewNormals(Collection<GeosetVertex> selectedVertices){
		Map<GeosetVertex, Vec3> gvToNewNorm = new HashMap<>();
		Map<InexactHashVector, List<GeosetVertex>> locationToGVs = new HashMap<>();
		for (GeosetVertex geosetVertex : selectedVertices) {
			InexactHashVector location = new InexactHashVector(geosetVertex);
			List<GeosetVertex> gvAtLocation = locationToGVs.computeIfAbsent(location, gvList -> new ArrayList<>());
			gvAtLocation.add(geosetVertex);
		}
		for(List<GeosetVertex> vertsAt : locationToGVs.values()){
			Vec3 newNormal = new Vec3(0, 0, 0);
			for (GeosetVertex geosetVertex : vertsAt) {
				newNormal.add(geosetVertex.getNormal());
				gvToNewNorm.put(geosetVertex, newNormal);
			}
			if (newNormal.length() < 0.000001){
				newNormal.add(Vec3.Z_AXIS);
			}
			newNormal.normalize();
		}
		return gvToNewNorm;
	}

	@Override
	public UndoAction undo() {
		for (GeosetVertex vertex : selectedVertices) {
			vertex.setNormalValue(gvToOldNorm.get(vertex));
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (GeosetVertex vertex : selectedVertices) {
			vertex.setNormalValue(gvToNewNorm.get(vertex));
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Snap Normals";
	}
}
