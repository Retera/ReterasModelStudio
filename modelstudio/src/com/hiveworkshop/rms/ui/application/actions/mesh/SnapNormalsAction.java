package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

/**
 * Undoable snap action.
 *
 * Eric Theller 6/11/2012
 */
public class SnapNormalsAction implements UndoAction {
	//	List<Vec3> oldSelLocs = new ArrayList<>();
//	List<Vec3> selectedNormals = new ArrayList<>();
	Vec3 snapPoint;
	List<GeosetVertex> selectedVertices = new ArrayList<>();
	Map<GeosetVertex, Vec3> gvToOldNorm = new HashMap<>();

	public SnapNormalsAction(Collection<? extends Vec3> vertices, Vec3 snapPoint) {
		vertices.forEach(vert -> selectedVertices.add((GeosetVertex) vert));
		this.snapPoint = new Vec3(snapPoint);

		for (GeosetVertex vertex : selectedVertices) {
			if (vertex.getNormal() != null) {
				gvToOldNorm.put(vertex, new Vec3(vertex.getNormal()));
//					oldSelLocs.add(new Vec3(gv.getNormal()));
//					selectedNormals.add(gv.getNormal());
			} // else no normal to snap!!!
		}
	}

	@Override
	public UndoAction undo() {
//		for (int i = 0; i < selection.size(); i++) {
//			selection.get(i).set(oldSelLocs.get(i));
//		}
		for (GeosetVertex vertex : selectedVertices) {
			vertex.setNormalValue(gvToOldNorm.get(vertex));
		}
		return this;
	}

	@Override
	public UndoAction redo() {
//		for (Vec3 vec3 : selection) {
//			vec3.set(snapPoint);
//		}
		for (GeosetVertex vertex : selectedVertices) {
			vertex.setNormalValue(snapPoint);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "snap normals";
	}
}
