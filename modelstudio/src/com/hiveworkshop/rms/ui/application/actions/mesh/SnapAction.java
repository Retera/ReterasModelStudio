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
public class SnapAction implements UndoAction {
	List<GeosetVertex> selectedVertices = new ArrayList<>();
	Map<GeosetVertex, Vec3> gvToOldPos = new HashMap<>();
	private List<Vec3> oldSelLocs;
	private List<Vec3> selection;
	private Vec3 snapPoint;

	public SnapAction(Collection<? extends Vec3> selection, List<Vec3> oldSelLocs, Vec3 snapPoint) {
		this.selection = new ArrayList<>(selection);
		this.oldSelLocs = oldSelLocs;
		this.snapPoint = new Vec3(snapPoint);
	}

	public SnapAction(Collection<? extends Vec3> selection) {
		selection.forEach(vert -> selectedVertices.add((GeosetVertex) vert));

		this.snapPoint = Vec3.centerOfGroup(selectedVertices);
		for (GeosetVertex vertex : selectedVertices) {
			gvToOldPos.put(vertex, new Vec3(vertex));
		}
	}

	@Override
	public void undo() {
//		for (int i = 0; i < selection.size(); i++) {
//			selection.get(i).set(oldSelLocs.get(i));
//		}
		for (GeosetVertex vertex : selectedVertices) {
			vertex.set(gvToOldPos.get(vertex));
		}
	}

	@Override
	public void redo() {
//        for (Vec3 vec3 : selection) {
//            vec3.set(snapPoint);
//        }
		for (GeosetVertex vertex : selectedVertices) {
			vertex.set(snapPoint);
		}
	}

	@Override
	public String actionName() {
		return "snap vertices";
	}
}
