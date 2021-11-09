package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.awt.*;
import java.util.*;

public class TVertSelectionManager extends AbstractSelectionManager {

	private static final Color FACE_SELECTED_COLOR = new Color(1f, 0.45f, 0.45f, 0.3f);
	private static final Color FACE_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
	private static final Color FACE_NOT_SELECTED_COLOR = new Color(0.45f, 0.45f, 1f, 0.3f);
	private static final Color CLUSTER_SELECTED_COLOR = new Color(1f, 0.45f, 0.75f, 0.3f);
	private static final Color CLUSTER_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
	private static final Color GROUP_SELECTED_COLOR = new Color(1f, 0.75f, 0.45f, 0.3f);
	private static final Color GROUP_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
	private final Set<SelectionListener> listeners = new HashSet<>();
	private boolean moveLinked;

	private VertexClusterDefinitions vertexClusterDefinitions;

	public TVertSelectionManager(RenderModel editorRenderModel, ModelView modelView, SelectionItemTypes selectionMode) {
		super(editorRenderModel, modelView, selectionMode);
	}

//	public UndoAction setSelectedRegion(Vec2 min, Vec2 max, CameraHandler cameraHandler) {
//		System.out.println("setSelectedRegion: " + min + "" + max);
//		SelectionBundle newSelection = getSelectionBundle(min, max, cameraHandler);
//		return new SetSelectionUggAction(newSelection, modelView, "select");
//	}
//
//	public UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CameraHandler cameraHandler) {
//		SelectionBundle newSelection = getSelectionBundle(min, max, cameraHandler);
//		return new RemoveSelectionUggAction(newSelection, modelView);
//	}
//
//	public UndoAction addSelectedRegion(Vec2 min, Vec2 max, CameraHandler cameraHandler) {
//		SelectionBundle newSelection = getSelectionBundle(min, max, cameraHandler);
//		return new AddSelectionUggAction(newSelection, modelView);
//	}
//
//	public UndoAction setSelectedRegion(Vec3 min3, Vec3 max3, CameraHandler cameraHandler) {
//		System.out.println("setSelectedRegion: " + min3 + "" + max3);
//		SelectionBundle newSelection = getSelectionBundle(min3, max3, cameraHandler);
//		return new SetSelectionUggAction(newSelection, modelView, "select");
//	}
//
//	public UndoAction removeSelectedRegion(Vec3 min3, Vec3 max3, CameraHandler cameraHandler) {
//		SelectionBundle newSelection = getSelectionBundle(min3, max3, cameraHandler);
//		return new RemoveSelectionUggAction(newSelection, modelView);
//	}
//
//	public UndoAction addSelectedRegion(Vec3 min3, Vec3 max3, CameraHandler cameraHandler) {
//		SelectionBundle newSelection = getSelectionBundle(min3, max3, cameraHandler);
//		return new AddSelectionUggAction(newSelection, modelView);
//	}
//
//	public final UndoAction setSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
//		SelectionBundle newSelection = getSelectionBundle(min, max, coordinateSystem);
//		return new SetSelectionUggAction(newSelection, modelView, "select");
//	}
//
//	public final UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
//		SelectionBundle newSelection = getSelectionBundle(min, max, coordinateSystem);
//		return new RemoveSelectionUggAction(newSelection, modelView);
//	}
//
//	public final UndoAction addSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
//		SelectionBundle newSelection = getSelectionBundle(min, max, coordinateSystem);
//		return new AddSelectionUggAction(newSelection, modelView);
//	}

	public SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, CameraHandler cameraHandler) {
		double vertexSize = cameraHandler.geomDist(ProgramGlobals.getPrefs().getVertexSize() / 2.0);
//		min.transform(cameraHandler.getViewPortAntiRotMat());
//		max.transform(cameraHandler.getViewPortAntiRotMat());
		return genericSelect(min, max, vertexSize, cameraHandler);
	}

	public SelectionBundle getSelectionBundle(Vec3 min3, Vec3 max3, CameraHandler cameraHandler) {
		double vertexSize = cameraHandler.geomDist(ProgramGlobals.getPrefs().getVertexSize() / 2.0);
		Vec2 min = min3.transform(cameraHandler.getViewPortAntiRotMat()).getProjected((byte) 1, (byte) 2);
		Vec2 max = max3.transform(cameraHandler.getViewPortAntiRotMat()).getProjected((byte) 1, (byte) 2);
		return genericSelect(min, max, vertexSize);
	}

	public SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		double vertexSize = ProgramGlobals.getPrefs().getVertexSize() / coordinateSystem.getZoom();
		return genericSelect(min, max, vertexSize);
	}


	public Vec3 getCenter() {
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER
				|| selectionMode == SelectionItemTypes.TPOSE) {
			return new Vec3().setCoords((byte) 0, (byte) 1, modelView.getTSelectionCenter());
//			return modelView.getSelectionCenter();
		}
		if (selectionMode == SelectionItemTypes.ANIMATE) {
			Vec3 centerOfGroupSumHeap = new Vec3(0, 0, 0);
			Set<IdObject> selectedIdObjects = modelView.getSelectedIdObjects();
			for (IdObject object : selectedIdObjects) {
				Vec4 pivotHeap = new Vec4(object.getPivotPoint(), 1);
				pivotHeap.transform(editorRenderModel.getRenderNode(object).getWorldMatrix());
				centerOfGroupSumHeap.add(pivotHeap.getVec3());
			}
			if (selectedIdObjects.size() > 0) {
				centerOfGroupSumHeap.scale(1f / selectedIdObjects.size());
			}
			return centerOfGroupSumHeap;
		}
		return new Vec3();
	}

	public SelectionBundle genericSelect(Vec2 min, Vec2 max, double vertexSize) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
//			System.out.println("Tvert genericSelect, " + "min: " + min + ", max" + max);
			Set<GeosetVertex> selectedItems = addVertsFromArea(min, max, vertexSize, 0);
			return new SelectionBundle(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> newSel = addTrisFromArea(min, max, 0);
			return new SelectionBundle(newSel);
		}
		return new SelectionBundle(Collections.emptySet());
	}

	public SelectionBundle genericSelect(Vec2 min, Vec2 max, double vertexSize, CameraHandler cameraHandler) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
//			System.out.println("Tvert genericSelect, " + "min: " + min + ", max" + max);
			Set<GeosetVertex> selectedItems = addVertsFromArea(min, max, vertexSize, 0, cameraHandler);
			return new SelectionBundle(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> newSel = addTrisFromArea(min, max, 0, cameraHandler);
			return new SelectionBundle(newSel);
		}
		return new SelectionBundle(Collections.emptySet());
	}

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, int uvLayerIndex) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (HitTestStuff.triHitTest(triangle, min, max, uvLayerIndex)) {
					newSelection.addAll(Arrays.asList(triangle.getVerts()));
				}
			}
		}
		return newSelection;
	}

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, int uvLayerIndex, CameraHandler cameraHandler) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (HitTestStuff.triHitTest(triangle, min, max, uvLayerIndex, cameraHandler)) {
					newSelection.addAll(Arrays.asList(triangle.getVerts()));
				}
			}
		}
		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, double vertexSize, int uvLayerIndex) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (geosetVertex.getTverts().size() > uvLayerIndex) {
					if (HitTestStuff.hitTest(min, max, geosetVertex.getTVertex(uvLayerIndex), vertexSize)) {
						newSelection.add(geosetVertex);
					}
				}
			}
		}

		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, double vertexSize, int uvLayerIndex, CameraHandler cameraHandler) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (geosetVertex.getTverts().size() > uvLayerIndex) {
					Vec2 tVertex = new Vec2(geosetVertex.getTVertex(uvLayerIndex)).transform(cameraHandler.getViewPortAntiRotMat());
					if (HitTestStuff.hitTest(min, max, tVertex, vertexSize)) {
						newSelection.add(geosetVertex);
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

	public Collection<Vec2> getSelectedTVertices(int tvertexLayerId) {
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
		if (selectionMode == SelectionItemTypes.VERTEX) {
			double vertexSize = ProgramGlobals.getPrefs().getVertexSize() / 2.0 / coordinateSystem.getZoom();
			Vec2 point2 = new Vec2(coordinateSystem.geomX(point.x), coordinateSystem.geomY(point.y));
			;
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (geosetVertex.getTverts().size() > 0) {
//						if (HitTestStuff.hitTest(geosetVertex.getTVertex(0), point, vertexSize)) {
						if (HitTestStuff.hitTest(geosetVertex.getTVertex(0), point2, vertexSize)) {
							return true;
						}
					}
				}
			}
		} else if (selectionMode == SelectionItemTypes.FACE) {
			Vec2 point2 = new Vec2(coordinateSystem.geomX(point.x), coordinateSystem.geomY(point.y));
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
//					if (HitTestStuff.triHitTest(triangle, point, 0)) {
					if (HitTestStuff.triHitTest(triangle, point2, 0)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean selectableUnderCursor(Vec2 point, CameraHandler cameraHandler) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			double vertexSize = cameraHandler.geomDist(ProgramGlobals.getPrefs().getVertexSize() / 2.0);
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (geosetVertex.getTverts().size() > 0) {
						if (HitTestStuff.hitTest(geosetVertex.getTVertex(0), point, vertexSize)) {
							return true;
						}
					}
				}
			}
		} else if (selectionMode == SelectionItemTypes.FACE) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (HitTestStuff.triHitTest(triangle, point, 0)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
