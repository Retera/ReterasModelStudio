package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.editor.render3d.RenderNodeCamera;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.SelectionBoxHelper;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.awt.*;
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
	private boolean moveLinked;

	public SelectionManager(RenderModel editorRenderModel, ModelView modelView, SelectionItemTypes selectionMode) {
		super(editorRenderModel, modelView, selectionMode);
	}

	public SelectionManager(RenderModel editorRenderModel, ModelView modelView, boolean moveLinked, SelectionItemTypes selectionMode) {
		super(editorRenderModel, modelView, selectionMode);
		this.moveLinked = moveLinked;
	}

	private Set<GeosetVertex> getGroupBundle(Collection<GeosetVertex> selectedVertices) {
		Set<VertexMatrixBundle> bundleSet = new HashSet<>();
		for (GeosetVertex vertex : selectedVertices) {
			bundleSet.add(new VertexMatrixBundle(vertex.getGeoset(), vertex.getMatrix()));
		}

		Set<GeosetVertex> verticesSelected = new HashSet<>();
		for (VertexMatrixBundle bundle : bundleSet) {
			for (GeosetVertex geosetVertex : bundle.getGeoset().getVertices()) {
				if (bundle.sameMatrix(geosetVertex.getMatrix())) {
					verticesSelected.add(geosetVertex);
				}
			}
		}
		return verticesSelected;
	}

	private Set<GeosetVertex> getClusterBundle(Collection<GeosetVertex> selectedVertices) {
		Set<GeosetVertex> verticesSelected = new HashSet<>();
		VertexClusterDefinitions vertexClusterDefinitions = new VertexClusterDefinitions(selectedVertices);

		for (GeosetVertex vertex : selectedVertices) {
			if (!verticesSelected.contains(vertex)) {
				verticesSelected.addAll(vertexClusterDefinitions.getVertexBundle(vertex));
			}
		}

		return verticesSelected;
	}


	public SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, SelectionBoxHelper viewBox, double sizeAdj) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			Set<GeosetVertex> selectedVerts = addVertsFromArea(min, max, viewBox, sizeAdj);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewBox, sizeAdj);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, viewBox, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> selectedVerts = addTrisFromArea(min, max, viewBox, sizeAdj);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewBox, sizeAdj);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, viewBox, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

		if (selectionMode == SelectionItemTypes.CLUSTER) {
			Set<GeosetVertex> selectedVerts = getClusterBundle(addTrisFromArea(min, max, viewBox, sizeAdj));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewBox, sizeAdj);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, viewBox, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

		if (selectionMode == SelectionItemTypes.GROUP) {
			Set<GeosetVertex> selectedVerts = getGroupBundle(addTrisFromArea(min, max, viewBox, sizeAdj));
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewBox, sizeAdj);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, viewBox, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}

		if (selectionMode == SelectionItemTypes.TPOSE) {
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewBox, sizeAdj);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, viewBox, sizeAdj);
			return new SelectionBundle(selectedObjs, selectedCamNodes);
		}

		if (selectionMode == SelectionItemTypes.ANIMATE) {
			Set<GeosetVertex> selectedVerts = addVertsFromArea(min, max, viewBox, sizeAdj);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewBox, sizeAdj);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, viewBox, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
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
			Set<GeosetVertex> selectedVerts = getClusterBundle(addTrisFromArea(min, max, viewPortAntiRotMat));
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

		if (selectionMode == SelectionItemTypes.TPOSE) {
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			Set<CameraNode> selectedCams = getCameraNodesFromArea(min, max, viewPortAntiRotMat, sizeAdj);

			return new SelectionBundle(selectedObjs, selectedCams);
		}

		if (selectionMode == SelectionItemTypes.ANIMATE) {
			Set<GeosetVertex> selectedVerts = addVertsFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			Set<IdObject> selectedObjs = getIdObjectsFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			Set<CameraNode> selectedCamNodes = getCameraNodesFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			return new SelectionBundle(selectedVerts, selectedObjs, selectedCamNodes);
		}
		return new SelectionBundle(Collections.emptySet());
	}

	private Set<CameraNode> getCameraNodesFromArea(Vec2 min, Vec2 max, Mat4 viewPortMat, double sizeAdj) {
		Set<CameraNode> selectedCamNodes = new HashSet<>();
		Vec2 vertexV2 = new Vec2();
		double vertexSize = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;
		for (CameraNode node : modelView.getEditableCameraNodes()) {
			if (modelView.isEditable(node)) {
				RenderNodeCamera renderNode = editorRenderModel.getRenderNode(node.getParent());
				Vec3 pivot = node instanceof CameraNode.SourceNode ? renderNode.getPivot() : renderNode.getTarget();
				vertexV2.setAsProjection(pivot, viewPortMat);
				if (HitTestStuff.hitTest(min, max, vertexV2, vertexSize)) {
					selectedCamNodes.add(node);
				}
			}
		}
		return selectedCamNodes;
	}

	private Set<IdObject> getIdObjectsFromArea(Vec2 min, Vec2 max, Mat4 viewPortMat, double sizeAdj) {
		Set<IdObject> selectedItems = new HashSet<>();
		Vec2 vertexV2 = new Vec2();
		for (IdObject object : modelView.getEditableIdObjects()) {
			if (modelView.isEditable(object)) {
				double vertSize = sizeAdj * object.getClickRadius() / 2.0;
				vertexV2.setAsProjection(object.getPivotPoint(), viewPortMat);
				if (HitTestStuff.hitTest(min, max, vertexV2, vertSize)) {
					selectedItems.add(object);
				}
//				if (object instanceof CollisionShape) {
//					for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
//						if (HitTestStuff.hitTest(min, max, vertexV2, vertSize)) {
//							selectedItems.add(vertex);
//						}
//					}
//				}
			}
		}

		return selectedItems;
	}

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, Mat4 viewPortMat) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		Vec2[] triPoints = new Vec2[] {new Vec2(), new Vec2(), new Vec2()};
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (modelView.isEditable(triangle)){
					triPoints[0].setAsProjection(triangle.get(0), viewPortMat);
					triPoints[1].setAsProjection(triangle.get(1), viewPortMat);
					triPoints[2].setAsProjection(triangle.get(2), viewPortMat);
					if (HitTestStuff.triangleOverlapArea(min, max, triPoints)) {
						newSelection.addAll(Arrays.asList(triangle.getAll()));
					}
				}
			}
		}
		newSelection.removeIf(vertex -> !modelView.isEditable(vertex));
		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, Mat4 viewPortAntiRotMat, double sizeAdj) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		Vec2 vertexV2 = new Vec2();

		double vertSize = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;

		for (Geoset geoset : modelView.getVisEdGeosets()) {
			RenderGeoset renderGeoset = editorRenderModel.getRenderGeoset(geoset);
			for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
				if (modelView.isEditable(renderVert.getVertex())){
					vertexV2.setAsProjection(renderVert.getRenderPos(), viewPortAntiRotMat);
					if (HitTestStuff.hitTest(min, max, vertexV2, vertSize)){
						newSelection.add(renderVert.getVertex());
					}
				}
			}
		}
		return newSelection;
	}
// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓  ViewBox  ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	private Set<CameraNode> getCameraNodesFromArea(Vec2 min, Vec2 max, SelectionBoxHelper viewBox, double sizeAdj) {
		Set<CameraNode> selectedCamNodes = new HashSet<>();
		float vertexSize = (float) (sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0);
		for (CameraNode node : modelView.getEditableCameraNodes()) {
			if (modelView.isEditable(node)) {

				RenderNodeCamera renderNode = editorRenderModel.getRenderNode(node.getParent());
				Vec3 pivot = node instanceof CameraNode.SourceNode ? renderNode.getPivot() : renderNode.getTarget();
				if(viewBox.pointInBox(pivot, vertexSize)) {
					selectedCamNodes.add(node);
				}
			}
		}
		return selectedCamNodes;
	}

	private Set<IdObject> getIdObjectsFromArea(Vec2 min, Vec2 max, SelectionBoxHelper viewBox, double sizeAdj) {
		Set<IdObject> selectedItems = new HashSet<>();
		for (IdObject object : modelView.getEditableIdObjects()) {
			if (modelView.isEditable(object)) {
				float vertSize = (float) (object.getClickRadius() / 5.0);

				RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
				if(viewBox.pointInBox(renderNode.getPivot(), vertSize)) {
					selectedItems.add(object);
				}
//				if (object instanceof CollisionShape) {
//					for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
//						if (viewBox.pointInBox(object.getPivotPoint(), vertSize)) {
//							selectedItems.add(vertex);
//						}
//					}
//				}
			}
		}
		return selectedItems;
	}

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, SelectionBoxHelper viewBox, double sizeAdj) {
		// ToDo fix!
		Set<GeosetVertex> newSelection = new HashSet<>();

		float vertSize = (float) (sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0);
//		viewBox.printRay();
		System.out.println("");
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			RenderGeoset renderGeoset = editorRenderModel.getRenderGeoset(geoset);
			for (Triangle triangle : geoset.getTriangles()) {
				if (modelView.isEditable(triangle)){
					Vec3 renderPos0 = renderGeoset.getRenderVert(triangle.get(0)).getRenderPos();
					Vec3 renderPos1 = renderGeoset.getRenderVert(triangle.get(1)).getRenderPos();
					Vec3 renderPos2 = renderGeoset.getRenderVert(triangle.get(2)).getRenderPos();
					if(viewBox.anyPointInBox(vertSize, renderPos0, renderPos1, renderPos2)
							|| viewBox.triIntersectBox(renderPos0, renderPos1, renderPos2)){
//						System.out.println("point in box!");
						newSelection.add(triangle.get(0));
						newSelection.add(triangle.get(1));
						newSelection.add(triangle.get(2));
					}
				}
			}
		}
		newSelection.removeIf(vertex -> !modelView.isEditable(vertex));
		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, SelectionBoxHelper viewBox, double sizeAdj) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		float vertSize = (float) (sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0);

		for (Geoset geoset : modelView.getVisEdGeosets()) {
			RenderGeoset renderGeoset = editorRenderModel.getRenderGeoset(geoset);
			for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
				if (modelView.isEditable(renderVert.getVertex())){
					if(viewBox.pointInBox(renderVert.getRenderPos(), vertSize)) {
						newSelection.add(renderVert.getVertex());
					}
				}
			}
		}
		return newSelection;
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

	public double getCircumscribedSphereRadius(Vec2 center) {
		if (selectionMode == SelectionItemTypes.VERTEX || selectionMode == SelectionItemTypes.FACE || selectionMode == SelectionItemTypes.CLUSTER || selectionMode == SelectionItemTypes.GROUP) {
			double radius = 0;
			for (GeosetVertex item : modelView.getSelectedVertices()) {
				if (uvLayerIndex < item.getTverts().size()) {
					double distance = center.distance(item.getTVertex(uvLayerIndex));
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
				|| selectionMode == SelectionItemTypes.CLUSTER) {
			return modelView.getSelectionCenter();
		}
		if (selectionMode == SelectionItemTypes.ANIMATE || selectionMode == SelectionItemTypes.TPOSE) {
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

	public Vec2 getUVCenter() {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			return Vec2.centerOfGroup(getSelectedTVertices());
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<Vec2> selectedVertices = new HashSet<>();
			for (Triangle triangle : modelView.getSelectedTriangles()) {
				for (GeosetVertex geosetVertex : triangle.getVerts()) {
					if (uvLayerIndex < geosetVertex.getTverts().size()) {
						selectedVertices.add(geosetVertex.getTVertex(uvLayerIndex));
					}
				}
			}
			return Vec2.centerOfGroup(selectedVertices);
		}
		return Vec2.ORIGIN;
	}

	public Collection<? extends Vec2> getSelectedTVertices() {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.CLUSTER
				|| selectionMode == SelectionItemTypes.GROUP) {
			Set<Vec2> selectedTVertices = new HashSet<>();
			for (GeosetVertex vertex : modelView.getSelectedVertices()) {
				if (uvLayerIndex < vertex.getTverts().size()) {
					selectedTVertices.add(vertex.getTVertex(uvLayerIndex));
				}
			}
			return selectedTVertices;
		}
		return Collections.emptySet();
	}

	public boolean selectableUnderCursor(Vec2 point, Mat4 viewPortAntiRotMat, double sizeAdj) {
		if (selectionMode == SelectionItemTypes.VERTEX || selectionMode == SelectionItemTypes.ANIMATE) {
			return anyEditableNodeIn(point, viewPortAntiRotMat, sizeAdj)
					|| anyEditableCameraIn(point, viewPortAntiRotMat, sizeAdj)
					|| anyEditableVertexIn(point, viewPortAntiRotMat, sizeAdj);
		}

		if (selectionMode == SelectionItemTypes.CLUSTER || selectionMode == SelectionItemTypes.GROUP) {
			return anyEditableNodeIn(point, viewPortAntiRotMat, sizeAdj)
					|| anyEditableCameraIn(point, viewPortAntiRotMat, sizeAdj)
					|| anyEditableGeometryIn(point, viewPortAntiRotMat, sizeAdj);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			return anyEditableNodeIn(point, viewPortAntiRotMat, sizeAdj)
					|| anyEditableCameraIn(point, viewPortAntiRotMat, sizeAdj)
					|| anyEditableTriangleIn(point, viewPortAntiRotMat);
		}

		if (selectionMode == SelectionItemTypes.TPOSE) {
			return anyEditableCameraIn(point, viewPortAntiRotMat, sizeAdj)
					|| anyEditableVertexIn(point, viewPortAntiRotMat, sizeAdj);
		}

		return false;
	}

	private boolean anyEditableCameraIn(Vec2 point, Mat4 viewPortAntiRotMat, double sizeAdj) {
		Vec2 vertexV2 = new Vec2();
		double vertexSize2 = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;
		for (CameraNode cameraNode : modelView.getEditableCameraNodes()) {
			vertexV2.setAsProjection(cameraNode.getPosition(), viewPortAntiRotMat);
			if (HitTestStuff.hitTest(point, vertexV2, vertexSize2)) {
				return true;
			}
		}
		return false;
	}

	private boolean anyEditableNodeIn(Vec2 point, Mat4 viewPortAntiRotMat, double sizeAdj) {
		Vec2 vertexV2 = new Vec2();
		for (IdObject object : modelView.getEditableIdObjects()) {
			RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
			double vertexSize = sizeAdj * object.getClickRadius() * 2;
			vertexV2.setAsProjection(renderNode.getPivot(), viewPortAntiRotMat);
			if (HitTestStuff.hitTest(point, vertexV2, vertexSize)) {
				return true;
			}
			if (object instanceof CollisionShape) {
				double vertexSize1 = sizeAdj * IdObject.DEFAULT_CLICK_RADIUS;
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					vertexV2.setAsProjection(vertex, viewPortAntiRotMat);
					if (HitTestStuff.hitTest(point, vertexV2, vertexSize1)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean anyEditableGeometryIn(Vec2 point, Mat4 viewPortAntiRotMat, double sizeAdj) {
		Vec2 vertexV2 = new Vec2();
		double vertexSize = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;
		Vec2[] triPoints = new Vec2[] {new Vec2(), new Vec2(), new Vec2()};
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (modelView.isEditable(triangle)) {
					triPoints[0].setAsProjection(triangle.get(0), viewPortAntiRotMat);
					triPoints[1].setAsProjection(triangle.get(1), viewPortAntiRotMat);
					triPoints[2].setAsProjection(triangle.get(2), viewPortAntiRotMat);
					if (HitTestStuff.pointInTriangle(point, triPoints[0], triPoints[1], triPoints[2])) {
						return true;
					}
				}
			}
			for (GeosetVertex vertex : geoset.getVertices()) {
				if (modelView.isEditable(vertex)) {
					vertexV2.setAsProjection(vertex, viewPortAntiRotMat);
					if (HitTestStuff.hitTest(point, vertexV2, vertexSize)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean anyEditableTriangleIn(Vec2 point, Mat4 viewPortAntiRotMat) {
		Vec2[] triPoints = new Vec2[] {new Vec2(), new Vec2(), new Vec2()};
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (modelView.isEditable(triangle)) {
					triPoints[0].setAsProjection(triangle.get(0), viewPortAntiRotMat);
					triPoints[1].setAsProjection(triangle.get(1), viewPortAntiRotMat);
					triPoints[2].setAsProjection(triangle.get(2), viewPortAntiRotMat);
					if (HitTestStuff.pointInTriangle(point, triPoints[0], triPoints[1], triPoints[2])) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean anyEditableVertexIn(Vec2 point, Mat4 viewPortAntiRotMat, double sizeAdj) {
		Vec2 vertexV2 = new Vec2();
		double vertexSize = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				if (modelView.isEditable(vertex)) {
					vertexV2.setAsProjection(vertex, viewPortAntiRotMat);
					if (HitTestStuff.hitTest(point, vertexV2, vertexSize)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean selectableUnderCursor(Vec2 point, SelectionBoxHelper viewBox, double sizeAdj) {
		Vec2 vertexV2 = new Vec2();

		if (selectionMode == SelectionItemTypes.VERTEX || selectionMode == SelectionItemTypes.ANIMATE) {
			return anyEditableNodeIn(viewBox, sizeAdj)
					|| anyEditableCameraIn(viewBox, sizeAdj)
					|| anyEditableVertexIn(viewBox, sizeAdj);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			return anyEditableNodeIn(viewBox, sizeAdj)
					|| anyEditableCameraIn(viewBox, sizeAdj)
					|| anyEditableTriangleIn(viewBox, sizeAdj);
		}

		if (selectionMode == SelectionItemTypes.CLUSTER || selectionMode == SelectionItemTypes.GROUP) {
			return anyEditableNodeIn(viewBox, sizeAdj)
					|| anyEditableCameraIn(viewBox, sizeAdj)
					|| anyEditableGeometryIn(viewBox, sizeAdj);
		}

		if (selectionMode == SelectionItemTypes.TPOSE) {
			return anyEditableNodeIn(viewBox, sizeAdj)
					|| anyEditableCameraIn(viewBox, sizeAdj);
		}

		return false;
	}

	private boolean anyEditableNodeIn(SelectionBoxHelper viewBox, double sizeAdj) {
		for (IdObject object : modelView.getEditableIdObjects()) {
			if (modelView.isEditable(object)) {
				float vertSize = (float) (object.getClickRadius() / 5.0);

				RenderNode2 renderNode = editorRenderModel.getRenderNode(object);
				if(viewBox.pointInBox(renderNode.getPivot(), vertSize)) {
					return true;
				}
//				if (object instanceof CollisionShape) {
//					for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
//						if (viewBox.pointInBox(vertex, vertSize)) {
//							return true;
//						}
//					}
//				}
			}
		}
		return false;
	}

	private boolean anyEditableCameraIn(SelectionBoxHelper viewBox, double sizeAdj) {
		float vertexSize = (float) (sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0);
		for (CameraNode node : modelView.getEditableCameraNodes()) {
			if (modelView.isEditable(node)) {

				RenderNodeCamera renderNode = editorRenderModel.getRenderNode(node.getParent());
				Vec3 pivot = node instanceof CameraNode.SourceNode ? renderNode.getPivot() : renderNode.getTarget();
				if(viewBox.pointInBox(pivot, vertexSize)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean anyEditableVertexIn(SelectionBoxHelper viewBox, double sizeAdj) {
		float vertSize = (float) (sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0);

		for (Geoset geoset : modelView.getVisEdGeosets()) {
			RenderGeoset renderGeoset = editorRenderModel.getRenderGeoset(geoset);
			for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
				if (modelView.isEditable(renderVert.getVertex())){
					if(viewBox.pointInBox(renderVert.getRenderPos(), vertSize)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean anyEditableGeometryIn(SelectionBoxHelper viewBox, double sizeAdj) {
		float vertSize = (float) (sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0);

		for (Geoset geoset : modelView.getVisEdGeosets()) {
			RenderGeoset renderGeoset = editorRenderModel.getRenderGeoset(geoset);
			for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
				if (modelView.isEditable(renderVert.getVertex())){
					if(viewBox.pointInBox(renderVert.getRenderPos(), vertSize)) {
						return true;
					}
				}
			}
			for (Triangle triangle : geoset.getTriangles()) {
				if (modelView.isEditable(triangle)){
					Vec3 renderPos0 = renderGeoset.getRenderVert(triangle.get(0)).getRenderPos();
					Vec3 renderPos1 = renderGeoset.getRenderVert(triangle.get(1)).getRenderPos();
					Vec3 renderPos2 = renderGeoset.getRenderVert(triangle.get(2)).getRenderPos();
					if(viewBox.triIntersectBox(renderPos0, renderPos1, renderPos2)){
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean anyEditableTriangleIn(SelectionBoxHelper viewBox, double sizeAdj) {
		float vertSize = (float) (sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0);
		System.out.println("");
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			RenderGeoset renderGeoset = editorRenderModel.getRenderGeoset(geoset);
			for (Triangle triangle : geoset.getTriangles()) {
				if (modelView.isEditable(triangle)){
					Vec3 renderPos0 = renderGeoset.getRenderVert(triangle.get(0)).getRenderPos();
					Vec3 renderPos1 = renderGeoset.getRenderVert(triangle.get(1)).getRenderPos();
					Vec3 renderPos2 = renderGeoset.getRenderVert(triangle.get(2)).getRenderPos();
					if(viewBox.anyPointInBox(vertSize, renderPos0, renderPos1, renderPos2)
							|| viewBox.triIntersectBox(renderPos0, renderPos1, renderPos2)){
						return true;
					}
				}
			}
		}
		return false;
	}
}
