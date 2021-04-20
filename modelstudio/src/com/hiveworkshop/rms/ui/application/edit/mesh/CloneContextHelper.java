package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.AdvancedCloneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CloneContextHelper {
	private final ModelView model;
	private final ModelStructureChangeListener structureChangeListener;
	private final VertexSelectionHelper vertexSelectionHelperNonPivots;
	private final VertexSelectionHelper vertexSelectionHelperPivots;
	private final SelectionManager<Vec3> pivotPointSelectionManager;
	private final SelectionManager<?> stuffSelectionManager;

	public CloneContextHelper(ModelView model, ModelStructureChangeListener structureChangeListener,
	                          VertexSelectionHelper vertexSelectionHelperNonPivots,
	                          VertexSelectionHelper vertexSelectionHelperPivots,
	                          SelectionManager<Vec3> pivotPointSelectionManager,
	                          SelectionManager<?> stuffSelectionManager) {
		this.model = model;
		this.structureChangeListener = structureChangeListener;
		this.vertexSelectionHelperNonPivots = vertexSelectionHelperNonPivots;
		this.vertexSelectionHelperPivots = vertexSelectionHelperPivots;
		this.pivotPointSelectionManager = pivotPointSelectionManager;
		this.stuffSelectionManager = stuffSelectionManager;
	}

	public AdvancedCloneAction cloneSelectedComponents(ClonedNodeNamePicker clonedNodeNamePicker) {
		List<Vec3> sourceNonPivots = new ArrayList<>(stuffSelectionManager.getSelectedVertices());
		List<Vec3> sourcePivots = new ArrayList<>(pivotPointSelectionManager.getSelectedVertices());
		List<Triangle> selTris = new ArrayList<>();

		List<GeosetVertex> newVertices = new ArrayList<>();
		for (Vec3 vert : sourceNonPivots) {
			if (vert.getClass() == GeosetVertex.class) {
				GeosetVertex gv = (GeosetVertex) vert;
				newVertices.add(new GeosetVertex(gv));
			} else {
				newVertices.add(null);
			}
		}

		List<IdObject> selBones = new ArrayList<>();
		List<IdObject> newBones = new ArrayList<>();
		for (IdObject b : model.getEditableIdObjects()) {
			if (sourcePivots.contains(b.getPivotPoint()) && !selBones.contains(b)) {
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
			Vec3 vert = sourceNonPivots.get(k);
			if (vert.getClass() == GeosetVertex.class) {
				GeosetVertex gv = (GeosetVertex) vert;
				List<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
				for (Triangle tri : gv.getGeoset().getTriangles()) {
					if (tri.contains(gv)) {
						boolean good = true;
						for (Vec3 vTemp : tri.getAll()) {
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
		}

		List<Triangle> newTriangles = new ArrayList<>();
		for (Triangle tri : selTris) {
			GeosetVertex a = newVertices.get(sourceNonPivots.indexOf(tri.get(0)));
			GeosetVertex b = newVertices.get(sourceNonPivots.indexOf(tri.get(1)));
			GeosetVertex c = newVertices.get(sourceNonPivots.indexOf(tri.get(2)));
			Triangle newTriangle = new Triangle(a, b, c, a.getGeoset());
			newTriangles.add(newTriangle);
//			a.addTriangle(newTriangle);
//			b.addTriangle(newTriangle);
//			c.addTriangle(newTriangle);
		}
		Set<Vec3> newSelection = new HashSet<>();

		for (Vec3 ver : newVertices) {
			if (ver != null) {
				newSelection.add(ver);
				if (ver.getClass() == GeosetVertex.class) {
					GeosetVertex gv = (GeosetVertex) ver;
					for (int i = 0; i < gv.getBones().size(); i++) {
						Bone b = gv.getBones().get(i);
						if (selBones.contains(b)) {
							gv.getBones().set(i, (Bone) newBones.get(selBones.indexOf(b)));
						}
					}
				}
			}
		}

		Set<Vec3> newSelectionPivots = new HashSet<>();
		for (IdObject b : newBones) {
			newSelectionPivots.add(b.getPivotPoint());
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
		AdvancedCloneAction cloneAction = new AdvancedCloneAction(model, sourceNonPivots, sourcePivots, structureChangeListener, vertexSelectionHelperNonPivots, vertexSelectionHelperPivots, selBones, newVerticesWithoutNulls, newTriangles, newBones, newSelection, newSelectionPivots);

		cloneAction.redo();
		return cloneAction;
	}
}
