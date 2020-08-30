package com.hiveworkshop.rms.ui.application.edit.mesh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class CloneContextHelper {
	private final ModelView model;
	private final ModelStructureChangeListener structureChangeListener;
	private final VertexSelectionHelper vertexSelectionHelperNonPivots;
	private final VertexSelectionHelper vertexSelectionHelperPivots;
	private final SelectionManager<Vec3> pivotPointSelectionManager;
	private final SelectionManager<?> stuffSelectionManager;

	public CloneContextHelper(final ModelView model, final ModelStructureChangeListener structureChangeListener,
							  final VertexSelectionHelper vertexSelectionHelperNonPivots,
							  final VertexSelectionHelper vertexSelectionHelperPivots,
							  final SelectionManager<Vec3> pivotPointSelectionManager,
							  final SelectionManager<?> stuffSelectionManager) {
		this.model = model;
		this.structureChangeListener = structureChangeListener;
		this.vertexSelectionHelperNonPivots = vertexSelectionHelperNonPivots;
		this.vertexSelectionHelperPivots = vertexSelectionHelperPivots;
		this.pivotPointSelectionManager = pivotPointSelectionManager;
		this.stuffSelectionManager = stuffSelectionManager;
	}

	public AdvancedCloneAction cloneSelectedComponents(final ClonedNodeNamePicker clonedNodeNamePicker) {
		final List<Vec3> sourceNonPivots = new ArrayList<>(stuffSelectionManager.getSelectedVertices());
		final List<Vec3> sourcePivots = new ArrayList<>(pivotPointSelectionManager.getSelectedVertices());
		final List<Triangle> selTris = new ArrayList<>();
		final List<IdObject> selBones = new ArrayList<>();
		final List<IdObject> newBones = new ArrayList<>();
		final List<GeosetVertex> newVertices = new ArrayList<>();
		final List<Triangle> newTriangles = new ArrayList<>();
        for (final Vec3 vert : sourceNonPivots) {
            if (vert.getClass() == GeosetVertex.class) {
                final GeosetVertex gv = (GeosetVertex) vert;
                newVertices.add(new GeosetVertex(gv));
            } else {
                newVertices.add(null);
            }
        }
		for (final IdObject b : model.getEditableIdObjects()) {
			if (sourcePivots.contains(b.getPivotPoint()) && !selBones.contains(b)) {
				selBones.add(b);
				newBones.add(b.copy());
			}
		}
		if (newBones.size() > 0) {
			final java.util.Map<IdObject, String> nodeToNamePicked = clonedNodeNamePicker.pickNames(newBones);
			if (nodeToNamePicked == null) {
				throw new RuntimeException(
						"user does not wish to continue so we put in an error to interrupt clone so model is OK");
			}
			for (final IdObject node : nodeToNamePicked.keySet()) {
				node.setName(nodeToNamePicked.get(node));
			}
		}
		for (int k = 0; k < sourceNonPivots.size(); k++) {
			final Vec3 vert = sourceNonPivots.get(k);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				final List<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
				for (final Triangle tri : gv.getGeoset().getTriangles()) {
					if (tri.contains(gv)) {
						boolean good = true;
						for (final Vec3 vTemp : tri.getAll()) {
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
		for (final Triangle tri : selTris) {
			final GeosetVertex a = newVertices.get(sourceNonPivots.indexOf(tri.get(0)));
			final GeosetVertex b = newVertices.get(sourceNonPivots.indexOf(tri.get(1)));
			final GeosetVertex c = newVertices.get(sourceNonPivots.indexOf(tri.get(2)));
			final Triangle newTriangle = new Triangle(a, b, c, a.getGeoset());
			newTriangles.add(newTriangle);
			a.getTriangles().add(newTriangle);
			b.getTriangles().add(newTriangle);
			c.getTriangles().add(newTriangle);
		}
		final Set<Vec3> newSelection = new HashSet<>();
		final Set<Vec3> newSelectionPivots = new HashSet<>();
		for (final Vec3 ver : newVertices) {
			if (ver != null) {
				newSelection.add(ver);
				if (ver.getClass() == GeosetVertex.class) {
					final GeosetVertex gv = (GeosetVertex) ver;
					for (int i = 0; i < gv.getBones().size(); i++) {
						final Bone b = gv.getBones().get(i);
						if (selBones.contains(b)) {
							gv.getBones().set(i, (Bone) newBones.get(selBones.indexOf(b)));
						}
					}
				}
			}
		}
		for (final IdObject b : newBones) {
			newSelectionPivots.add(b.getPivotPoint());
			if (selBones.contains(b.getParent())) {
				b.setParent(newBones.get(selBones.indexOf(b.getParent())));
			}
		}
		final List<GeosetVertex> newVerticesWithoutNulls = new ArrayList<>();
		for (final GeosetVertex vertex : newVertices) {
			if (vertex != null) {
				newVerticesWithoutNulls.add(vertex);
			}
		}
		// TODO cameras
		final AdvancedCloneAction cloneAction = new AdvancedCloneAction(model, sourceNonPivots, sourcePivots,
				structureChangeListener, vertexSelectionHelperNonPivots, vertexSelectionHelperPivots, selBones,
				newVerticesWithoutNulls, newTriangles, newBones, newSelection, newSelectionPivots);
		cloneAction.redo();
		return cloneAction;
	}
}
