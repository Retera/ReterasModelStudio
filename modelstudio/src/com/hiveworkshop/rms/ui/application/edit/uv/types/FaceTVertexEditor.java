package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class FaceTVertexEditor extends TVertexEditor<Triangle> {
	private final ProgramPreferences programPreferences;

	public FaceTVertexEditor(ModelView model, ProgramPreferences programPreferences, SelectionManager<Triangle> selectionManager, ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
	}

	public static boolean hitTest(Triangle triangle, Vec2 point, CoordinateSystem coordinateSystem, int uvLayerIndex) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		GeosetVertex[] verts = triangle.getVerts();

		return pointInTriangle(point, verts[0].getTVertex(uvLayerIndex), verts[1].getTVertex(uvLayerIndex), verts[2].getTVertex(uvLayerIndex));
	}

	public static boolean hitTest(Triangle triangle, Vec2 min, Vec2 max, CoordinateSystem coordinateSystem, int uvLayerIndex) {
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

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		Set<Triangle> newlySelectedFaces = new HashSet<>();
		for (Geoset geoset : model.getModel().getGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				boolean allInSelection = true;
				for (GeosetVertex vertex : triangle.getVerts()) {
					if (!newSelection.contains(vertex)) {
						allInSelection = false;
						break;
					}
				}
				if (allInSelection) {
					newlySelectedFaces.add(triangle);
				}
			}
		}
		selectionManager.setSelection(newlySelectedFaces);
	}

	@Override
	public UndoAction expandSelection() {
		Set<Triangle> oldSelection = new HashSet<>(selectionManager.getSelection());
		Set<Triangle> expandedSelection = new HashSet<>(selectionManager.getSelection());
		for (Triangle triangle : new ArrayList<>(selectionManager.getSelection())) {
			expandSelection(triangle, expandedSelection);
		}
		selectionManager.addSelection(expandedSelection);
		return new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection");
	}

	private void expandSelection(Triangle currentTriangle, Set<Triangle> selection) {
		selection.add(currentTriangle);
		for (GeosetVertex geosetVertex : currentTriangle.getVerts()) {
			for (Triangle triangle : geosetVertex.getTriangles()) {
				if (!selection.contains(triangle)) {
					expandSelection(triangle, selection);
				}
			}
		}
	}

	@Override
	public UndoAction invertSelection() {
		Set<Triangle> oldSelection = new HashSet<>(selectionManager.getSelection());
		Set<Triangle> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (Geoset geoset : model.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (invertedSelection.contains(triangle)) {
					invertedSelection.remove(triangle);
				} else {
					invertedSelection.add(triangle);
				}
			}
		}
		selectionManager.setSelection(invertedSelection);
		return new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection");
	}

	@Override
	public UndoAction selectAll() {
		Set<Triangle> oldSelection = new HashSet<>(selectionManager.getSelection());
		Set<Triangle> allSelection = new HashSet<>();
		for (Geoset geoset : model.getEditableGeosets()) {
			allSelection.addAll(geoset.getTriangles());
		}
		selectionManager.setSelection(allSelection);
		return new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all");
	}

	@Override
	protected List<Triangle> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<Triangle> newSelection = new ArrayList<>();

		for (Geoset geoset : model.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (hitTest(triangle, min, coordinateSystem, uvLayerIndex)
						|| hitTest(triangle, max, coordinateSystem, uvLayerIndex)
						|| hitTest(triangle, min, max, coordinateSystem, uvLayerIndex)) {
					newSelection.add(triangle);
				}
			}
		}
		return newSelection;
	}

	@Override
	public boolean canSelectAt(Vec2 point, CoordinateSystem axes) {
		boolean canSelect = false;
		for (Geoset geoset : model.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (hitTest(triangle, point, axes, uvLayerIndex)) {
					canSelect = true;
				}
			}
		}
		return canSelect;
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement<?>> selectableComponents,
	                                              EditabilityToggleHandler editabilityToggleHandler,
	                                              Runnable refreshGUIRunnable) {
		List<Triangle> previousSelection = new ArrayList<>(selectionManager.getSelection());
		List<Triangle> possibleTrianglesToTruncate = new ArrayList<>();
		List<Vec3> possibleVerticesToTruncate = new ArrayList<>();
		for (CheckableDisplayElement<?> component : selectableComponents) {
			Object item = component.getItem();
			if (item instanceof Camera) {
				possibleVerticesToTruncate.add(((Camera) item).getPosition());
				possibleVerticesToTruncate.add(((Camera) item).getTargetPosition());
			} else if (item instanceof IdObject) {
				possibleVerticesToTruncate.add(((IdObject) item).getPivotPoint());
			} else if (item instanceof Geoset) {
				possibleTrianglesToTruncate.addAll(((Geoset) item).getTriangles());
			}
		}
		Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(possibleTrianglesToTruncate);
		Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
