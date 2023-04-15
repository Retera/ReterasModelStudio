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

	public TVertSelectionManager(RenderModel editorRenderModel, ModelView modelView, SelectionItemTypes selectionMode) {
		super(editorRenderModel, modelView, selectionMode);
	}

	public SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, Mat4 viewPortAntiRotMat, double sizeAdj) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
//			System.out.println("Tvert genericSelect, " + "min: " + min + ", max" + max);
			Set<GeosetVertex> selectedItems = addVertsFromArea(min, max, viewPortAntiRotMat, sizeAdj);
			return new SelectionBundle(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> newSel = addTrisFromArea(min, max, viewPortAntiRotMat);
			return new SelectionBundle(newSel);
		}
		return new SelectionBundle(Collections.emptySet());
	}

	public SelectionBundle getSelectionBundle(Vec2 min, Vec2 max, SelectionBoxHelper viewBox, double sizeAdj) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
//			System.out.println("Tvert genericSelect, " + "min: " + min + ", max" + max);
			Set<GeosetVertex> selectedItems = addVertsFromArea(min, max, viewBox, sizeAdj);
			return new SelectionBundle(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> newSel = addTrisFromArea(min, max, viewBox, sizeAdj);
			return new SelectionBundle(newSel);
		}
		return new SelectionBundle(Collections.emptySet());
	}

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, Mat4 viewPortAntiRotMat) {
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

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, SelectionBoxHelper viewBox, double sizeAdj) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		Vec3 v0 = new Vec3();
		Vec3 v1 = new Vec3();
		Vec3 v2 = new Vec3();
		float vertSize = (float) (sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0);
		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (modelView.isEditable(triangle)){
					v0.set(triangle.get(0).getTVertex(uvLayerIndex), 0);
					v1.set(triangle.get(1).getTVertex(uvLayerIndex), 0);
					v2.set(triangle.get(2).getTVertex(uvLayerIndex), 0);
					if(viewBox.anyPointInBox(vertSize, v0, v1, v2) || viewBox.triIntersectBox(v0, v1, v2)){
						newSelection.add(triangle.get(0));
						newSelection.add(triangle.get(1));
						newSelection.add(triangle.get(2));
					}
				}
			}
		}
		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, Mat4 viewPortAntiRotMat, double sizeAdj) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		Vec2 tVertex = new Vec2();

		double vertexSize = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;

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

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, SelectionBoxHelper viewBox, double sizeAdj) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		Vec3 vec3 = new Vec3();

		float vertexSize = (float) (sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0);

		for (Geoset geoset : modelView.getVisEdGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (modelView.isEditable(geosetVertex) && geosetVertex.getTverts().size() > uvLayerIndex && modelView.isEditable(geosetVertex)) {
					vec3.set(geosetVertex.getTVertex(uvLayerIndex), 0);
					if (viewBox.pointInBox(vec3, vertexSize)) {
						newSelection.add(geosetVertex);
					}
				}
			}
		}

		return newSelection;
	}

	public double getCircumscribedSphereRadius(Vec3 sphereCenter) {

		if (selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.CLUSTER
				|| selectionMode == SelectionItemTypes.GROUP) {
			Vec2 center = sphereCenter.getProjected(Vec3.X_AXIS, Vec3.Y_AXIS);
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
				|| selectionMode == SelectionItemTypes.CLUSTER
				|| selectionMode == SelectionItemTypes.TPOSE) {

			return new Vec3().set(getUVCenter(), 0);
//			return new Vec3().setCoords((byte) 0, (byte) 1, modelView.getTSelectionCenter());
		}

		return new Vec3();
	}

	public Vec2 getUVCenter() {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			return Vec2.centerOfGroup(getSelectedTVertices());
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Vec2 center = new Vec2();
			int vertCount = 0;
			for (Triangle triangle : modelView.getSelectedTriangles()) {
				for (GeosetVertex geosetVertex : triangle.getVerts()) {
					if (uvLayerIndex < geosetVertex.getTverts().size()) {
						center.add(geosetVertex.getTVertex(uvLayerIndex));
						vertCount++;
					}
				}
			}
			return center.scale(1f/vertCount);
			// Not sure if the average position of vertices or the average position of tris should be used...
//			Set<Vec2> selectedVertices = new HashSet<>();
//			for (Triangle triangle : modelView.getSelectedTriangles()) {
//				for (GeosetVertex geosetVertex : triangle.getVerts()) {
//					if (uvLayerIndex < geosetVertex.getTverts().size()) {
//						selectedVertices.add(geosetVertex.getTVertex(uvLayerIndex));
//					}
//				}
//			}
//			return Vec2.centerOfGroup(selectedVertices);
		}
		return Vec2.ORIGIN;
	}

	public Collection<Vec2> getSelectedTVertices() {
		if (selectionMode == SelectionItemTypes.VERTEX || selectionMode == SelectionItemTypes.FACE || selectionMode == SelectionItemTypes.CLUSTER || selectionMode == SelectionItemTypes.GROUP) {
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
		Vec2 tVertex = new Vec2();

		double vertexSize = sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0;

		if (selectionMode == SelectionItemTypes.VERTEX) {
			for (Geoset geoset : modelView.getVisEdGeosets()) {
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (modelView.isEditable(geosetVertex) && geosetVertex.getTverts().size() > uvLayerIndex) {
						tVertex.set(geosetVertex.getTVertex(uvLayerIndex)).transform(viewPortAntiRotMat);
						if (HitTestStuff.hitTest(point, tVertex, vertexSize)) {
							return true;
						}
					}
				}
			}
		} else if (selectionMode == SelectionItemTypes.FACE) {
			Vec2[] triPoints = new Vec2[] {new Vec2(), new Vec2(), new Vec2()};
			for (Geoset geoset : modelView.getVisEdGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (modelView.isEditable(triangle)) {
						triPoints[0].setAsProjection(triangle.get(0), viewPortAntiRotMat);
						triPoints[1].setAsProjection(triangle.get(1), viewPortAntiRotMat);
						triPoints[2].setAsProjection(triangle.get(2), viewPortAntiRotMat);
						if(HitTestStuff.pointInTriangle(point, triPoints[0], triPoints[1], triPoints[2])){
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean selectableUnderCursor(Vec2 point, SelectionBoxHelper viewBox, double sizeAdj) {
		Vec3 vec3 = new Vec3();

		float vertexSize = (float) (sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0);

		if (selectionMode == SelectionItemTypes.VERTEX) {
			for (Geoset geoset : modelView.getVisEdGeosets()) {
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (modelView.isEditable(geosetVertex) && geosetVertex.getTverts().size() > uvLayerIndex && modelView.isEditable(geosetVertex)) {
						vec3.set(geosetVertex.getTVertex(uvLayerIndex), 0);
						if (viewBox.pointInBox(vec3, vertexSize)) {
							return true;
						}
					}
				}
			}
		} else if (selectionMode == SelectionItemTypes.FACE) {
			Vec3 v0 = new Vec3();
			Vec3 v1 = new Vec3();
			Vec3 v2 = new Vec3();
			float vertSize = (float) (sizeAdj * ProgramGlobals.getPrefs().getVertexSize() / 2.0);
			for (Geoset geoset : modelView.getVisEdGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (modelView.isEditable(triangle)){
						v0.set(triangle.get(0).getTVertex(uvLayerIndex), 0);
						v1.set(triangle.get(1).getTVertex(uvLayerIndex), 0);
						v2.set(triangle.get(2).getTVertex(uvLayerIndex), 0);
						if(viewBox.anyPointInBox(vertSize, v0, v1, v2) || viewBox.triIntersectBox(v0, v1, v2)){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
