package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.AddSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.RemoveSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.renderparts.RenderGeoset;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
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

	public SelectionManager(RenderModel editorRenderModel, ModelView modelView, SelectionItemTypes selectionMode) {
		super(editorRenderModel, modelView, selectionMode);
//		if (selectionMode == SelectionItemTypes.GROUP) {
//			addSelectionListener(newSelection -> modelView.setSelectedVertices(getGroupBundle(modelView)));
//		} else if (selectionMode == SelectionItemTypes.CLUSTER) {
////			vertexClusterDefinitions = new VertexClusterDefinitions(modelView.getModel(), selectionMode);
//			addSelectionListener(newSelection -> modelView.setSelectedVertices(getClusterBundle(vertexClusterDefinitions, modelView)));
//		}
//		vertexClusterDefinitions = new VertexClusterDefinitions(modelView.getModel());
		vertexClusterDefinitions = new VertexClusterDefinitions();
	}

	public SelectionManager(RenderModel editorRenderModel, ModelView modelView, boolean moveLinked, SelectionItemTypes selectionMode) {
		super(editorRenderModel, modelView, selectionMode);
		this.moveLinked = moveLinked;
//		vertexClusterDefinitions = new VertexClusterDefinitions(modelView.getModel());
		vertexClusterDefinitions = new VertexClusterDefinitions();
	}

	private Set<GeosetVertex> getGroupBundle(Collection<GeosetVertex> selectedVertices) {
		Set<VertexGroupBundle> bundleSet = new HashSet<>();
		for (GeosetVertex vertex : selectedVertices) {
//			bundleSet.add(new VertexGroupBundle(vertex.getGeoset(), vertex.getVertexGroup()));
			bundleSet.add(new VertexGroupBundle(vertex.getGeoset(), vertex.getMatrixIndex()));
		}

		Set<GeosetVertex> verticesSelected = new HashSet<>();
		for (VertexGroupBundle bundle : bundleSet) {
			for (GeosetVertex geosetVertex : bundle.getGeoset().getVertices()) {
//				if (geosetVertex.getVertexGroup() == bundle.getVertexGroupId()) {
				if (geosetVertex.getMatrixIndex() == bundle.getVertexGroupId()) {
					verticesSelected.add(geosetVertex);
				}
			}
		}
		return verticesSelected;
	}

	private Set<GeosetVertex> getClusterBundle(VertexClusterDefinitions vertexClusterDefinitions, Collection<GeosetVertex> selectedVertices) {
		Set<GeosetVertex> verticesSelected = new HashSet<>();

		for (GeosetVertex vertex : selectedVertices) {
			if (!verticesSelected.contains(vertex)) {
				verticesSelected.addAll(vertexClusterDefinitions.getVertexBundle(vertex));
			}
		}

		return verticesSelected;
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
				pivotHeap.transform(editorRenderModel.getRenderNode(object).getWorldMatrix());
				centerOfGroupSumHeap.add(pivotHeap.getVec3());
			}
			if (modelView.getSelectedIdObjects().size() > 0) {
				centerOfGroupSumHeap.scale(1f / modelView.getSelectedIdObjects().size());
			}
			return centerOfGroupSumHeap;
		}
		return new Vec3();
	}

	public SelectionBundle genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			Set<GeosetVertex> selectedVerts = addVertsFromArea(min, max, coordinateSystem);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, coordinateSystem);
			Set<Camera> selectedCams = getCamerasFromArea(min, max, coordinateSystem);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> selectedVerts = addTrisFromArea(min, max, coordinateSystem);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, coordinateSystem);
			Set<Camera> selectedCams = getCamerasFromArea(min, max, coordinateSystem);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

		if (selectionMode == SelectionItemTypes.CLUSTER) {
			Set<GeosetVertex> selectedVerts = getClusterBundle(vertexClusterDefinitions, addTrisFromArea(min, max, coordinateSystem));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, coordinateSystem);
			Set<Camera> selectedCams = getCamerasFromArea(min, max, coordinateSystem);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

		if (selectionMode == SelectionItemTypes.GROUP) {
			Set<GeosetVertex> selectedVerts = getGroupBundle(addTrisFromArea(min, max, coordinateSystem));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, coordinateSystem);
			Set<Camera> selectedCams = getCamerasFromArea(min, max, coordinateSystem);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

//		if (selectionMode == SelectionItemTypes.VERTEX
//				|| selectionMode == SelectionItemTypes.FACE
//				|| selectionMode == SelectionItemTypes.GROUP
//				|| selectionMode == SelectionItemTypes.CLUSTER) {
//			Set<IdObject> selectedItems = getIdObjectsFromArea(min, max, coordinateSystem);
//			Set<Camera> selectedCams = getCamerasFromArea(min, max, coordinateSystem);
//			return new SelectionBundle(selectedItems, selectedCams);
//		}

		if (selectionMode == SelectionItemTypes.TPOSE) {
			List<IdObject> selectedItems = new ArrayList<>();

			for (IdObject object : modelView.getEditableIdObjects()) {
				double vertexSize = object.getClickRadius();
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
			return new SelectionBundle(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.ANIMATE) {
			List<IdObject> selectedItems = new ArrayList<>();

			for (IdObject object : modelView.getEditableIdObjects()) {
				double vertexSize = object.getClickRadius() * 2;
				RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
				if (HitTestStuff.hitTest(min, max, renderNode.getPivot(), coordinateSystem, vertexSize)) {
					selectedItems.add(object);
				}
			}
			return new SelectionBundle(selectedItems);
		}
		return new SelectionBundle(Collections.emptySet());
	}


	public SelectionBundle genericSelect3D(Vec2 min, Vec2 max, CameraHandler cameraHandler) {
		Mat4 viewPortMat = cameraHandler.getViewPortAntiRotMat();
		double zoom = cameraHandler.getZoom();
		if (selectionMode == SelectionItemTypes.VERTEX) {
			Set<GeosetVertex> selectedVerts = addVertsFromArea(min, max, cameraHandler);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, cameraHandler);
			Set<Camera> selectedCams = getCamerasFromArea(min, max, cameraHandler);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> selectedVerts = addTrisFromArea(min, max, viewPortMat, zoom);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, cameraHandler);
			Set<Camera> selectedCams = getCamerasFromArea(min, max, cameraHandler);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

		if (selectionMode == SelectionItemTypes.CLUSTER) {
			Set<GeosetVertex> selectedVerts = getClusterBundle(vertexClusterDefinitions, addTrisFromArea(min, max, viewPortMat, zoom));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, cameraHandler);
			Set<Camera> selectedCams = getCamerasFromArea(min, max, cameraHandler);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

		if (selectionMode == SelectionItemTypes.GROUP) {
			Set<GeosetVertex> selectedVerts = getGroupBundle(addTrisFromArea(min, max, viewPortMat, zoom));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, cameraHandler);
			Set<Camera> selectedCams = getCamerasFromArea(min, max, cameraHandler);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

//		if (selectionMode == SelectionItemTypes.VERTEX
//				|| selectionMode == SelectionItemTypes.FACE
//				|| selectionMode == SelectionItemTypes.GROUP
//				|| selectionMode == SelectionItemTypes.CLUSTER) {
//			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewPortMat, zoom);
//			Set<Camera> selectedCams = getCamerasFromArea(min, max, viewPortMat, zoom);
//			return new SelectionBundle(selectedObjs, selectedCams);
//		}

//		if (selectionMode == SelectionItemTypes.TPOSE) {
//			List<IdObject> selectedItems = new ArrayList<>();
//
//			for (IdObject object : modelView.getEditableIdObjects()) {
//				double vertexSize = object.getClickRadius();
//				if (HitTestStuff.hitTest(min, max, object.getPivotPoint(), coordinateSystem, vertexSize)) {
//					selectedItems.add(object);
//				}
//				if (object instanceof CollisionShape) {
//					for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
//						if (HitTestStuff.hitTest(min, max, vertex, coordinateSystem, IdObject.DEFAULT_CLICK_RADIUS)) {
//							selectedItems.add(object);
//						}
//					}
//				}
//			}
//			return new SelectionBundle(selectedItems);
//		}

		if (selectionMode == SelectionItemTypes.ANIMATE) {
			List<IdObject> selectedItems = new ArrayList<>();

			for (IdObject object : modelView.getEditableIdObjects()) {
				double vertexSize = object.getClickRadius() * 2;
				RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
				if (modelView.isEditable(object) && HitTestStuff.hitTest(min, max, renderNode.getPivot(), viewPortMat, ProgramGlobals.getPrefs().getVertexSize(), zoom)) {
					selectedItems.add(object);
				}
			}
			Set<GeosetVertex> selectedVerts = addVertsFromArea(min, max, cameraHandler);
			return new SelectionBundle(selectedItems, selectedVerts);
		}
		return new SelectionBundle(Collections.emptySet());
	}

	private Set<Camera> getCamerasFromArea(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		Set<Camera> selectedCams = new HashSet<>();
		for (Camera camera : modelView.getEditableCameras()) {
			int vertexSize = ProgramGlobals.getPrefs().getVertexSize();
			if (modelView.isEditable(camera) && HitTestStuff.hitTest(min, max, camera.getPosition(), coordinateSystem, vertexSize)) {
				selectedCams.add(camera);
			}
			if (modelView.isEditable(camera) && HitTestStuff.hitTest(min, max, camera.getTargetPosition(), coordinateSystem, vertexSize)) {
				selectedCams.add(camera);
			}
		}
		return selectedCams;
	}

	private Set<IdObject> getIdObjectsFromArea(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		Set<IdObject> selectedItems = new HashSet<>();
		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertexSize1 = object.getClickRadius() * 2;
			if (modelView.isEditable(object) && HitTestStuff.hitTest(min, max, object.getPivotPoint(), coordinateSystem, vertexSize1)) {
//				System.out.println("selected " + object.getName());
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
				if (HitTestStuff.triHitTest(triangle, min, max, coordinateSystem)) {
					newSelection.addAll(Arrays.asList(triangle.getAll()));
				}
			}
		}
		newSelection.removeIf(vertex -> !modelView.isEditable(vertex));
		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (modelView.isEditable(geosetVertex) && HitTestStuff.hitTest(min, max, geosetVertex, coordinateSystem, ProgramGlobals.getPrefs().getVertexSize()))
					newSelection.add(geosetVertex);
			}
		}
		return newSelection;
	}
// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓  3D  ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	private Set<Camera> getCamerasFromArea(Vec2 min, Vec2 max, CameraHandler cameraHandler) {
		Set<Camera> selectedCams = new HashSet<>();
		double vertSize = cameraHandler.geomDist(ProgramGlobals.getPrefs().getVertexSize() / 2.0);
		Mat4 viewPortMat = cameraHandler.getViewPortAntiRotMat();
		double zoom = cameraHandler.getZoom();
		for (Camera camera : modelView.getEditableCameras()) {
			int vertexSize = ProgramGlobals.getPrefs().getVertexSize();
			if (modelView.isEditable(camera) && HitTestStuff.hitTest(min, max, camera.getPosition(), viewPortMat, vertSize, zoom)) {
				selectedCams.add(camera);
			}
			if (modelView.isEditable(camera) && HitTestStuff.hitTest(min, max, camera.getTargetPosition(), viewPortMat, vertSize, zoom)) {
				selectedCams.add(camera);
			}
		}
		return selectedCams;
	}

	private Set<IdObject> getIdObjectsFromArea(Vec2 min, Vec2 max, CameraHandler cameraHandler) {

		Set<IdObject> selectedItems = new HashSet<>();
		Mat4 viewPortMat = cameraHandler.getViewPortAntiRotMat();
		double zoom = cameraHandler.getZoom();
		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertSize = cameraHandler.geomDist(object.getClickRadius() / 2.0);
//			double vertexSize1 = object.getClickRadius() * 2;
			if (modelView.isEditable(object) && HitTestStuff.hitTest(min, max, object.getPivotPoint(), viewPortMat, vertSize, zoom)) {
//				System.out.println("selected " + object.getName());
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

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, Mat4 viewPortMat, double zoom) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (HitTestStuff.triHitTest(triangle, min, max, viewPortMat)
				) {
					newSelection.addAll(Arrays.asList(triangle.getAll()));
				}
			}
		}
		newSelection.removeIf(vertex -> !modelView.isEditable(vertex));
		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, CameraHandler cameraHandler) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		Mat4 viewPortMat = cameraHandler.getViewPortAntiRotMat();
		double zoom = cameraHandler.getZoom();

		double vertSize = cameraHandler.geomDist(ProgramGlobals.getPrefs().getVertexSize() / 2.0);

		for (Geoset geoset : modelView.getEditableGeosets()) {
			RenderGeoset renderGeoset = editorRenderModel.getRenderGeoset(geoset);
			for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
//			for (GeosetVertex geosetVertex : geoset.getVertices()) {

				if (modelView.isEditable(renderVert.getVertex()) && HitTestStuff.hitTest(min, max, renderVert.getRenderPos(), viewPortMat, vertSize, zoom))
					newSelection.add(renderVert.getVertex());
			}
		}
		return newSelection;
	}

	public final UndoAction setSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		SelectionBundle newSelection = genericSelect(min, max, coordinateSystem);
		if (modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
			return null;
		}
		UndoAction select = new SetSelectionUggAction(newSelection, modelView, "select").redo();
		fireChangeListeners();
		return select;
	}

	public final UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		SelectionBundle newSelection = genericSelect(min, max, coordinateSystem);
		if (modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
			return null;
		}
		UndoAction removeSelectionUggAction = new RemoveSelectionUggAction(newSelection, modelView).redo();
		fireChangeListeners();
		return removeSelectionUggAction;
	}

	public final UndoAction addSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		SelectionBundle newSelection = genericSelect(min, max, coordinateSystem);
		if (modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
			return null;
		}
		UndoAction addSelectionUggAction = new AddSelectionUggAction(newSelection, modelView).redo();
		fireChangeListeners();
		return addSelectionUggAction;
	}

	public final UndoAction setSelectedRegion(Vec2 min, Vec2 max, CameraHandler cameraHandler) {
		SelectionBundle newSelection = genericSelect3D(min, max, cameraHandler);
//		if (modelView.getSelectedVertices().size() == newSelection.getSelectedVertices().size() && modelView.getSelectedVertices().containsAll(newSelection.getSelectedVertices())
//				&& modelView.getSelectedIdObjects().size() == newSelection.getSelectedIdObjects().size() && modelView.getSelectedIdObjects().containsAll(newSelection.getSelectedIdObjects())
//				&& modelView.getSelectedCameras().size() == newSelection.getSelectedCameras().size() && modelView.getSelectedCameras().containsAll(newSelection.getSelectedCameras())){
//			return null;
//		}
		if (modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
			return null;
		}
		UndoAction select = new SetSelectionUggAction(newSelection, modelView, "select").redo();
		fireChangeListeners();
		return select;
	}

	public final UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CameraHandler cameraHandler) {
		SelectionBundle newSelection = genericSelect3D(min, max, cameraHandler);
		if (modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
			return null;
		}
		UndoAction removeSelectionUggAction = new RemoveSelectionUggAction(newSelection, modelView).redo();
		fireChangeListeners();
		return removeSelectionUggAction;
	}

	public final UndoAction addSelectedRegion(Vec2 min, Vec2 max, CameraHandler cameraHandler) {
		SelectionBundle newSelection = genericSelect3D(min, max, cameraHandler);
		if (modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
			return null;
		}
		UndoAction addSelectionUggAction = new AddSelectionUggAction(newSelection, modelView).redo();
		fireChangeListeners();
		return addSelectionUggAction;
	}

	public void setSelection(SelectionBundle selectionItem) {
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


	public void addSelection(SelectionBundle selectionItem) {
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

	public void removeSelection(SelectionBundle selectionItem) {
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
				pivotHeap.transform(editorRenderModel.getRenderNode(item).getWorldMatrix());
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
			double vertexSize1 = object.getClickRadius() * 2;
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
				RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
				double vertexSize = object.getClickRadius() * 2;
				if (HitTestStuff.hitTest(renderNode.getPivot(), CoordSysUtils.geomV2(axes, point), axes, vertexSize)) {
					return true;
				}
			}
			return false;
		}

		return false;
	}

	public boolean selectableUnderCursor(Vec2 point, CameraHandler cameraHandler) {
		for (IdObject object : modelView.getEditableIdObjects()) {
			RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
			double vertexSize = object.getClickRadius() * 2;
			if (HitTestStuff.hitTest(renderNode.getPivot(), point, cameraHandler, vertexSize)) {
				return true;
			}
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					int vertexSize1 = IdObject.DEFAULT_CLICK_RADIUS;
					if (HitTestStuff.hitTest(vertex, point, cameraHandler, vertexSize1)) {
						return true;
					}
				}
			}
		}
		for (Camera camera : modelView.getEditableCameras()) {
			int vertexSize = ProgramGlobals.getPrefs().getVertexSize();
			if (HitTestStuff.hitTest(camera.getPosition(), point, cameraHandler, vertexSize)) {
				return true;
			}
			if (HitTestStuff.hitTest(camera.getTargetPosition(), point, cameraHandler, vertexSize)) {
				return true;
			}
		}

		if (selectionMode == SelectionItemTypes.VERTEX) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (HitTestStuff.hitTest(geosetVertex, point, cameraHandler, ProgramGlobals.getPrefs().getVertexSize() / 3f)) {
						return true;
					}
				}
			}
		}


		if (selectionMode == SelectionItemTypes.CLUSTER) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (HitTestStuff.triHitTest(triangle, point, cameraHandler.getViewPortAntiRotMat())) {
						return true;
					}
				}
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (HitTestStuff.hitTest(geosetVertex, point, cameraHandler, ProgramGlobals.getPrefs().getVertexSize() / 3f)) {
						return true;
					}
				}
			}
		}

		if (selectionMode == SelectionItemTypes.GROUP) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (HitTestStuff.triHitTest(triangle, point, cameraHandler.getViewPortAntiRotMat())) {
						return true;
					}
				}
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (HitTestStuff.hitTest(geosetVertex, point, cameraHandler, ProgramGlobals.getPrefs().getVertexSize())) {
						return true;
					}
				}
			}
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (HitTestStuff.triHitTest(triangle, point, cameraHandler.getViewPortAntiRotMat())) {
						return true;
					}
				}
			}
		}
		if (selectionMode == SelectionItemTypes.ANIMATE) {
			for (IdObject object : modelView.getEditableIdObjects()) {
				RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
				double vertexSize = object.getClickRadius() * 2;
				if (HitTestStuff.hitTest(renderNode.getPivot(), point, cameraHandler, vertexSize)) {
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
