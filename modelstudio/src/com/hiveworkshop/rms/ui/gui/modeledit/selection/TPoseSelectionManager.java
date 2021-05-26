package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public final class TPoseSelectionManager extends SelectionManager<IdObject> {
//	private final ModelView modelView;
	private final boolean moveLinked;

	private final Bone renderBoneDummy = new Bone();


	public TPoseSelectionManager(ModelView modelView, boolean moveLinked, SelectionItemTypes selectionMode) {
		super(modelView, selectionMode);
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
	public List<IdObject> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<IdObject> selectedItems = new ArrayList<>();

		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertexSize = object.getClickRadius(coordinateSystem) * coordinateSystem.getZoom();
			if (AbstractModelEditor.hitTest(min, max, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					if (AbstractModelEditor.hitTest(min, max, vertex, coordinateSystem, IdObject.DEFAULT_CLICK_RADIUS)) {
						selectedItems.add(object);
					}
				}
			}
		}
		return selectedItems;
	}

//	@Override
//	public Set<Triangle> getSelectedFaces() {
//		return new HashSet<>();
//	}


//	@Override
//	public void renderSelection(ModelElementRenderer renderer, CoordinateSystem coordinateSystem,
//	                            ModelView model) {
////		Set<IdObject> drawnSelection = new HashSet<>();
////		Set<IdObject> parentedNonSelection = new HashSet<>();
////		for (IdObject object : model.getEditableIdObjects()) {
////			if (selection.contains(object)) {
////				renderer.renderIdObject(object);
////				drawnSelection.add(object);
////			} else {
////				IdObject parent = object.getParent();
////				while (parent != null) {
////					if (selection.contains(parent)) {
////						parentedNonSelection.add(object);
////					}
////					parent = parent.getParent();
////				}
////			}
////		}
////		for (IdObject selectedObject : selection) {
////			if (!drawnSelection.contains(selectedObject)) {
////				renderBoneDummy.setPivotPoint(selectedObject.getPivotPoint());
////				renderer.renderIdObject(renderBoneDummy);
////				drawnSelection.add(selectedObject);
////			}
////		}
////		for (IdObject object : model.getEditableIdObjects()) {
////			if (parentedNonSelection.contains(object) && !drawnSelection.contains(object)) {
////				renderer.renderIdObject(object);
////			}
////		}
//	}

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
	public void renderUVSelection(TVertexModelElementRenderer renderer, ModelView modelView, int tvertexLayerId) {

	}
}
