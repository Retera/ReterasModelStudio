package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.editor.render3d.RenderNodeCamera;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
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

	public SelectionManager(RenderModel editorRenderModel, ModelView modelView, SelectionItemTypes selectionMode) {
		super(editorRenderModel, modelView, selectionMode);
		vertexClusterDefinitions = new VertexClusterDefinitions();
	}

	public SelectionManager(RenderModel editorRenderModel, ModelView modelView, boolean moveLinked, SelectionItemTypes selectionMode) {
		super(editorRenderModel, modelView, selectionMode);
		this.moveLinked = moveLinked;
		vertexClusterDefinitions = new VertexClusterDefinitions();
	}

	private Set<GeosetVertex> getGroupBundle(Collection<GeosetVertex> selectedVertices) {
		Set<VertexGroupBundle> bundleSet = new HashSet<>();
		for (GeosetVertex vertex : selectedVertices) {
			bundleSet.add(new VertexGroupBundle(vertex.getGeoset(), vertex.getMatrixIndex()));
		}

		Set<GeosetVertex> verticesSelected = new HashSet<>();
		for (VertexGroupBundle bundle : bundleSet) {
			for (GeosetVertex geosetVertex : bundle.getGeoset().getVertices()) {
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

	public SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			Set<GeosetVertex> selectedVerts = addVertsFromArea(min, max, coordinateSystem);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, coordinateSystem);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, coordinateSystem);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> selectedVerts = addTrisFromArea(min, max, coordinateSystem);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, coordinateSystem);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, coordinateSystem);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

		if (selectionMode == SelectionItemTypes.CLUSTER) {
			Set<GeosetVertex> selectedVerts = getClusterBundle(vertexClusterDefinitions, addTrisFromArea(min, max, coordinateSystem));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, coordinateSystem);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, coordinateSystem);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

		if (selectionMode == SelectionItemTypes.GROUP) {
			Set<GeosetVertex> selectedVerts = getGroupBundle(addTrisFromArea(min, max, coordinateSystem));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, coordinateSystem);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, coordinateSystem);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

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


	public SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, Mat4 viewPortAntiRotMat, double sizeAdj) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			Set<GeosetVertex> selectedVerts = addVertsFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> selectedVerts = addTrisFromArea(min, max, viewPortAntiRotMat);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

		if (selectionMode == SelectionItemTypes.CLUSTER) {
			Set<GeosetVertex> selectedVerts = getClusterBundle(vertexClusterDefinitions, addTrisFromArea(min, max, viewPortAntiRotMat));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

		if (selectionMode == SelectionItemTypes.GROUP) {
			Set<GeosetVertex> selectedVerts = getGroupBundle(addTrisFromArea(min, max, viewPortAntiRotMat));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

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
				RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
//				double vertexSize = object.getClickRadius() * 2;
//				int vertexSize1 = ProgramGlobals.getPrefs().getVertexSize();
				double vertexSize = sizeAdj * object.getClickRadius()*2;
				if (modelView.isEditable(object) && HitTestStuff.hitTest(min, max, renderNode.getPivot(), viewPortAntiRotMat, vertexSize)) {
					selectedItems.add(object);
				}
			}
			Set<GeosetVertex> selectedVerts = addVertsFromArea(min, max, viewPortAntiRotMat, sizeAdj);

			Set<CameraNode> selectedCams = new HashSet<>();
			for(CameraNode cameraNode : modelView.getEditableCameraNodes()) {
				if (modelView.isEditable(cameraNode)){
					RenderNodeCamera renderNode = editorRenderModel.getRenderNode(cameraNode.getParent());
					Vec3 pivot = cameraNode instanceof CameraNode.SourceNode ? renderNode.getPivot() : renderNode.getTarget();
					if(HitTestStuff.hitTest(min, max, pivot, viewPortAntiRotMat, 2)){
						selectedCams.add(cameraNode);
					}
				}
			}
			return new SelectionBundle(selectedItems, selectedVerts, selectedCams);
		}
		return new SelectionBundle(Collections.emptySet());
	}


	public SelectionBundle getSelectionBundle(Vec3 min, Vec3 max, Mat4 viewPortAntiRotMat, double sizeAdj) {
		Vec2 minFlat = new Vec3(min).transform(viewPortAntiRotMat).getProjected((byte) 1, (byte) 2);
		Vec2 maxFlat = new Vec3(max).transform(viewPortAntiRotMat).getProjected((byte) 1, (byte) 2);
		System.out.println("min: " + minFlat + ", max: " + maxFlat + "(" + min + ", " + max + ")");
		if (selectionMode == SelectionItemTypes.VERTEX) {
			Set<GeosetVertex> selectedVerts = addVertsFromArea(minFlat, maxFlat, viewPortAntiRotMat, sizeAdj);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(minFlat, maxFlat, viewPortAntiRotMat, sizeAdj);
			Set<CameraNode> selectedCams = getCameraNodesFromArea(minFlat, maxFlat, viewPortAntiRotMat, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> selectedVerts = addTrisFromArea(minFlat, maxFlat, viewPortAntiRotMat);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(minFlat, maxFlat, viewPortAntiRotMat, sizeAdj);
			Set<CameraNode> selectedCams = getCameraNodesFromArea(minFlat, maxFlat, viewPortAntiRotMat, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

		if (selectionMode == SelectionItemTypes.CLUSTER) {
			Set<GeosetVertex> selectedVerts = getClusterBundle(vertexClusterDefinitions, addTrisFromArea(minFlat, maxFlat, viewPortAntiRotMat));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(minFlat, maxFlat, viewPortAntiRotMat, sizeAdj);
			Set<CameraNode> selectedCams = getCameraNodesFromArea(minFlat, maxFlat, viewPortAntiRotMat, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

		if (selectionMode == SelectionItemTypes.GROUP) {
			Set<GeosetVertex> selectedVerts = getGroupBundle(addTrisFromArea(minFlat, maxFlat, viewPortAntiRotMat));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(minFlat, maxFlat, viewPortAntiRotMat, sizeAdj);
			Set<CameraNode> selectedCams = getCameraNodesFromArea(minFlat, maxFlat, viewPortAntiRotMat, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCams);
		}

//		if (selectionMode == SelectionItemTypes.TPOSE) {
//			List<IdObject> selectedItems = new ArrayList<>();
//
//			for (IdObject object : modelView.getEditableIdObjects()) {
//				double vertexSize = object.getClickRadius();
//				if (HitTestStuff.hitTest(minFlat, maxFlat, object.getPivotPoint(), coordinateSystem, vertexSize)) {
//					selectedItems.add(object);
//				}
//				if (object instanceof CollisionShape) {
//					for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
//						if (HitTestStuff.hitTest(minFlat, maxFlat, vertex, coordinateSystem, IdObject.DEFAULT_CLICK_RADIUS)) {
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
//				double vertexSize1 = ProgramGlobals.getPrefs().getVertexSize();
				double vertexSize1 = sizeAdj * object.getClickRadius() / 2.0;
//				double vertexSize1 = cameraHandler.geomDist(1);
				if (modelView.isEditable(object) && HitTestStuff.hitTest(minFlat, maxFlat, renderNode.getPivot(), viewPortAntiRotMat, vertexSize1)) {
					selectedItems.add(object);
				}
			}
			Set<GeosetVertex> selectedVerts = addVertsFromArea(minFlat, maxFlat, viewPortAntiRotMat, sizeAdj);
			Set<CameraNode> selectedCams = new HashSet<>();
			for(CameraNode cameraNode : modelView.getEditableCameraNodes()) {
				if (modelView.isEditable(cameraNode)){
					RenderNodeCamera renderNode = editorRenderModel.getRenderNode(cameraNode.getParent());
					Vec3 pivot = cameraNode instanceof CameraNode.SourceNode ? renderNode.getPivot() : renderNode.getTarget();
					if(HitTestStuff.hitTest(minFlat, maxFlat, pivot, viewPortAntiRotMat, 2)){
						selectedCams.add(cameraNode);
					}
				}
			}
			return new SelectionBundle(selectedItems, selectedVerts, selectedCams);
		}
		return new SelectionBundle(Collections.emptySet());
	}

	private Set<CameraNode> getCameraNodesFromArea(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		Set<CameraNode> selectedCamNodes = new HashSet<>();
		for (CameraNode node : modelView.getEditableCameraNodes()) {
			int vertexSize = ProgramGlobals.getPrefs().getVertexSize();
			if (modelView.isEditable(node) && HitTestStuff.hitTest(min, max, node.getPosition(), coordinateSystem, vertexSize)) {
				selectedCamNodes.add(node);
			}
		}
		return selectedCamNodes;
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

	private Set<CameraNode> getCameraNodesFromArea(Vec2 min, Vec2 max, Mat4 viewPortMat, double sizeAdj) {
		Set<CameraNode> selectedCamNodes = new HashSet<>();
		double vertexSize = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;
		for (CameraNode node : modelView.getEditableCameraNodes()) {
			if (modelView.isEditable(node) && HitTestStuff.hitTest(min, max, node.getPosition(), viewPortMat, vertexSize)) {
				selectedCamNodes.add(node);
			}
		}
		return selectedCamNodes;
	}

	private Set<IdObject> getIdObjectsFromArea(Vec2 min, Vec2 max, Mat4 viewPortMat, double sizeAdj) {
		Set<IdObject> selectedItems = new HashSet<>();
		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertSize = sizeAdj * object.getClickRadius() / 2.0;
//			double vertSize = 1.0;
//			double vertSize = cameraHandler.geomDist(1.0);
			if (modelView.isEditable(object) && HitTestStuff.hitTest(min, max, object.getPivotPoint(), viewPortMat, vertSize)) {
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

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, Mat4 viewPortMat) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (HitTestStuff.triHitTest(triangle, min, max, viewPortMat)) {
					newSelection.addAll(Arrays.asList(triangle.getAll()));
				}
			}
		}
		newSelection.removeIf(vertex -> !modelView.isEditable(vertex));
		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, Mat4 viewPortAntiRotMat, double sizeAdj) {
		Set<GeosetVertex> newSelection = new HashSet<>();

		double vertSize = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;

		for (Geoset geoset : modelView.getEditableGeosets()) {
			RenderGeoset renderGeoset = editorRenderModel.getRenderGeoset(geoset);
			for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
				if (modelView.isEditable(renderVert.getVertex()) && HitTestStuff.hitTest(min, max, renderVert.getRenderPos(), viewPortAntiRotMat, vertSize))
					newSelection.add(renderVert.getVertex());
			}
		}
		return newSelection;
	}

	public double getCircumscribedSphereRadius(Vec3 sphereCenter, int tvertexLayerId) {

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
			for (CameraNode item : modelView.getSelectedCameraNodes()) {
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

	public Vec3 getCenter() {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER
				|| selectionMode == SelectionItemTypes.TPOSE) {
			return modelView.getSelectionCenter();
		}
		if (selectionMode == SelectionItemTypes.ANIMATE) {
			int tot = 0;
			Vec3 centerOfGroupSumHeap = new Vec3(0, 0, 0);
			for (IdObject object : modelView.getSelectedIdObjects()) {
				centerOfGroupSumHeap.add(editorRenderModel.getRenderNode(object).getRenderPivot());
				tot++;
			}
			for (CameraNode cameraNode : modelView.getEditableCameraNodes()) {
				if(cameraNode instanceof CameraNode.SourceNode){
					centerOfGroupSumHeap.add(editorRenderModel.getRenderNode(cameraNode).getRenderPivot());
					tot++;
				} else if (cameraNode instanceof CameraNode.TargetNode) {
					centerOfGroupSumHeap.add(editorRenderModel.getRenderNode(cameraNode).getTarget());
					tot++;
				}
			}
			if (modelView.getSelectedIdObjects().size() > 0) {
//				centerOfGroupSumHeap.scale(1f / modelView.getSelectedIdObjects().size());
				centerOfGroupSumHeap.scale(1f / tot);
			}
			return centerOfGroupSumHeap;
		}
		return new Vec3();
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


	public boolean selectableUnderCursor(Vec2 point, CoordinateSystem coordinateSystem) {
		for (IdObject object : modelView.getEditableIdObjects()) {
			if(modelView.isSelected(object)){
				double nodeSize = object.getClickRadius() / coordinateSystem.getZoom();
				if (HitTestStuff.hitTest(coordinateSystem.convertToViewVec2(object.getPivotPoint()), point, nodeSize)) {
					return true;
				}
				if (object instanceof CollisionShape) {
					for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
						double vertexSize = IdObject.DEFAULT_CLICK_RADIUS / 2.0 / coordinateSystem.getZoom();
						if (HitTestStuff.hitTest(coordinateSystem.convertToViewVec2(vertex), point, vertexSize)) {
							return true;
						}
					}
				}
			}
		}
		double vertexSize = ProgramGlobals.getPrefs().getVertexSize() / 2.0 / coordinateSystem.getZoom();
		for (CameraNode cameraNode : modelView.getEditableCameraNodes()) {
			if(modelView.isEditable(cameraNode)){
				if (HitTestStuff.hitTest(coordinateSystem.convertToViewVec2(cameraNode.getPosition()), point, vertexSize)) {
					return true;
				}
			}
		}

		if (selectionMode == SelectionItemTypes.VERTEX) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (GeosetVertex vertex : geoset.getVertices()) {
					if (modelView.isEditable(vertex) && HitTestStuff.hitTest(coordinateSystem.convertToViewVec2(vertex), point, vertexSize)) {
						return true;
					}
				}
			}
		}


		if(selectionMode == SelectionItemTypes.CLUSTER){
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (modelView.isEditable(triangle) && HitTestStuff.triHitTest(triangle, point, coordinateSystem)) {
						return true;
					}
				}
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (HitTestStuff.hitTest(coordinateSystem.convertToViewVec2(geosetVertex), point, vertexSize)) {
						return true;
					}
				}
			}
		}

		if(selectionMode == SelectionItemTypes.GROUP){
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (modelView.isEditable(triangle) && HitTestStuff.triHitTest(triangle, point, coordinateSystem)) {
						return true;
					}
				}
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (HitTestStuff.hitTest(coordinateSystem.convertToViewVec2(geosetVertex), point, vertexSize)) {
						return true;
					}
				}
			}
		}

		if(selectionMode == SelectionItemTypes.FACE){
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (modelView.isEditable(triangle) && HitTestStuff.triHitTest(triangle, point, coordinateSystem)) {
						return true;
					}
				}
			}
		}
		if(selectionMode == SelectionItemTypes.ANIMATE){
			for (IdObject object : modelView.getEditableIdObjects()) {
				RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
				double nodeSize = object.getClickRadius() * 2 / 2.0 / coordinateSystem.getZoom();
				if (HitTestStuff.hitTest(coordinateSystem.convertToViewVec2(renderNode.getPivot()), point, nodeSize)) {
					return true;
				}
			}
			return false;
		}

		return false;
	}

	public boolean selectableUnderCursor(Vec2 point, Mat4 viewPortAntiRotMat, double sizeAdj) {
		for (IdObject object : modelView.getEditableIdObjects()) {
			RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
			double vertexSize = sizeAdj * object.getClickRadius() * 2;
			if (HitTestStuff.hitTest(renderNode.getPivot(), point, viewPortAntiRotMat, vertexSize)) {
				return true;
			}
			if (object instanceof CollisionShape) {
				double vertexSize1 = sizeAdj * IdObject.DEFAULT_CLICK_RADIUS;
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					if (HitTestStuff.hitTest(vertex, point, viewPortAntiRotMat, vertexSize1)) {
						return true;
					}
				}
			}
		}

		double vertexSize2 = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;
		for (CameraNode cameraNode : modelView.getEditableCameraNodes()) {
			if (HitTestStuff.hitTest(cameraNode.getPosition(), point, viewPortAntiRotMat, vertexSize2)) {
				return true;
			}
		}

		if (selectionMode == SelectionItemTypes.VERTEX) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (GeosetVertex vertex : geoset.getVertices()) {
					if (modelView.isEditable(vertex) && HitTestStuff.hitTest(vertex, point, viewPortAntiRotMat, vertexSize2)) {
						return true;
					}
				}
			}
		}


		if (selectionMode == SelectionItemTypes.CLUSTER) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (modelView.isEditable(triangle) && HitTestStuff.triHitTest(triangle, point, viewPortAntiRotMat)) {
						return true;
					}
				}
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (HitTestStuff.hitTest(geosetVertex, point, viewPortAntiRotMat, vertexSize2)) {
						return true;
					}
				}
			}
		}

		if (selectionMode == SelectionItemTypes.GROUP) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (modelView.isEditable(triangle) && HitTestStuff.triHitTest(triangle, point, viewPortAntiRotMat)) {
						return true;
					}
				}
				for (GeosetVertex vertex : geoset.getVertices()) {
					if (modelView.isEditable(vertex) && HitTestStuff.hitTest(vertex, point, viewPortAntiRotMat, vertexSize2)) {
						return true;
					}
				}
			}
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (modelView.isEditable(triangle) && HitTestStuff.triHitTest(triangle, point, viewPortAntiRotMat)) {
						return true;
					}
				}
			}
		}
		if (selectionMode == SelectionItemTypes.ANIMATE) {
			for (IdObject object : modelView.getEditableIdObjects()) {
				RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
				double vertexSize = sizeAdj * object.getClickRadius() * 2;
//				double vertexSize = 1;
//				double vertexSize = cameraHandler.geomDist(1.0);
				if (HitTestStuff.hitTest(renderNode.getPivot(), point, viewPortAntiRotMat, vertexSize)) {
					return true;
				}
			}
			return false;
		}

		return false;
	}
}
