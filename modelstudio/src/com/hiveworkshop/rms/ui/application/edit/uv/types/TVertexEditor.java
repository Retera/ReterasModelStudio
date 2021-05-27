package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actions.uv.UVRemapAction;
import com.hiveworkshop.rms.ui.application.actions.uv.UVSnapAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.*;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ComponentVisibilityListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.MirrorTVerticesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.StaticMeshUVMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.StaticMeshUVRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.StaticMeshUVScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.TVertexSelectionItemTypes;
import com.hiveworkshop.rms.util.Vec2;

import java.util.*;

/**
 * So, in some ideal future this would be an implementation of the ModelEditor
 * interface, I believe, and the editor would be operating on an interface who
 * could capture clicks and convert them into 2D operations regardless of
 * whether the underlying thing being editor was UV or Mesh.
 *
 * It isn't like that right now, though, so this is just going to be a 2D copy pasta.
 */
public class TVertexEditor implements ComponentVisibilityListener {
	protected final ModelView modelView;
	protected final ModelStructureChangeListener structureChangeListener;
	protected int uvLayerIndex;
	protected TVertexSelectionItemTypes selectionType;

	public TVertexEditor(ModelView modelView, ModelStructureChangeListener structureChangeListener, TVertexSelectionItemTypes selectionTyp) {
		this.modelView = modelView;
		this.structureChangeListener = structureChangeListener;
		this.selectionType = selectionTyp;
	}

	public static boolean triHitTest(Triangle triangle, Vec2 point, CoordinateSystem coordinateSystem, int uvLayerIndex) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		GeosetVertex[] verts = triangle.getVerts();

		return pointInTriangle(point, verts[0].getTVertex(uvLayerIndex), verts[1].getTVertex(uvLayerIndex), verts[2].getTVertex(uvLayerIndex));
	}

	public static boolean triHitTest(Triangle triangle, Vec2 min, Vec2 max, CoordinateSystem coordinateSystem, int uvLayerIndex) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		GeosetVertex[] verts = triangle.getVerts();
//		Path2D.Double path = new Path2D.Double();
//		path.moveTo(verts[0].getTVertex(uvLayerIndex).getCoord(dim1), verts[0].getTVertex(uvLayerIndex).getCoord(dim2));
//		for (int i = 1; i < verts.length; i++) {
//			path.lineTo(verts[i].getTVertex(uvLayerIndex).getCoord(dim1), verts[i].getTVertex(uvLayerIndex).getCoord(dim2));

		System.out.println("min: " + min + ", max: " + max + ", tVertex1: " + verts[0].getTVertex(uvLayerIndex));
//		}
		return within(verts[0].getTVertex(uvLayerIndex), min, max)
				|| within(verts[1].getTVertex(uvLayerIndex), min, max)
				|| within(verts[2].getTVertex(uvLayerIndex), min, max);
	}

	private static boolean within(Vec2 point, Vec2 min, Vec2 max){
		boolean xIn = max.x >= point.x && point.x >= min.x;
		boolean yIn = max.y >= point.y && point.y >= min.y;
		return xIn && yIn;
	}

	private static boolean pointInTriangle (Vec2 point, Vec2 v1, Vec2 v2, Vec2 v3)
	{
		float d1 = (point.x - v2.x) * (v1.y - v2.y) - (v1.x - v2.x) * (point.y - v2.y);
		float d2 = (point.x - v3.x) * (v2.y - v3.y) - (v2.x - v3.x) * (point.y - v3.y);
		float d3 = (point.x - v1.x) * (v3.y - v1.y) - (v3.x - v1.x) * (point.y - v1.y);;
//        float d1 = sign(point, v1, v2);
//        float d2 = sign(point, v2, v3);
//        float d3 = sign(point, v3, v1);

		boolean has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
		boolean has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

		return !(has_neg && has_pos);
	}

	public static boolean hitTest(Vec2 min, Vec2 max, Vec2 tVertex, CoordinateSystem coordinateSystem, double vertexSize) {
		double vSizeView = vertexSize / coordinateSystem.getZoom();
//		System.out.println("min: " + min + ", max: " + max + ", tVertex: " + tVertex + ", vSizeView: " + vSizeView);
		return tVertex.distance(min) <= vSizeView
				|| tVertex.distance(max) <= vSizeView
				|| within(tVertex, min, max);
	}

	public static boolean hitTest(Vec2 vertex, Vec2 point, CoordinateSystem coordinateSystem, double vertexSize) {
		double vSizeView = vertexSize / coordinateSystem.getZoom();
		return vertex.distance(point) <= vSizeView / 2.0;
	}

	public static double distance(double vertexX, double vertexY, double x, double y) {
		double dx = x - vertexX;
		double dy = y - vertexY;
		return Math.sqrt(dx * dx + dy * dy);
	}


	public UndoAction expandSelection() {
		Set<GeosetVertex> expandedSelection = new HashSet<>(modelView.getSelectedVertices());
		Set<GeosetVertex> oldSelection = new HashSet<>(modelView.getSelectedVertices());
		for (GeosetVertex v : oldSelection) {
			expandSelection(v, expandedSelection);
		}

		SetSelectionAction setSelectionAction = new SetSelectionAction(expandedSelection, modelView, "expand selection");
		setSelectionAction.redo();
		return setSelectionAction;
	}

	private void expandSelection(GeosetVertex currentVertex, Set<GeosetVertex> selection) {
		selection.add(currentVertex);
		for (Triangle tri : currentVertex.getTriangles()) {
			for (GeosetVertex other : tri.getVerts()) {
				if (!selection.contains(other)) {
					expandSelection(other, selection);
				}
			}
		}
	}


	public UndoAction invertSelection() {
		Set<GeosetVertex> invertedSelection = new HashSet<>();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			invertedSelection.addAll(geoset.getVertices());
		}
		invertedSelection.removeAll(modelView.getSelectedVertices());

		SetSelectionAction setSelectionAction = new SetSelectionAction(invertedSelection, modelView, "invert selection");
		setSelectionAction.redo();
		return setSelectionAction;
	}

	public UndoAction selectAll() {
		Set<GeosetVertex> allSelection = new HashSet<>();
		for (Geoset geo : modelView.getEditableGeosets()) {
			allSelection.addAll(geo.getVertices());
		}

		SetSelectionAction setSelectionAction = new SetSelectionAction(allSelection, modelView, "select all");
		setSelectionAction.redo();
		return setSelectionAction;
	}

	public boolean canSelectAt(Vec2 point, CoordinateSystem axes) {
		if(selectionType == TVertexSelectionItemTypes.VERTEX){
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (geosetVertex.getTverts().size() > uvLayerIndex) {
						if (hitTest(geosetVertex.getTVertex(uvLayerIndex), point, axes, ProgramGlobals.getPrefs().getVertexSize())) {
							return true;
						}
					}
				}
			}
		} else if(selectionType == TVertexSelectionItemTypes.FACE){
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (triHitTest(triangle, point, axes, uvLayerIndex)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public final UndoAction addSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<GeosetVertex> newSelection = genericSelect(min, max, coordinateSystem);
		final AddSelectionAction tAddSelectionAction = new AddSelectionAction(newSelection, modelView);
		tAddSelectionAction.redo();
		return tAddSelectionAction;
	}

	public final UndoAction setSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<GeosetVertex> newSelection = genericSelect(min, max, coordinateSystem);
		final SetSelectionAction select = new SetSelectionAction(newSelection, modelView, "select");
		select.redo();
		return select;
	}

	public final UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<GeosetVertex> newSelection = genericSelect(min, max, coordinateSystem);
		final RemoveSelectionAction tRemoveSelectionAction = new RemoveSelectionAction(newSelection, modelView);
		tRemoveSelectionAction.redo();
		return tRemoveSelectionAction;
	}

	protected List<GeosetVertex> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<GeosetVertex> selectedVerts = new ArrayList<>();
		if(selectionType == TVertexSelectionItemTypes.FACE){
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (Triangle triangle : geoset.getTriangles()) {
					if (triHitTest(triangle, min, coordinateSystem, uvLayerIndex)
							|| triHitTest(triangle, max, coordinateSystem, uvLayerIndex)
							|| triHitTest(triangle, min, max, coordinateSystem, uvLayerIndex)) {
						selectedVerts.addAll(Arrays.asList(triangle.getVerts()));
					}
				}
			}
		}

		if(selectionType == TVertexSelectionItemTypes.VERTEX){
			for (Geoset geoset : modelView.getEditableGeosets()) {
				for (GeosetVertex geosetVertex : geoset.getVertices()) {
					if (geosetVertex.getTverts().size() > uvLayerIndex) {
						if(hitTest(min, max, geosetVertex.getTVertex(uvLayerIndex), coordinateSystem, ProgramGlobals.getPrefs().getVertexSize())){
							selectedVerts.add(geosetVertex);
						}
					}
				}
			}
		}
		return selectedVerts;
	}

	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement<?>> selectableComponents,
	                                              EditabilityToggleHandler editabilityToggleHandler,
	                                              Runnable refreshGUIRunnable) {
		List<GeosetVertex> previousSelection = new ArrayList<>(modelView.getSelectedVertices());
		List<GeosetVertex> possibleVerticesToTruncate = new ArrayList<>();
		for (CheckableDisplayElement<?> component : selectableComponents) {
			Object item = component.getItem();
			if (item instanceof Geoset) {
				possibleVerticesToTruncate.addAll(((Geoset) item).getVertices());
			}
		}
		final Runnable truncateSelectionRunnable = () -> modelView.removeSelectedVertices(possibleVerticesToTruncate);

		final Runnable unTruncateSelectionRunnable = () -> modelView.setSelectedVertices(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}
	@Override
	public UndoAction hideComponent(List<? extends CheckableDisplayElement<?>> selectableComponent, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		UndoAction hideComponentAction = buildHideComponentAction(selectableComponent, editabilityToggleHandler, refreshGUIRunnable);
		hideComponentAction.redo();
		return hideComponentAction;
	}

	@Override
	public UndoAction showComponent(EditabilityToggleHandler editabilityToggleHandler) {
		editabilityToggleHandler.makeEditable();
		return new MakeEditableAction(editabilityToggleHandler);
	}

	public UndoAction mirror(byte dim, double centerX, double centerY) {
		MirrorTVerticesAction mirror = new MirrorTVerticesAction(TVertexUtils.getTVertices(modelView.getSelectedVertices(), uvLayerIndex), dim, centerX, centerY);
		// super weird passing of currently editable id Objects, works because mirror action checks selected vertices against pivot points from this list
		mirror.redo();
		return mirror;
	}

	public UndoAction remap(byte xDim, byte yDim, UVPanel.UnwrapDirection unwrapDirection) {
		UVRemapAction uvRemapAction = new UVRemapAction(modelView.getSelectedVertices(), uvLayerIndex, xDim, yDim, unwrapDirection);
		uvRemapAction.redo();
		return uvRemapAction;
	}

	public UndoAction snapSelectedVertices() {
		Collection<? extends Vec2> selection = TVertexUtils.getTVertices(modelView.getSelectedVertices(), uvLayerIndex);
		List<Vec2> oldLocations = new ArrayList<>();
		Vec2 cog = Vec2.centerOfGroup(selection);
		for (Vec2 vertex : selection) {
			oldLocations.add(new Vec2(vertex));
		}
		UVSnapAction temp = new UVSnapAction(selection, oldLocations, cog);
		temp.redo();
		return temp;
	}

	public Vec2 getSelectionCenter() {
//		return selectionManager.getCenter();
		Set<Vec2> tvertices = new HashSet<>(TVertexUtils.getTVertices(modelView.getSelectedVertices(), uvLayerIndex));
		return Vec2.centerOfGroup(tvertices); // TODO is this correct?
	}

	public UndoAction selectFromViewer(SelectionView viewerSelectionView) {
		SetSelectionAction setSelectionAction = new SetSelectionAction(modelView.getSelectedVertices(), modelView, "");
		setSelectionAction.redo();
		return setSelectionAction;
	}

	public GenericMoveAction beginTranslation() {
		return new StaticMeshUVMoveAction(modelView.getSelectedVertices(), uvLayerIndex, Vec2.ORIGIN);
	}

	public GenericRotateAction beginRotation(Vec2 center, byte dim1, byte dim2) {
		return new StaticMeshUVRotateAction(modelView.getSelectedVertices(), uvLayerIndex, center, dim1, dim2);
	}

	public GenericScaleAction beginScaling(Vec2 center) {
		return new StaticMeshUVScaleAction(modelView.getSelectedVertices(), uvLayerIndex, center);
	}

	public int getUVLayerIndex() {
		return uvLayerIndex;
	}

	public void setUVLayerIndex(int uvLayerIndex) {
		this.uvLayerIndex = uvLayerIndex;
		// TODO deselect vertices with no such layer
	}

}
