package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.actions.SetSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoManager;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class VertexSelectingEventListener extends AbstractSelectingEventListener<Vertex> {
	private final ModelView model;

	public VertexSelectingEventListener(final UndoManager undoManager, final SelectionManager<Vertex> selectionManager,
			final ModelView model) {
		super(undoManager, selectionManager);
		this.model = model;
	}

	@Override
	public void expandSelection() {
		final Set<Vertex> expandedSelection = new HashSet<>(selectionManager.getSelection());
		final ArrayList<Vertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		for (final Vertex v : oldSelection) {
			if (v instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) v;
				expandSelection(gv, expandedSelection);
			}
		}
		selectionManager.setSelection(expandedSelection);
		undoManager.pushAction(
				new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection"));
	}

	private void expandSelection(final GeosetVertex currentVertex, final Set<Vertex> selection) {
		selection.add(currentVertex);
		for (final Triangle tri : currentVertex.getTriangles()) {
			for (final GeosetVertex other : tri.getVerts()) {
				if (!selection.contains(other)) {
					expandSelection(other, selection);
				}
			}
		}
	}

	@Override
	public void invertSelection() {
		final ArrayList<Vertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<Vertex> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (final Geoset geo : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geo.getVertices()) {
				toggleSelection(invertedSelection, geosetVertex);
			}
		}
		for (final IdObject object : model.getEditableIdObjects()) {
			toggleSelection(invertedSelection, object.getPivotPoint());
		}
		for (final Camera object : model.getEditableCameras()) {
			toggleSelection(invertedSelection, object.getPosition());
			toggleSelection(invertedSelection, object.getTargetPosition());
		}
		selectionManager.setSelection(invertedSelection);
		undoManager.pushAction(
				new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection"));
	}

	private void toggleSelection(final Set<Vertex> selection, final Vertex position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public void selectAll() {
		final ArrayList<Vertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<Vertex> allSelection = new HashSet<>();
		for (final Geoset geo : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geo.getVertices()) {
				allSelection.add(geosetVertex);
			}
		}
		for (final IdObject object : model.getEditableIdObjects()) {
			allSelection.add(object.getPivotPoint());
		}
		for (final Camera object : model.getEditableCameras()) {
			allSelection.add(object.getPosition());
			allSelection.add(object.getTargetPosition());
		}
		selectionManager.setSelection(allSelection);
		undoManager.pushAction(new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all"));
	}

	@Override
	protected List<Vertex> genericSelect(final Rectangle region, final CoordinateSystem coordinateSystem) {
		final List<Vertex> selectedItems = new ArrayList<>();
		final double startingClickX = coordinateSystem.geomX(region.getX());
		final double startingClickY = coordinateSystem.geomY(region.getY());
		final double endingClickX = coordinateSystem.geomX(region.getX() + region.getWidth());
		final double endingClickY = coordinateSystem.geomY(region.getY() + region.getHeight());

		final double minX = Math.min(startingClickX, endingClickX);
		final double minY = Math.min(startingClickY, endingClickY);
		final double maxX = Math.max(startingClickX, endingClickX);
		final double maxY = Math.max(startingClickY, endingClickY);
		final Rectangle2D area = new Rectangle2D.Double(minX, minY, (maxX - minX), (maxY - minY));
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geoset.getVertices()) {
				hitTest(coordinateSystem, selectedItems, area, geosetVertex);
			}
		}
		for (final IdObject object : model.getEditableIdObjects()) {
			hitTest(coordinateSystem, selectedItems, area, object.getPivotPoint());
		}
		return selectedItems;
	}

	private static void hitTest(final CoordinateSystem coordinateSystem, final List<Vertex> selectedItems,
			final Rectangle2D area, final Vertex geosetVertex) {
		final double vertexX = geosetVertex.getCoord(coordinateSystem.getPortFirstXYZ());
		final double vertexY = geosetVertex.getCoord(coordinateSystem.getPortSecondXYZ());
		if (distance(vertexX, vertexY, area.getX(), area.getY()) <= 1.5f
				|| distance(vertexX, vertexY, area.getX() + area.getWidth(), area.getY() + area.getHeight()) <= 1.5f
				|| area.contains(vertexX, vertexY)) {
			selectedItems.add(geosetVertex);
		}
	}

	private static double distance(final double vertexX, final double vertexY, final double x, final double y) {
		final double dx = x - vertexX;
		final double dy = y - vertexY;
		return Math.sqrt(dx * dx + dy * dy);
	}
}
