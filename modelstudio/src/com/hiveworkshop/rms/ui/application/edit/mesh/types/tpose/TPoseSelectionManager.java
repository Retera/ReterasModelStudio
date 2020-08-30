package com.hiveworkshop.rms.ui.application.edit.mesh.types.tpose;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class TPoseSelectionManager extends AbstractSelectionManager<IdObject> {
	private final ModelView modelView;
	private final boolean moveLinked;

	public TPoseSelectionManager(final ModelView modelView, final boolean moveLinked) {
		this.modelView = modelView;
		this.moveLinked = moveLinked;
	}

	@Override
	public Set<Triangle> getSelectedFaces() {
		return new HashSet<>();
	}

	private final Vec4 pivotHeap = new Vec4();
	private final Vec3 centerOfGroupSumHeap = new Vec3(0, 0, 0);

	@Override
	public Vec3 getCenter() {
		centerOfGroupSumHeap.x = 0;
		centerOfGroupSumHeap.y = 0;
		centerOfGroupSumHeap.z = 0;
		for (final IdObject object : selection) {
			final Vec3 pivot = object.getPivotPoint();
			pivotHeap.x = pivot.x;
			pivotHeap.y = pivot.y;
			pivotHeap.z = pivot.z;
			centerOfGroupSumHeap.x += pivotHeap.x;
			centerOfGroupSumHeap.y += pivotHeap.y;
			centerOfGroupSumHeap.z += pivotHeap.z;
		}
		if (selection.size() > 0) {
			centerOfGroupSumHeap.x /= selection.size();
			centerOfGroupSumHeap.y /= selection.size();
			centerOfGroupSumHeap.z /= selection.size();
		}
		return centerOfGroupSumHeap;
	}

	@Override
	public double getCircumscribedSphereRadius(final Vec3 sphereCenter) {
		double radius = 0;
		for (final IdObject item : selection) {
			final Vec3 pivot = item.getPivotPoint();
			pivotHeap.x = pivot.x;
			pivotHeap.y = pivot.y;
			pivotHeap.z = pivot.z;
			pivotHeap.w = 1;
			final double distance = sphereCenter.distance(pivotHeap);
			if (distance >= radius) {
				radius = distance;
			}
		}
		return radius;
	}

	private final Bone renderBoneDummy = new Bone();

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem,
								final ModelView model, final ProgramPreferences programPreferences) {
		// TODO !!! apply rendering
		final Set<IdObject> drawnSelection = new HashSet<>();
		final Set<IdObject> parentedNonSelection = new HashSet<>();
		for (final IdObject object : model.getEditableIdObjects()) {
			if (selection.contains(object)) {
				renderer.renderIdObject(object, NodeIconPalette.SELECTED,
						programPreferences.getAnimatedBoneSelectedColor(),
						programPreferences.getAnimatedBoneSelectedColor());
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
		for (final IdObject selectedObject : selection) {
			if (!drawnSelection.contains(selectedObject)) {
				renderBoneDummy.setPivotPoint(selectedObject.getPivotPoint());
				renderer.renderIdObject(renderBoneDummy, NodeIconPalette.SELECTED,
						programPreferences.getAnimatedBoneSelectedColor(),
						programPreferences.getAnimatedBoneSelectedColor());
				drawnSelection.add(selectedObject);
			}
		}
		for (final IdObject object : model.getEditableIdObjects()) {
			if (parentedNonSelection.contains(object) && !drawnSelection.contains(object)) {
				renderer.renderIdObject(object, NodeIconPalette.SELECTED,
						programPreferences.getAnimatedBoneSelectedUpstreamColor(),
						programPreferences.getAnimatedBoneSelectedUpstreamColor());
			}
		}
	}

	@Override
	public Collection<Vec3> getSelectedVertices() {
		// These reference the MODEL EDITOR pivot points,
		// used only as memory references so that downstream will know
		// to select those pivots, and therefore those IdObject nodes,
		// for static editing (hence we do not apply worldMatrix)
		final Set<Vec3> vertices = new HashSet<>();
		final Set<IdObject> nodesToMove = new HashSet<>(selection);
		if (moveLinked) {
			for (final IdObject object : modelView.getEditableIdObjects()) {
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
		for (final Geoset geoset : modelView.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				for (final GeosetVertex geosetVertex : triangle.getVerts()) {
					for (final Bone bone : geosetVertex.getBoneAttachments()) {
						if (nodesToMove.contains(bone)) {
							vertices.add(geosetVertex);
						}
					}
				}
			}
		}
		for (final IdObject object : nodesToMove) {
			vertices.add(object.getPivotPoint());
		}
		return vertices;
	}

	@Override
	public Vec2 getUVCenter(final int tvertexLayerId) {
		return Vec2.ORIGIN;
	}

	@Override
	public Collection<? extends Vec2> getSelectedTVertices(final int tvertexLayerId) {
		return Collections.emptySet();
	}

	@Override
	public double getCircumscribedSphereRadius(final Vec2 center, final int tvertexLayerId) {
		return 0;
	}

	@Override
	public void renderUVSelection(final TVertexModelElementRenderer renderer, final ModelView modelView,
                                  final ProgramPreferences programPreferences, final int tvertexLayerId) {

	}
}
