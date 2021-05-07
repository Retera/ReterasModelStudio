package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class GeosetVertexTVertexEditor extends TVertexEditor<GeosetVertex> {

	public GeosetVertexTVertexEditor(ModelView model, SelectionManager<GeosetVertex> selectionManager, ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
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

	private static boolean within(Vec2 point, Vec2 min, Vec2 max){
		boolean xIn = max.x >= point.x && point.x >= min.x;
		boolean yIn = max.y >= point.y && point.y >= min.y;
		return xIn && yIn;
	}

	public static double distance(double vertexX, double vertexY, double x, double y) {
		double dx = x - vertexX;
		double dy = y - vertexY;
		return Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		List<GeosetVertex> newGeosetVertices = new ArrayList<>();
		for (Geoset geoset : model.getEditableGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				if (newSelection.contains(vertex)) {
					newGeosetVertices.add(vertex);
				}
			}
		}
		selectionManager.setSelection(newGeosetVertices);
	}

	@Override
	public UndoAction expandSelection() {
		Set<GeosetVertex> expandedSelection = new HashSet<>(selectionManager.getSelection());
		List<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		for (GeosetVertex v : oldSelection) {
			expandSelection(v, expandedSelection);
		}
		selectionManager.setSelection(expandedSelection);
		return new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection");
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

	@Override
	public UndoAction invertSelection() {
		List<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		Set<GeosetVertex> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (Geoset geo : model.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geo.getVertices()) {
				toggleSelection(invertedSelection, geosetVertex);
			}
		}
		selectionManager.setSelection(invertedSelection);
		return new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection");
	}

	private void toggleSelection(Set<GeosetVertex> selection, GeosetVertex position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public UndoAction selectAll() {
		List<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		Set<GeosetVertex> allSelection = new HashSet<>();
		for (Geoset geo : model.getEditableGeosets()) {
			allSelection.addAll(geo.getVertices());
		}
		selectionManager.setSelection(allSelection);
		return new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all");
	}

	@Override
	protected List<GeosetVertex> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<GeosetVertex> selectedItems = new ArrayList<>();
		for (Geoset geoset : model.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (geosetVertex.getTverts().size() > uvLayerIndex) {
					if(hitTest(min, max, geosetVertex.getTVertex(uvLayerIndex), coordinateSystem, ProgramGlobals.getPrefs().getVertexSize())){
						selectedItems.add(geosetVertex);
					}
				}
			}
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(Vec2 point, CoordinateSystem axes) {
		boolean canSelect = false;
		for (Geoset geoset : model.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (geosetVertex.getTverts().size() > uvLayerIndex) {
					if (hitTest(geosetVertex.getTVertex(uvLayerIndex), point, axes, ProgramGlobals.getPrefs().getVertexSize())) {
						canSelect = true;
					}
				}
			}
		}
		return canSelect;
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement<?>> selectableComponents,
	                                              EditabilityToggleHandler editabilityToggleHandler,
	                                              Runnable refreshGUIRunnable) {
		List<GeosetVertex> previousSelection = new ArrayList<>(selectionManager.getSelection());
		List<GeosetVertex> possibleVerticesToTruncate = new ArrayList<>();
		for (CheckableDisplayElement<?> component : selectableComponents) {
			Object item = component.getItem();
			if (item instanceof Geoset) {
				possibleVerticesToTruncate.addAll(((Geoset) item).getVertices());
			}
		}
		final Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(possibleVerticesToTruncate);

		final Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
