package com.hiveworkshop.rms.ui.application.edit.mesh.types.tpose;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class TPoseSelectionManager extends SelectionManager<IdObject> {
//	private final ModelView modelView;
	private final boolean moveLinked;

	private final Bone renderBoneDummy = new Bone();


	public TPoseSelectionManager(ModelView modelView, boolean moveLinked) {
		super(modelView);
//		this.modelView = modelView;
		this.moveLinked = moveLinked;
	}

	@Override
	public Set<IdObject> getSelection() {
		return modelView.getSelectedIdObjects();
	}

	@Override
	public void setSelection(final Collection<? extends IdObject> selectionItem) {
		modelView.setSelectedIdObjects((Collection<IdObject>) selectionItem);
//		fireChangeListeners();
	}

	@Override
	public void addSelection(final Collection<? extends IdObject> selectionItem) {
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
	public boolean isEmpty() {
		return modelView.getSelectedIdObjects().isEmpty();
	}



	@Override
	public Vec3 getCenter() {
		Vec3 centerOfGroupSumHeap = new Vec3(0, 0, 0);
		for (IdObject object : modelView.getSelectedIdObjects()) {
			centerOfGroupSumHeap.add(object.getPivotPoint());
		}
		if (modelView.getSelectedIdObjects().size() > 0) {
			centerOfGroupSumHeap.scale(1f / modelView.getSelectedIdObjects().size());
		}
		return centerOfGroupSumHeap;
	}

	@Override
	public Collection<Vec3> getSelectedVertices() {
		// These reference the MODEL EDITOR pivot points, used only as memory references
		// so that downstream will know  to select those pivots, and therefore those IdObject nodes,
		// for static editing (hence we do not apply worldMatrix)
		Set<Vec3> vertices = new HashSet<>();
		Set<IdObject> nodesToMove = new HashSet<>(modelView.getEditableIdObjects());
		if (moveLinked) {
			for (IdObject object : modelView.getEditableIdObjects()) {
				if (!modelView.isSelected(object)) {
					IdObject parent = object.getParent();
					while (parent != null) {
						if (modelView.isSelected(parent)) {
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
	                            ModelView model) {
//		Set<IdObject> drawnSelection = new HashSet<>();
//		Set<IdObject> parentedNonSelection = new HashSet<>();
//		for (IdObject object : model.getEditableIdObjects()) {
//			if (selection.contains(object)) {
//				renderer.renderIdObject(object);
//				drawnSelection.add(object);
//			} else {
//				IdObject parent = object.getParent();
//				while (parent != null) {
//					if (selection.contains(parent)) {
//						parentedNonSelection.add(object);
//					}
//					parent = parent.getParent();
//				}
//			}
//		}
//		for (IdObject selectedObject : selection) {
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
		for (IdObject item : modelView.getEditableIdObjects()) {
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
	                              int tvertexLayerId) {

	}
}
