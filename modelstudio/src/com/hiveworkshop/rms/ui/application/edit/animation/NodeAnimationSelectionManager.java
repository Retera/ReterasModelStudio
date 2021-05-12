package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.*;

public final class NodeAnimationSelectionManager extends SelectionManager<IdObject> {
	private final RenderModel renderModel;

	private final Bone renderBoneDummy = new Bone();

	public NodeAnimationSelectionManager(final RenderModel renderModel, ModelView modelView) {
		super(modelView);
		this.renderModel = renderModel;
	}

	@Override
	public Vec3 getCenter() {
		Vec3 centerOfGroupSumHeap = new Vec3(0, 0, 0);
		for (IdObject object : modelView.getSelectedIdObjects()) {
			Vec4 pivotHeap = new Vec4(object.getPivotPoint(), 1);
			pivotHeap.transform(renderModel.getRenderNode(object).getWorldMatrix());
			centerOfGroupSumHeap.add(pivotHeap.getVec3());
		}
		if (modelView.getSelectedIdObjects().size() > 0) {
			centerOfGroupSumHeap.scale(1f / modelView.getSelectedIdObjects().size());
		}
		return centerOfGroupSumHeap;
	}

	@Override
	public Set<IdObject> getSelection() {
		return modelView.getSelectedIdObjects();
	}

	@Override
	public void setSelection(final Collection<? extends IdObject> selectionItem) {
//		selection.clear();
//		selection.addAll(selectionItem);
		modelView.setSelectedIdObjects((Collection<IdObject>) selectionItem);
//		fireChangeListeners();
	}

	@Override
	public void addSelection(final Collection<? extends IdObject> selectionItem) {
//		selection.addAll(selectionItem);
		modelView.addSelectedIdObjects((Collection<IdObject>) selectionItem);
//		fireChangeListeners();
	}

	@Override
	public void removeSelection(final Collection<? extends IdObject> selectionItem) {
		for (final IdObject item : selectionItem) {
//			selection.remove(item);
			modelView.removeSelectedIdObjects((Collection<IdObject>) selectionItem);
		}
//		fireChangeListeners();
	}

	@Override
	public Collection<Vec3> getSelectedVertices() {
		// These reference the MODEL EDITOR pivot points, used only as memory references
		// so that downstream will know to select those pivots, and therefore those
		// IdObject nodes, for static editing (hence we do not apply worldMatrix)
		List<Vec3> vertices = new ArrayList<>();
		for (IdObject obj : modelView.getSelectedIdObjects()) {
			vertices.add(obj.getPivotPoint());
		}
		return vertices;
	}

	@Override
	public Set<Triangle> getSelectedFaces() {
		return new HashSet<>();
	}

	@Override
	public void renderSelection(ModelElementRenderer renderer, CoordinateSystem coordinateSystem, ModelView model) {
//		// TODO !!! apply rendering
//		Set<IdObject> drawnSelection = new HashSet<>();
//		Set<IdObject> parentedNonSelection = new HashSet<>();
//		for (IdObject object : model.getEditableIdObjects()) {
//			if (modelView.getSelectedIdObjects().contains(object)) {
//				renderer.renderIdObject(object);
//				drawnSelection.add(object);
//			} else {
//				IdObject parent = object.getParent();
//				while (parent != null) {
//					if (modelView.getSelectedIdObjects().contains(parent)) {
//						parentedNonSelection.add(object);
//					}
//					parent = parent.getParent();
//				}
//			}
//		}
//		for (IdObject selectedObject : modelView.getSelectedIdObjects()) {
//			if (!drawnSelection.contains(selectedObject)) {
//				renderBoneDummy.setPivotPoint(selectedObject.getPivotPoint());
//				renderer.renderIdObject(renderBoneDummy);
//				drawnSelection.add(selectedObject);
//			}
//		}
//		for (IdObject object : model.getEditableIdObjects()) {
//			if (parentedNonSelection.contains(object) && !drawnSelection.contains(object)) {
//				renderer.renderIdObject(object);
//			}
//		}
	}

	@Override
	public double getCircumscribedSphereRadius(Vec3 sphereCenter) {
		double radius = 0;
		for (IdObject item : modelView.getSelectedIdObjects()) {
			Vec4 pivotHeap = new Vec4(item.getPivotPoint(), 1);
			pivotHeap.transform(renderModel.getRenderNode(item).getWorldMatrix());
			double distance = sphereCenter.distance(pivotHeap);
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
	                              int tvertexLayerId) {

	}
}
