package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.actions.SetSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoActionListener;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class FaceSelectingEventHandler extends AbstractSelectingEventHandler<Triangle> {

	private final ModelView model;

	public FaceSelectingEventHandler(final UndoActionListener undoManager,
			final SelectionManager<Triangle> selectionManager, final ModelView model) {
		super(undoManager, selectionManager);
		this.model = model;
	}

	@Override
	public void expandSelection() {
		final Set<Triangle> oldSelection = new HashSet<>(selectionManager.getSelection());
		final Set<Triangle> expandedSelection = new HashSet<>(selectionManager.getSelection());
		for (final Triangle triangle : new ArrayList<>(selectionManager.getSelection())) {
			expandSelection(triangle, expandedSelection);
		}
		selectionManager.addSelection(expandedSelection);
		undoManager.pushAction(
				new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection"));
	}

	private void expandSelection(final Triangle currentTriangle, final Set<Triangle> selection) {
		selection.add(currentTriangle);
		for (final GeosetVertex geosetVertex : currentTriangle.getVerts()) {
			for (final Triangle triangle : geosetVertex.getTriangles()) {
				if (!selection.contains(triangle)) {
					expandSelection(triangle, selection);
				}
			}
		}
	}

	@Override
	public void invertSelection() {
		final Set<Triangle> oldSelection = new HashSet<>(selectionManager.getSelection());
		final Set<Triangle> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangle()) {
				if (invertedSelection.contains(triangle)) {
					invertedSelection.remove(triangle);
				} else {
					invertedSelection.add(triangle);
				}
			}
		}
		selectionManager.setSelection(invertedSelection);
		undoManager.pushAction(
				new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection"));
	}

	@Override
	public void selectAll() {
		final Set<Triangle> oldSelection = new HashSet<>(selectionManager.getSelection());
		final Set<Triangle> allSelection = new HashSet<>();
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangle()) {
				allSelection.add(triangle);
			}
		}
		selectionManager.setSelection(allSelection);
		undoManager.pushAction(new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all"));
	}

	@Override
	protected List<Triangle> genericSelect(final Rectangle region, final CoordinateSystem coordinateSystem) {
		final List<Triangle> newSelection = new ArrayList<>();
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
			for (final Triangle triangle : geoset.getTriangle()) {
				if (hitTest(triangle, new Point2D.Double(area.getX(), area.getY()), coordinateSystem) || hitTest(
						triangle, new Point2D.Double(area.getX() + area.getWidth(), area.getY() + area.getHeight()),
						coordinateSystem) || hitTest(triangle, area, coordinateSystem)) {
					newSelection.add(triangle);
				}
			}
		}
		return newSelection;
	}

	public boolean hitTest(final Triangle triangle, final Point2D point, final CoordinateSystem coordinateSystem) {
		final GeosetVertex[] verts = triangle.getVerts();
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(verts[0].getCoord(coordinateSystem.getPortFirstXYZ()),
				verts[0].getCoord(coordinateSystem.getPortSecondXYZ()));
		for (int i = 1; i < verts.length; i++) {
			path.lineTo(verts[i].getCoord(coordinateSystem.getPortFirstXYZ()),
					verts[i].getCoord(coordinateSystem.getPortSecondXYZ()));
			// xpts[i] = (int)
			// (verts[i].getCoord(coordinateSystem.getPortFirstXYZ()));
			// ypts[i] = (int)
			// (verts[i].getCoord(coordinateSystem.getPortSecondXYZ()));
		} // TODO fix bad performance allocation
		path.closePath();
		return path.contains(point);
	}

	public boolean hitTest(final Triangle triangle, final Rectangle2D rectangle,
			final CoordinateSystem coordinateSystem) {
		final GeosetVertex[] verts = triangle.getVerts();
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(verts[0].getCoord(coordinateSystem.getPortFirstXYZ()),
				verts[0].getCoord(coordinateSystem.getPortSecondXYZ()));
		for (int i = 1; i < verts.length; i++) {
			path.lineTo(verts[i].getCoord(coordinateSystem.getPortFirstXYZ()),
					verts[i].getCoord(coordinateSystem.getPortSecondXYZ()));
		}
		return rectangle.contains(verts[0].getCoord(coordinateSystem.getPortFirstXYZ()),
				verts[0].getCoord(coordinateSystem.getPortSecondXYZ()))
				|| rectangle.contains(verts[1].getCoord(coordinateSystem.getPortFirstXYZ()),
						verts[1].getCoord(coordinateSystem.getPortSecondXYZ()))
				|| rectangle.contains(verts[2].getCoord(coordinateSystem.getPortFirstXYZ()),
						verts[2].getCoord(coordinateSystem.getPortSecondXYZ()))
				|| path.intersects(rectangle);
	}

}
