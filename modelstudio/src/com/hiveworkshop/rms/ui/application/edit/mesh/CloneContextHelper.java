package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.actions.tools.AdvancedCloneAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CloneContextHelper {
	private final ModelView modelView;
	private final ModelStructureChangeListener structureChangeListener;

	public CloneContextHelper(ModelView modelView,
	                          ModelStructureChangeListener structureChangeListener) {
		this.modelView = modelView;
		this.structureChangeListener = structureChangeListener;
	}

	public AdvancedCloneAction cloneSelectedComponents(ClonedNodeNamePicker clonedNodeNamePicker) {
		List<GeosetVertex> sourceNonPivots = new ArrayList<>(modelView.getSelectedVertices());
		List<IdObject> sourcePivots = new ArrayList<>(modelView.getSelectedIdObjects());
//		List<Vec3> sourceNonPivots = new ArrayList<>(stuffSelectionManager.getSelectedVertices());
//		List<Vec3> sourcePivots = new ArrayList<>(pivotPointSelectionManager.getSelectedVertices());
		List<Triangle> selTris = new ArrayList<>();

		List<GeosetVertex> newVertices = new ArrayList<>();
		for (GeosetVertex vert : sourceNonPivots) {
			newVertices.add(vert.deepCopy());
		}

		List<IdObject> selBones = new ArrayList<>();
		List<IdObject> newBones = new ArrayList<>();
		for (IdObject b : modelView.getEditableIdObjects()) {
			if (sourcePivots.contains(b) && !selBones.contains(b)) {
				selBones.add(b);
				newBones.add(b.copy());
			}
		}
		if (newBones.size() > 0) {
			java.util.Map<IdObject, String> nodeToNamePicked = clonedNodeNamePicker.pickNames(newBones);
			if (nodeToNamePicked == null) {
				throw new RuntimeException(
						"user does not wish to continue so we put in an error to interrupt clone so model is OK");
			}
			for (IdObject node : nodeToNamePicked.keySet()) {
				node.setName(nodeToNamePicked.get(node));
			}
		}
		for (int k = 0; k < sourceNonPivots.size(); k++) {
			GeosetVertex vert = sourceNonPivots.get(k);
			List<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
			for (Triangle tri : vert.getGeoset().getTriangles()) {
				if (tri.containsLoc(vert)) {
					boolean good = true;
					for (Vec3 vTemp : tri.getVerts()) {
						if (!sourceNonPivots.contains(vTemp)) {
							good = false;
							break;
						}
					}
					if (good) {
						gvTriangles.add(tri);
						if (!selTris.contains(tri)) {
							selTris.add(tri);
						}
					}
				}
			}
		}

		List<Triangle> newTriangles = new ArrayList<>();
		for (Triangle tri : selTris) {
			GeosetVertex a = newVertices.get(sourceNonPivots.indexOf(tri.get(0)));
			GeosetVertex b = newVertices.get(sourceNonPivots.indexOf(tri.get(1)));
			GeosetVertex c = newVertices.get(sourceNonPivots.indexOf(tri.get(2)));
			Triangle newTriangle = new Triangle(a, b, c, a.getGeoset()).addToVerts();
			newTriangles.add(newTriangle);
//			a.addTriangle(newTriangle);
//			b.addTriangle(newTriangle);
//			c.addTriangle(newTriangle);
		}
		Set<GeosetVertex> newSelection = new HashSet<>();

		for (GeosetVertex vert : newVertices) {
			if (vert != null) {
				newSelection.add(vert);
				for (int i = 0; i < vert.getBones().size(); i++) {
					Bone b = vert.getBones().get(i);
					if (selBones.contains(b)) {
						vert.setBone(i, (Bone) newBones.get(selBones.indexOf(b)));
					}
				}
			}
		}

		Set<IdObject> newSelectionPivots = new HashSet<>();
		for (IdObject b : newBones) {
			newSelectionPivots.add(b);
			if (selBones.contains(b.getParent())) {
				b.setParent(newBones.get(selBones.indexOf(b.getParent())));
			}
		}

		List<GeosetVertex> newVerticesWithoutNulls = new ArrayList<>();
		for (GeosetVertex vertex : newVertices) {
			if (vertex != null) {
				newVerticesWithoutNulls.add(vertex);
			}
		}
		// TODO cameras
		AdvancedCloneAction cloneAction = new AdvancedCloneAction(modelView, sourceNonPivots, sourcePivots,
				structureChangeListener,
				selBones,
				newVerticesWithoutNulls, newTriangles, newBones, newSelection, newSelectionPivots);

		return cloneAction.redo();
	}
}
