package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.AddSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.RemoveSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.awt.*;
import java.util.List;
import java.util.*;

public class SelectionManager extends AbstractSelectionManager {

	private static final Color FACE_SELECTED_COLOR = new Color(1f, 0.45f, 0.45f, 0.3f);
	private static final Color FACE_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
	private static final Color FACE_NOT_SELECTED_COLOR = new Color(0.45f, 0.45f, 1f, 0.3f);
	private static final Color CLUSTER_SELECTED_COLOR = new Color(1f, 0.45f, 0.75f, 0.3f);
	private static final Color CLUSTER_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
	private static final Color GROUP_SELECTED_COLOR = new Color(1f, 0.75f, 0.45f, 0.3f);
	private static final Color GROUP_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
	private final Set<SelectionListener> listeners = new HashSet<>();
	//	protected final Set<T> selection = new HashSet<>();
	private boolean moveLinked;

	private VertexClusterDefinitions vertexClusterDefinitions;

	public SelectionManager(ModelView modelView, SelectionItemTypes selectionMode) {
		super(modelView, selectionMode);
		if (selectionMode == SelectionItemTypes.GROUP) {
			addSelectionListener(newSelection -> selectBundle(modelView));
		} else if (selectionMode == SelectionItemTypes.CLUSTER) {
			vertexClusterDefinitions = new VertexClusterDefinitions(modelView.getModel(), selectionMode);
			addSelectionListener(newSelection -> selectBundle(vertexClusterDefinitions, modelView));
		}
	}

	public SelectionManager(ModelView modelView, boolean moveLinked, SelectionItemTypes selectionMode) {
		super(modelView, selectionMode);
		this.moveLinked = moveLinked;
	}

	private void selectBundle(ModelView modelView) {
		Set<VertexGroupBundle> bundleSet = new HashSet<>();
		java.util.List<GeosetVertex> verticesSelected = new ArrayList<>();
		for (GeosetVertex vertex : modelView.getSelectedVertices()) {
			bundleSet.add(new VertexGroupBundle(vertex.getGeoset(), vertex.getVertexGroup()));
		}

		for (VertexGroupBundle bundle : bundleSet) {
			for (GeosetVertex geosetVertex : bundle.getGeoset().getVertices()) {
				if (geosetVertex.getVertexGroup() == bundle.getVertexGroupId()) {
					verticesSelected.add(geosetVertex);
				}
			}
		}
		modelView.setSelectedVertices(verticesSelected);
	}

	private void selectBundle(VertexClusterDefinitions vertexClusterDefinitions, ModelView modelView) {
		Set<VertexGroupBundle> bundleSet = new HashSet<>();
		java.util.List<GeosetVertex> verticesSelected = new ArrayList<>();
		for (GeosetVertex vertex : modelView.getSelectedVertices()) {
			bundleSet.add(new VertexGroupBundle(vertex.getGeoset(), vertexClusterDefinitions.getClusterId(vertex)));
		}

		for (VertexGroupBundle bundle : bundleSet) {
			for (GeosetVertex geosetVertex : bundle.getGeoset().getVertices()) {
				if (vertexClusterDefinitions.getClusterId(geosetVertex) == bundle.getVertexGroupId()) {
					verticesSelected.add(geosetVertex);
				}
			}
		}
		modelView.setSelectedVertices(verticesSelected);
	}

	protected void fireChangeListeners() {
//		for (final SelectionListener listener : listeners) {
//			listener.onSelectionChanged(this);
//		}
	}

	public Collection<GeosetVertex> getSelectedVertices() {
		return modelView.getSelectedVertices();
	}

	public Vec3 getCenter() {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER
				|| selectionMode == SelectionItemTypes.TPOSE) {
			return modelView.getSelectionCenter();
		}
		if (selectionMode == SelectionItemTypes.ANIMATE) {
			Vec3 centerOfGroupSumHeap = new Vec3(0, 0, 0);
			for (IdObject object : modelView.getSelectedIdObjects()) {
				Vec4 pivotHeap = new Vec4(object.getPivotPoint(), 1);
				pivotHeap.transform(modelView.getEditorRenderModel().getRenderNode(object).getWorldMatrix());
				centerOfGroupSumHeap.add(pivotHeap.getVec3());
			}
			if (modelView.getSelectedIdObjects().size() > 0) {
				centerOfGroupSumHeap.scale(1f / modelView.getSelectedIdObjects().size());
			}
			return centerOfGroupSumHeap;
		}
		return new Vec3();
	}

	public SelectoinUgg genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			Set<GeosetVertex> selectedItems = addVertsFromArea(min, max, coordinateSystem);
			return new SelectoinUgg(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> newSel = addTrisFromArea(min, max, coordinateSystem);
			return new SelectoinUgg(newSel);
		}

		if (selectionMode == SelectionItemTypes.CLUSTER) {
			List<VertexGroupBundle> newSelection = new ArrayList<>();
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (HitTestStuff.triHitTest(triangle, min, coordinateSystem)
							|| HitTestStuff.triHitTest(triangle, max, coordinateSystem)
							|| HitTestStuff.triHitTest(triangle, min, max, coordinateSystem)) {
						for (GeosetVertex vertex : triangle.getAll()) {
							newSelection.add(new VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(vertex)));
						}
					}
				}
			}

			Set<GeosetVertex> geosetVerticesSelected = addVertsFromArea(min, max, coordinateSystem);
			for (GeosetVertex vertex : geosetVerticesSelected) {
				newSelection.add(new VertexGroupBundle(vertex.getGeoset(), vertexClusterDefinitions.getClusterId(vertex)));
			}
			Set<GeosetVertex> selectedItems = new HashSet<>();
			for (VertexGroupBundle bundle : newSelection) {
				selectedItems.addAll(bundle.getGeoset().getVertices());
			}
			return new SelectoinUgg(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.GROUP) {
			List<VertexGroupBundle> newSelection = new ArrayList<>();

			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (HitTestStuff.triHitTest(triangle, min, coordinateSystem)
							|| HitTestStuff.triHitTest(triangle, max, coordinateSystem)
							|| HitTestStuff.triHitTest(triangle, min, max, coordinateSystem)) {
						for (GeosetVertex vertex : triangle.getAll()) {
							newSelection.add(new VertexGroupBundle(geoset, vertex.getVertexGroup()));
						}
					}
				}
			}
			Set<GeosetVertex> geosetVerticesSelected = addVertsFromArea(min, max, coordinateSystem);
			for (GeosetVertex vertex : geosetVerticesSelected) {
				newSelection.add(new VertexGroupBundle(vertex.getGeoset(), vertex.getVertexGroup()));
			}

			Set<GeosetVertex> selectedItems = new HashSet<>();
			for (VertexGroupBundle bundle : newSelection) {
				selectedItems.addAll(bundle.getGeoset().getVertices());
			}
			return new SelectoinUgg(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER) {
			Set<IdObject> selectedItems = getIdObjectsFromArea(min, max, coordinateSystem);
			Set<Camera> selectedCams = getCamerasFromArea(min, max, coordinateSystem);
			return new SelectoinUgg(selectedItems, selectedCams);
		}

		if (selectionMode == SelectionItemTypes.TPOSE) {
			List<IdObject> selectedItems = new ArrayList<>();

			for (IdObject object : modelView.getEditableIdObjects()) {
				double vertexSize = object.getClickRadius(coordinateSystem) * coordinateSystem.getZoom();
				if (HitTestStuff.hitTest(min, max, object.getPivotPoint(), coordinateSystem, vertexSize)) {
					selectedItems.add(object);
				}
				if (object instanceof CollisionShape) {
					for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
						if (HitTestStuff.hitTest(min, max, vertex, coordinateSystem, IdObject.DEFAULT_CLICK_RADIUS)) {
							selectedItems.add(object);
						}
					}
				}
			}
			return new SelectoinUgg(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.ANIMATE) {
			List<IdObject> selectedItems = new ArrayList<>();

			for (IdObject object : modelView.getEditableIdObjects()) {
				double vertexSize = object.getClickRadius(coordinateSystem) * coordinateSystem.getZoom() * 2;
				HitTestStuff.hitTest(selectedItems, min, max, coordinateSystem, vertexSize, object, modelView.getEditorRenderModel());
			}
			return new SelectoinUgg(selectedItems);
		}
		return new SelectoinUgg(Collections.emptySet());
	}

	private Set<Camera> getCamerasFromArea(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		Set<Camera> selectedCams = new HashSet<>();
		for (Camera camera : modelView.getEditableCameras()) {
			int vertexSize = ProgramGlobals.getPrefs().getVertexSize();
			if (HitTestStuff.hitTest(min, max, camera.getPosition(), coordinateSystem, vertexSize)) {
				selectedCams.add(camera);
			}
			if (HitTestStuff.hitTest(min, max, camera.getTargetPosition(), coordinateSystem, vertexSize)) {
				selectedCams.add(camera);
			}
		}
		return selectedCams;
	}

	private Set<IdObject> getIdObjectsFromArea(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		Set<IdObject> selectedItems = new HashSet<>();
		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertexSize1 = object.getClickRadius(coordinateSystem) * coordinateSystem.getZoom() * 2;
			if (HitTestStuff.hitTest(min, max, object.getPivotPoint(), coordinateSystem, vertexSize1)) {
				System.out.println("selected " + object.getName());
				selectedItems.add(object);
			}

//				if (object instanceof CollisionShape) {
//					for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
//						int vertexSize = IdObject.DEFAULT_CLICK_RADIUS;
//						if (HitTestStuff.hitTest(min, max, vertex, coordinateSystem, vertexSize)) {
//							selectedItems.add(vertex);
//						}
//					}
//				}
		}
		return selectedItems;
	}

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (HitTestStuff.triHitTest(triangle, min, coordinateSystem)
						|| HitTestStuff.triHitTest(triangle, max, coordinateSystem)
						|| HitTestStuff.triHitTest(triangle, min, max, coordinateSystem)) {
					newSelection.addAll(Arrays.asList(triangle.getAll()));
				}
			}
		}
		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (HitTestStuff.hitTest(min, max, geosetVertex, coordinateSystem, ProgramGlobals.getPrefs().getVertexSize()))
					newSelection.add(geosetVertex);
			}
		}
		return newSelection;
	}

	public final UndoAction setSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		SelectoinUgg newSelection = genericSelect(min, max, coordinateSystem);
		UndoAction select = new SetSelectionUggAction(newSelection, modelView, "select").redo();
		fireChangeListeners();
		return select;
	}

	public final UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		SelectoinUgg newSelection = genericSelect(min, max, coordinateSystem);
		UndoAction removeSelectionUggAction = new RemoveSelectionUggAction(newSelection, modelView).redo();
		fireChangeListeners();
		return removeSelectionUggAction;
	}

	public final UndoAction addSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		SelectoinUgg newSelection = genericSelect(min, max, coordinateSystem);
		UndoAction addSelectionUggAction = new AddSelectionUggAction(newSelection, modelView).redo();
		fireChangeListeners();
		return addSelectionUggAction;
	}

	public void setSelection(SelectoinUgg selectionItem) {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER) {
			modelView.setSelectedVertices(selectionItem.getSelectedVertices());
			modelView.setSelectedIdObjects(selectionItem.getSelectedIdObjects());
			modelView.setSelectedCameras(selectionItem.getSelectedCameras());
		}
		if (selectionMode == SelectionItemTypes.ANIMATE
				|| selectionMode == SelectionItemTypes.TPOSE) {
			modelView.setSelectedIdObjects(selectionItem.getSelectedIdObjects());
		}
	}


	public void addSelection(SelectoinUgg selectionItem) {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER) {
			modelView.addSelectedVertices(selectionItem.getSelectedVertices());
			modelView.addSelectedIdObjects(selectionItem.getSelectedIdObjects());
			modelView.addSelectedCameras(selectionItem.getSelectedCameras());
		}
		if (selectionMode == SelectionItemTypes.ANIMATE
				|| selectionMode == SelectionItemTypes.TPOSE) {
			modelView.addSelectedIdObjects(selectionItem.getSelectedIdObjects());
		}
		fireChangeListeners();
	}

	public void removeSelection(SelectoinUgg selectionItem) {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER) {
			modelView.removeSelectedVertices(selectionItem.getSelectedVertices());
			modelView.removeSelectedIdObjects(selectionItem.getSelectedIdObjects());
			modelView.removeSelectedCameras(selectionItem.getSelectedCameras());
		}
		if (selectionMode == SelectionItemTypes.ANIMATE
				|| selectionMode == SelectionItemTypes.TPOSE) {
			modelView.removeSelectedIdObjects(selectionItem.getSelectedIdObjects());
		}
		fireChangeListeners();
	}

	public void addSelectionListener(final SelectionListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(final SelectionListener listener) {
		listeners.remove(listener);
	}

	public boolean isEmpty() {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER) {
			return modelView.getSelectedVertices().isEmpty()
					&& modelView.getSelectedIdObjects().isEmpty()
					&& modelView.getSelectedCameras().isEmpty();
		}
		if (selectionMode == SelectionItemTypes.ANIMATE
				|| selectionMode == SelectionItemTypes.TPOSE) {
			return modelView.getSelectedIdObjects().isEmpty();
		}
		return false;
	}

	public double getCircumscribedSphereRadius(Vec3 sphereCenter) {

		if (selectionMode == SelectionItemTypes.VERTEX || selectionMode == SelectionItemTypes.FACE || selectionMode == SelectionItemTypes.CLUSTER || selectionMode == SelectionItemTypes.GROUP) {
			double radius = 0;
			for (Vec3 item : modelView.getSelectedVertices()) {
				double distance = sphereCenter.distance(item);
				if (distance >= radius) {
					radius = distance;
				}
			}
			for (IdObject item : modelView.getSelectedIdObjects()) {
				double distance = sphereCenter.distance(item.getPivotPoint());
				if (distance >= radius) {
					radius = distance;
				}
			}
			for (Camera item : modelView.getSelectedCameras()) {
				double distance = sphereCenter.distance(item.getPosition());
				if (distance >= radius) {
					radius = distance;
				}
			}
//			for (Vec3 item : selection) {
//				double distance = sphereCenter.distance(item);
//				if (distance >= radius) {
//					radius = distance;
//				}
//			}

			return radius;
		}

		if (selectionMode == SelectionItemTypes.TPOSE) {
			double radius = 0;
			for (IdObject item : modelView.getEditableIdObjects()) {
				double distance = sphereCenter.distance(item.getPivotPoint());
				if (distance >= radius) {
					radius = distance;
				}
			}
			return radius;
		}

		if (selectionMode == SelectionItemTypes.ANIMATE) {
			double radius = 0;
			for (IdObject item : modelView.getSelectedIdObjects()) {
				Vec4 pivotHeap = new Vec4(item.getPivotPoint(), 1);
				pivotHeap.transform(modelView.getEditorRenderModel().getRenderNode(item).getWorldMatrix());
				double distance = sphereCenter.distance(pivotHeap);
				if (distance >= radius) {
					radius = distance;
				}
			}
			return radius;
		}
		return 0;
	}

	public double getCircumscribedSphereRadius(Vec2 center, int tvertexLayerId) {
		if (selectionMode == SelectionItemTypes.VERTEX || selectionMode == SelectionItemTypes.FACE || selectionMode == SelectionItemTypes.CLUSTER || selectionMode == SelectionItemTypes.GROUP) {
			double radius = 0;
			for (GeosetVertex item : modelView.getSelectedVertices()) {
				if (tvertexLayerId < item.getTverts().size()) {
					double distance = center.distance(item.getTVertex(tvertexLayerId));
					if (distance >= radius) {
						radius = distance;
					}
				}
			}
			return radius;
		}
		return 0;
	}

	public Vec2 getUVCenter(int tvertexLayerId) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			return Vec2.centerOfGroup(getSelectedTVertices(tvertexLayerId));
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<Vec2> selectedVertices = new HashSet<>();
			for (Triangle triangle : modelView.getSelectedTriangles()) {
				for (GeosetVertex geosetVertex : triangle.getVerts()) {
					if (tvertexLayerId < geosetVertex.getTverts().size()) {
						selectedVertices.add(geosetVertex.getTVertex(tvertexLayerId));
					}
				}
			}
			return Vec2.centerOfGroup(selectedVertices);
		}
		return Vec2.ORIGIN;
	}

	public Collection<? extends Vec2> getSelectedTVertices(int tvertexLayerId) {
		if (selectionMode == SelectionItemTypes.VERTEX || selectionMode == SelectionItemTypes.FACE || selectionMode == SelectionItemTypes.CLUSTER || selectionMode == SelectionItemTypes.GROUP) {
			Set<Vec2> selectedTVertices = new HashSet<>();
			for (GeosetVertex vertex : modelView.getSelectedVertices()) {
				if (tvertexLayerId < vertex.getTverts().size()) {
					selectedTVertices.add(vertex.getTVertex(tvertexLayerId));
				}
			}
			return selectedTVertices;
		}
		return Collections.emptySet();
	}



	public boolean selectableUnderCursor(Vec2 point, CoordinateSystem axes) {
		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertexSize1 = object.getClickRadius(axes) * axes.getZoom() * 2;
			if (HitTestStuff.hitTest(object.getPivotPoint(), CoordSysUtils.geomV2(axes, point), axes, vertexSize1)) {
				return true;
			}
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					int vertexSize = IdObject.DEFAULT_CLICK_RADIUS;
					if (HitTestStuff.hitTest(vertex, CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
						return true;
					}
				}
			}
		}
		for (Camera camera : modelView.getEditableCameras()) {
			int vertexSize = ProgramGlobals.getPrefs().getVertexSize();
			if (HitTestStuff.hitTest(camera.getPosition(), CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
				return true;
			}
			if (HitTestStuff.hitTest(camera.getTargetPosition(), CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
				return true;
			}
		}

		if (selectionMode == SelectionItemTypes.VERTEX){
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (HitTestStuff.hitTest(geosetVertex, CoordSysUtils.geomV2(axes, point), axes, ProgramGlobals.getPrefs().getVertexSize())) {
						return true;
					}
				}
			}
		}


		if(selectionMode == SelectionItemTypes.CLUSTER){
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (HitTestStuff.triHitTest(triangle, CoordSysUtils.geomV2(axes, point), axes)) {
						return true;
					}
				}
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (HitTestStuff.hitTest(geosetVertex, CoordSysUtils.geomV2(axes, point), axes, ProgramGlobals.getPrefs().getVertexSize())) {
						return true;
					}
				}
			}
		}

		if(selectionMode == SelectionItemTypes.GROUP){
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (HitTestStuff.triHitTest(triangle, CoordSysUtils.geomV2(axes, point), axes)) {
						return true;
					}
				}
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (HitTestStuff.hitTest(geosetVertex, CoordSysUtils.geomV2(axes, point), axes, ProgramGlobals.getPrefs().getVertexSize())) {
						return true;
					}
				}
			}
		}

		if(selectionMode == SelectionItemTypes.FACE){
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (HitTestStuff.triHitTest(triangle, CoordSysUtils.geomV2(axes, point), axes)) {
						return true;
					}
				}
			}
		}
		if(selectionMode == SelectionItemTypes.ANIMATE){
			for (IdObject object : modelView.getEditableIdObjects()) {
				Mat4 worldMatrix = modelView.getEditorRenderModel().getRenderNode(object).getWorldMatrix();
				double vertexSize = object.getClickRadius(axes) * axes.getZoom() * 2;
				if (HitTestStuff.hitTest(object.getPivotPoint(), CoordSysUtils.geomV2(axes, point), axes, vertexSize, worldMatrix)) {
					return true;
				}
			}
			return false;
		}

		return false;
	}


	private void ugg() {
		if (selectionMode == SelectionItemTypes.VERTEX) {
		}

		if (selectionMode == SelectionItemTypes.FACE) {
		}

		if (selectionMode == SelectionItemTypes.CLUSTER) {
		}

		if (selectionMode == SelectionItemTypes.GROUP) {
		}

		boolean piv = true;
		if (piv) {
		}

		if (selectionMode == SelectionItemTypes.TPOSE) {
		}

		if (selectionMode == SelectionItemTypes.ANIMATE) {
		}

		if (selectionMode == SelectionItemTypes.VERTEX || selectionMode == SelectionItemTypes.FACE || selectionMode == SelectionItemTypes.GROUP || selectionMode == SelectionItemTypes.CLUSTER) {
		}
	}
}
