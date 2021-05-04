package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
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

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class GeosetVertexTVertexEditor extends TVertexEditor<GeosetVertex> {
	private final ProgramPreferences programPreferences;

	public GeosetVertexTVertexEditor(ModelView model, ProgramPreferences programPreferences, SelectionManager<GeosetVertex> selectionManager, ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
	}

	public static void hitTest(List<GeosetVertex> selectedItems, Rectangle2D area, GeosetVertex geosetVertex, Vec2 tVertex, CoordinateSystem coordinateSystem, double vertexSize) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		double minX = coordinateSystem.viewX(area.getMinX());
		double minY = coordinateSystem.viewY(area.getMinY());
		double maxX = coordinateSystem.viewX(area.getMaxX());
		double maxY = coordinateSystem.viewY(area.getMaxY());
		double vertexX = tVertex.getCoord(dim1);
		double x = coordinateSystem.viewX(vertexX);
		double vertexY = tVertex.getCoord(dim2);
		double y = coordinateSystem.viewY(vertexY);
		if (distance(x, y, minX, minY) <= vertexSize / 2.0
				|| distance(x, y, maxX, maxY) <= vertexSize / 2.0
				|| area.contains(vertexX, vertexY)) {
			selectedItems.add(geosetVertex);
		}
	}

	public static boolean hitTest(Vec2 vertex, Point2D point, CoordinateSystem coordinateSystem, double vertexSize) {
		double x = coordinateSystem.viewX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
		double y = coordinateSystem.viewY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
		double px = coordinateSystem.viewX(point.getX());
		double py = coordinateSystem.viewY(point.getY());
		return Point2D.distance(px, py, x, y) <= vertexSize / 2.0;
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
	protected List<GeosetVertex> genericSelect(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<GeosetVertex> selectedItems = new ArrayList<>();
		double startingClickX = region.getX();
		double startingClickY = region.getY();
		double endingClickX = region.getX() + region.getWidth();
		double endingClickY = region.getY() + region.getHeight();

		double minX = Math.min(startingClickX, endingClickX);
		double minY = Math.min(startingClickY, endingClickY);
		double maxX = Math.max(startingClickX, endingClickX);
		double maxY = Math.max(startingClickY, endingClickY);
		Rectangle2D area = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
		for (Geoset geoset : model.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (geosetVertex.getTverts().size() > uvLayerIndex) {
					hitTest(selectedItems, area, geosetVertex, geosetVertex.getTVertex(uvLayerIndex), coordinateSystem, programPreferences.getVertexSize());
				}
			}
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(Point point, CoordinateSystem axes) {
		boolean canSelect = false;
		for (Geoset geoset : model.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (geosetVertex.getTverts().size() > uvLayerIndex) {
					if (hitTest(geosetVertex.getTVertex(uvLayerIndex), CoordSysUtils.geom(axes, point), axes, programPreferences.getVertexSize())) {
						canSelect = true;
					}
				}
			}
		}
		return canSelect;
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement> selectableComponents,
	                                              EditabilityToggleHandler editabilityToggleHandler,
	                                              Runnable refreshGUIRunnable) {
		List<GeosetVertex> previousSelection = new ArrayList<>(selectionManager.getSelection());
		List<GeosetVertex> possibleVerticesToTruncate = new ArrayList<>();
		for (CheckableDisplayElement component : selectableComponents) {
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
