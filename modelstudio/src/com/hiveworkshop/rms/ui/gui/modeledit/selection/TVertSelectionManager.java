package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.AddSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.RemoveSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Mat4;
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
	//	protected final Set<T> selection = new HashSet<>();
	private boolean moveLinked;

	private VertexClusterDefinitions vertexClusterDefinitions;

	public TVertSelectionManager(ModelView modelView, SelectionItemTypes selectionMode) {
		super(modelView, selectionMode);
	}


	public UndoAction setSelectedRegion(Vec2 min, Vec2 max, Mat4 viewPortMat, double zoom) {
		return null;
	}

	;

	public UndoAction removeSelectedRegion(Vec2 min, Vec2 max, Mat4 viewPortMat, double zoom) {
		return null;
	}

	;

	public UndoAction addSelectedRegion(Vec2 min, Vec2 max, Mat4 viewPortMat, double zoom) {
		return null;
	}

	;

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
			Set<GeosetVertex> selectedItems = addVertsFromArea(min, max, coordinateSystem, 0);
			return new SelectoinUgg(selectedItems);
		}

		if (selectionMode == SelectionItemTypes.FACE) {
			Set<GeosetVertex> newSel = addTrisFromArea(min, max, coordinateSystem, 0);
			return new SelectoinUgg(newSel);
		}
		return new SelectoinUgg(Collections.emptySet());
	}

	private Set<GeosetVertex> addTrisFromArea(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem, int uvLayerIndex) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (HitTestStuff.triHitTest(triangle, min, coordinateSystem, uvLayerIndex)
						|| HitTestStuff.triHitTest(triangle, max, coordinateSystem, uvLayerIndex)
						|| HitTestStuff.triHitTest(triangle, min, max, coordinateSystem, uvLayerIndex)) {
					newSelection.addAll(Arrays.asList(triangle.getVerts()));
				}
			}
		}
		return newSelection;
	}

	public Set<GeosetVertex> addVertsFromArea(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem, int uvLayerIndex) {
		Set<GeosetVertex> newSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (geosetVertex.getTverts().size() > uvLayerIndex) {
					if(HitTestStuff.hitTest(min, max, geosetVertex.getTVertex(uvLayerIndex), coordinateSystem, ProgramGlobals.getPrefs().getVertexSize())){
						newSelection.add(geosetVertex);
					}
				}
			}
		}

		return newSelection;
	}

	public final UndoAction setSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		SelectoinUgg newSelection = genericSelect(min, max, coordinateSystem);
		return new SetSelectionUggAction(newSelection, modelView, "select").redo();
	}

	public final UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		SelectoinUgg newSelection = genericSelect(min, max, coordinateSystem);
		return new RemoveSelectionUggAction(newSelection, modelView).redo();
	}

	public final UndoAction addSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		SelectoinUgg newSelection = genericSelect(min, max, coordinateSystem);
		return new AddSelectionUggAction(newSelection, modelView).redo();
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

	public boolean selectableUnderCursor(Vec2 point, CoordinateSystem axes) {
		if (selectionMode == SelectionItemTypes.VERTEX) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (geosetVertex.getTverts().size() > 0) {
						if (HitTestStuff.hitTest(geosetVertex.getTVertex(0), point, axes, ProgramGlobals.getPrefs().getVertexSize())) {
							return true;
						}
					}
				}
			}
		} else if (selectionMode == SelectionItemTypes.FACE) {
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (HitTestStuff.triHitTest(triangle, point, axes, 0)) {
						return true;
					}
				}
			}
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
