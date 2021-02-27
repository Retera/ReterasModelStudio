package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.*;

public final class NodeAnimationSelectionManager extends AbstractSelectionManager<IdObject> {
	private final RenderModel renderModel;

	public NodeAnimationSelectionManager(final RenderModel renderModel) {
		this.renderModel = renderModel;
	}

	@Override
	public Set<Triangle> getSelectedFaces() {
		return new HashSet<>();
	}

	@Override
	public Vec3 getCenter() {
		Vec3 centerOfGroupSumHeap = new Vec3(0, 0, 0);
		for (final IdObject object : selection) {
			final Vec3 pivot = object.getPivotPoint();
			Vec4 pivotHeap = new Vec4(pivot, 1);
			pivotHeap.transform(renderModel.getRenderNode(object).getWorldMatrix());
			centerOfGroupSumHeap.add(pivotHeap.getVec3());
		}
		if (selection.size() > 0) {
			centerOfGroupSumHeap.scale(1f / selection.size());
		}
		return centerOfGroupSumHeap;
	}

	@Override
	public double getCircumscribedSphereRadius(final Vec3 sphereCenter) {
		double radius = 0;
		for (final IdObject item : selection) {
			final Vec3 pivot = item.getPivotPoint();
			Vec4 pivotHeap = new Vec4(pivot, 1);
			pivotHeap.transform(renderModel.getRenderNode(item).getWorldMatrix());
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
		// These reference the MODEL EDITOR pivot points, used only as memory references
		// so that downstream will know to select those pivots, and therefore those
		// IdObject nodes, for static editing (hence we do not apply worldMatrix)
		final List<Vec3> vertices = new ArrayList<>();
		for (final IdObject obj : selection) {
			vertices.add(obj.getPivotPoint());
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
