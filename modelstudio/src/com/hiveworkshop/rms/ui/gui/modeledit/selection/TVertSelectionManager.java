package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.SelectionBoxHelper;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

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

	public SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, Mat4 viewPortAntiRotMat, double sizeAdj) {
		double vertexSize = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;
		return genericSelect(min, max, vertexSize, viewPortAntiRotMat);
	}

	public SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, SelectionBoxHelper viewBox, double sizeAdj) {
		double vertexSize = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;
		return genericSelect(min, max, vertexSize, viewBox);
	}

	public SelectionBundle genericSelect(Vec2 min, Vec2 max, double vertexSize) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			Set<GeosetVertex> selectedItems = addVertsFromArea(min, max, vertexSize, 0);
			return new SelectionBundle(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> newSel = addTrisFromArea(min, max, 0);
			return new SelectionBundle(newSel);
		}
		return new SelectionBundle(Collections.emptySet());
	}

	public SelectionBundle genericSelect(Vec2 min, Vec2 max, double vertexSize, Mat4 viewPortAntiRotMat) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
//			System.out.println("Tvert genericSelect, " + "min: " + min + ", max" + max);
			Set<GeosetVertex> selectedItems = addVertsFromArea(min, max, vertexSize, 0, viewPortAntiRotMat);
			return new SelectionBundle(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> newSel = addTrisFromArea(min, max, 0, viewPortAntiRotMat);
			return new SelectionBundle(newSel);
		}
		return new SelectionBundle(Collections.emptySet());
	}

	public SelectionBundle genericSelect(Vec2 min, Vec2 max, double vertexSize, SelectionBoxHelper viewBox) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
//			System.out.println("Tvert genericSelect, " + "min: " + min + ", max" + max);
			Set<GeosetVertex> selectedItems = addVertsFromArea(min, max, vertexSize, 0, viewBox);
			return new SelectionBundle(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> newSel = addTrisFromArea(min, max, 0, viewBox);
			return new SelectionBundle(newSel);
		}
		return new SelectionBundle(Collections.emptySet());
	}

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, int uvLayerIndex) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (modelView.isEditable(triangle) && HitTestStuff.triHitTest(triangle, min, max, uvLayerIndex)) {
					newSelection.addAll(Arrays.asList(triangle.getVerts()));
				}
			}
		}
		return newSelection;
	}

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, int uvLayerIndex, Mat4 viewPortAntiRotMat) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (modelView.isEditable(triangle) && HitTestStuff.triHitTest(triangle, min, max, uvLayerIndex, viewPortAntiRotMat)) {
					newSelection.addAll(Arrays.asList(triangle.getVerts()));
				}
			}
		}
		return newSelection;
	}

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, int uvLayerIndex, SelectionBoxHelper viewBox) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		Vec3 v0 = new Vec3();
		Vec3 v1 = new Vec3();
		Vec3 v2 = new Vec3();
		float vertSize = (float) (1 * ProgramGlobals.getPrefs().getVertexSize() / 2.0);
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (modelView.isEditable(triangle)){
					v0.set(triangle.get(0).getTVertex(uvLayerIndex), 0);
					v1.set(triangle.get(1).getTVertex(uvLayerIndex), 0);
					v2.set(triangle.get(2).getTVertex(uvLayerIndex), 0);
					if(viewBox.anyPointInBox(vertSize, v0, v1, v2) || viewBox.triIntersectBox(v0, v1, v2)){
//						System.out.println("point in box!");
						newSelection.add(triangle.get(0));
						newSelection.add(triangle.get(1));
						newSelection.add(triangle.get(2));
					}
				}
//				if (modelView.isEditable(triangle) && HitTestStuff.triHitTest(triangle, min, max, uvLayerIndex, viewBox)) {
//					newSelection.addAll(Arrays.asList(triangle.getVerts()));
//				}
			}
		}
		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, double vertexSize, int uvLayerIndex) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (modelView.isEditable(geosetVertex) && geosetVertex.getTverts().size() > uvLayerIndex) {
					if (HitTestStuff.hitTest(min, max, geosetVertex.getTVertex(uvLayerIndex), vertexSize)) {
						newSelection.add(geosetVertex);
					}
				}
			}
		}

		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, double vertexSize, int uvLayerIndex, Mat4 viewPortAntiRotMat) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		Vec2 tVertex = new Vec2();
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (modelView.isEditable(geosetVertex) && geosetVertex.getTverts().size() > uvLayerIndex) {
					tVertex.set(geosetVertex.getTVertex(uvLayerIndex)).transform(viewPortAntiRotMat);
					if (HitTestStuff.hitTest(min, max, tVertex, vertexSize)) {
						newSelection.add(geosetVertex);
					}
				}
			}
		}

		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, double vertexSize, int uvLayerIndex, SelectionBoxHelper viewBox) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		Vec3 vec3 = new Vec3();
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (modelView.isEditable(geosetVertex) && geosetVertex.getTverts().size() > uvLayerIndex && modelView.isEditable(geosetVertex)) {
					vec3.set(geosetVertex.getTVertex(uvLayerIndex), 0);
					if (viewBox.pointInBox(vec3)) {
						newSelection.add(geosetVertex);
					}
				}
			}
		}

		return newSelection;
	}

	public double getCircumscribedSphereRadius(Vec3 sphereCenter, int tvertexLayerId) {

		if (selectionMode == SelectionItemTypes.VERTEX || selectionMode == SelectionItemTypes.FACE || selectionMode == SelectionItemTypes.CLUSTER || selectionMode == SelectionItemTypes.GROUP) {
			Vec2 center = sphereCenter.getProjected(Vec3.X_AXIS, Vec3.Y_AXIS);
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

//		if (selectionMode == SelectionItemTypes.VERTEX || selectionMode == SelectionItemTypes.FACE || selectionMode == SelectionItemTypes.CLUSTER || selectionMode == SelectionItemTypes.GROUP) {
//			double radius = 0;
//			for (Vec3 item : modelView.getSelectedVertices()) {
//				double distance = sphereCenter.distance(item);
//				if (distance >= radius) {
//					radius = distance;
//				}
//			}
//			for (IdObject item : modelView.getSelectedIdObjects()) {
//				double distance = sphereCenter.distance(item.getPivotPoint());
//				if (distance >= radius) {
//					radius = distance;
//				}
//			}
//			for (CameraNode item : modelView.getSelectedCameraNodes()) {
//				double distance = sphereCenter.distance(item.getPosition());
//				if (distance >= radius) {
//					radius = distance;
//				}
//			}
////			for (Vec3 item : selection) {
////				double distance = sphereCenter.distance(item);
////				if (distance >= radius) {
////					radius = distance;
////				}
////			}
//
//			return radius;
//		}
//		return 0;
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
		int tvertexLayerId = 0;
		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.CLUSTER
				|| selectionMode == SelectionItemTypes.TPOSE) {

			return new Vec3().set(getUVCenter(tvertexLayerId), 0);
//			return new Vec3().setCoords((byte) 0, (byte) 1, modelView.getTSelectionCenter());
		}
//		if (selectionMode == SelectionItemTypes.ANIMATE) {
//			Vec3 centerOfGroupSumHeap = new Vec3(0, 0, 0);
//			Set<IdObject> selectedIdObjects = modelView.getSelectedIdObjects();
//			for (IdObject object : selectedIdObjects) {
//				Vec4 pivotHeap = new Vec4(object.getPivotPoint(), 1);
//				pivotHeap.transform(editorRenderModel.getRenderNode(object).getWorldMatrix());
//				centerOfGroupSumHeap.add(pivotHeap.getVec3());
//			}
//			if (selectedIdObjects.size() > 0) {
//				centerOfGroupSumHeap.scale(1f / selectedIdObjects.size());
//			}
//			return centerOfGroupSumHeap;
//		}

		return new Vec3();
	}

	public Vec2 getUVCenter(int tvertexLayerId) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			return Vec2.centerOfGroup(getSelectedTVertices(tvertexLayerId));
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Vec2 center = new Vec2();
			int vertCount = 0;
			for (Triangle triangle : modelView.getSelectedTriangles()) {
				for (GeosetVertex geosetVertex : triangle.getVerts()) {
					if (tvertexLayerId < geosetVertex.getTverts().size()) {
						center.add(geosetVertex.getTVertex(tvertexLayerId));
						vertCount++;
					}
				}
			}
			return center.scale(1f/vertCount);
			// Not sure if the average position of vertices or the average position of tris should be used...
//			Set<Vec2> selectedVertices = new HashSet<>();
//			for (Triangle triangle : modelView.getSelectedTriangles()) {
//				for (GeosetVertex geosetVertex : triangle.getVerts()) {
//					if (tvertexLayerId < geosetVertex.getTverts().size()) {
//						selectedVertices.add(geosetVertex.getTVertex(tvertexLayerId));
//					}
//				}
//			}
//			return Vec2.centerOfGroup(selectedVertices);
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

	public boolean selectableUnderCursor(Vec2 point, Mat4 viewPortAntiRotMat, double sizeAdj) {
		int tvertexLayerId = 0;
		if (selectionMode == SelectionItemTypes.VERTEX) {
			double vertexSize = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;
			for (Geoset geoset : modelView.getVisEdGeosets()) {
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (modelView.isEditable(geosetVertex) && geosetVertex.getTverts().size() > tvertexLayerId) {
						if (HitTestStuff.hitTest(point, geosetVertex.getTVertex(tvertexLayerId), vertexSize)) {
							return true;
						}
					}
				}
			}
		} else if (selectionMode == SelectionItemTypes.FACE) {
			for (Geoset geoset : modelView.getVisEdGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (modelView.isEditable(triangle) && HitTestStuff.triHitTest(triangle, point, tvertexLayerId)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
