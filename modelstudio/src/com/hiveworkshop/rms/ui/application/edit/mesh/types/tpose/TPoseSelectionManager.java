package com.hiveworkshop.rms.ui.application.edit.mesh.types.tpose;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class TPoseSelectionManager extends SelectionManager<IdObject> {
	private final ModelView modelView;
	private final boolean moveLinked;

	private final Bone renderBoneDummy = new Bone();


	public TPoseSelectionManager(ModelView modelView, boolean moveLinked) {
		this.modelView = modelView;
		this.moveLinked = moveLinked;
	}

	@Override
	public Vec3 getCenter() {
		Vec3 centerOfGroupSumHeap = new Vec3(0, 0, 0);
		for (IdObject object : selection) {
			centerOfGroupSumHeap.add(object.getPivotPoint());
		}
		if (selection.size() > 0) {
			centerOfGroupSumHeap.scale(1f / selection.size());
		}
		return centerOfGroupSumHeap;
	}

	@Override
	public Collection<Vec3> getSelectedVertices() {
		// These reference the MODEL EDITOR pivot points, used only as memory references
		// so that downstream will know  to select those pivots, and therefore those IdObject nodes,
		// for static editing (hence we do not apply worldMatrix)
		Set<Vec3> vertices = new HashSet<>();
		Set<IdObject> nodesToMove = new HashSet<>(selection);
		if (moveLinked) {
			for (IdObject object : modelView.getEditableIdObjects()) {
				if (!selection.contains(object)) {
					IdObject parent = object.getParent();
					while (parent != null) {
						if (selection.contains(parent)) {
							nodesToMove.add(object);
						}
						parent = parent.getParent();
					}
				}
			}
		}
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				for (GeosetVertex geosetVertex : triangle.getVerts()) {
					for (Bone bone : geosetVertex.getBoneAttachments()) {
						if (nodesToMove.contains(bone)) {
							vertices.add(geosetVertex);
						}
					}
				}
			}
		}
		for (IdObject object : nodesToMove) {
			vertices.add(object.getPivotPoint());
		}
		return vertices;
	}

	@Override
	public Set<Triangle> getSelectedFaces() {
		return new HashSet<>();
	}


	@Override
	public void renderSelection(ModelElementRenderer renderer, CoordinateSystem coordinateSystem,
	                            ModelView model, ProgramPreferences programPreferences) {
		// TODO !!! apply rendering
		Set<IdObject> drawnSelection = new HashSet<>();
		Set<IdObject> parentedNonSelection = new HashSet<>();
		for (IdObject object : model.getEditableIdObjects()) {
			if (selection.contains(object)) {
				renderer.renderIdObject(object, NodeIconPalette.SELECTED, programPreferences.getAnimatedBoneSelectedColor(), programPreferences.getAnimatedBoneSelectedColor());
				drawnSelection.add(object);
			} else {
				IdObject parent = object.getParent();
				while (parent != null) {
					if (selection.contains(parent)) {
						parentedNonSelection.add(object);
					}
					parent = parent.getParent();
				}
			}
		}
		for (IdObject selectedObject : selection) {
			if (!drawnSelection.contains(selectedObject)) {
				renderBoneDummy.setPivotPoint(selectedObject.getPivotPoint());
				renderer.renderIdObject(renderBoneDummy, NodeIconPalette.SELECTED, programPreferences.getAnimatedBoneSelectedColor(), programPreferences.getAnimatedBoneSelectedColor());
				drawnSelection.add(selectedObject);
			}
		}
		for (IdObject object : model.getEditableIdObjects()) {
			if (parentedNonSelection.contains(object) && !drawnSelection.contains(object)) {
				renderer.renderIdObject(object, NodeIconPalette.SELECTED, programPreferences.getAnimatedBoneSelectedUpstreamColor(), programPreferences.getAnimatedBoneSelectedUpstreamColor());
			}
		}
	}

	@Override
	public double getCircumscribedSphereRadius(Vec3 sphereCenter) {
		double radius = 0;
		for (IdObject item : selection) {
			double distance = sphereCenter.distance(item.getPivotPoint());
			if (distance >= radius) {
				radius = distance;
			}
		}
		return radius;
	}

	@Override
	public double getCircumscribedSphereRadius(Vec2 center, int tvertexLayerId) {
		return 0;
	}

	@Override
	public Vec2 getUVCenter(int tvertexLayerId) {
		return Vec2.ORIGIN;
	}

	@Override
	public Collection<? extends Vec2> getSelectedTVertices(int tvertexLayerId) {
		return Collections.emptySet();
	}

	@Override
	public void renderUVSelection(TVertexModelElementRenderer renderer, ModelView modelView,
	                              ProgramPreferences programPreferences, int tvertexLayerId) {

	}
}
