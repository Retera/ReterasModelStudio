package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class FaceTVertexEditor extends AbstractTVertexEditor<Triangle> {
	private final ProgramPreferences programPreferences;

	public FaceTVertexEditor(final ModelView model, final ProgramPreferences programPreferences, final SelectionManager<Triangle> selectionManager, final ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
	}

	public static boolean hitTest(final Triangle triangle, final Point2D point, final CoordinateSystem coordinateSystem, final int uvLayerIndex) {
		final byte dim1 = coordinateSystem.getPortFirstXYZ();
		final byte dim2 = coordinateSystem.getPortSecondXYZ();
		final GeosetVertex[] verts = triangle.getVerts();
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(verts[0].getTVertex(uvLayerIndex).getCoord(dim1), verts[0].getTVertex(uvLayerIndex).getCoord(dim2));
		for (int i = 1; i < verts.length; i++) {
			path.lineTo(verts[i].getTVertex(uvLayerIndex).getCoord(dim1), verts[i].getTVertex(uvLayerIndex).getCoord(dim2));
			// xpts[i] = (int)
			// (verts[i].getCoord(dim1));
			// ypts[i] = (int)
			// (verts[i].getCoord(dim2));
		} // TODO fix bad performance allocation
		path.closePath();
		return path.contains(point);
	}

	public static boolean hitTest(final Triangle triangle, final Rectangle2D rectangle, final CoordinateSystem coordinateSystem, final int uvLayerIndex) {
		final byte dim1 = coordinateSystem.getPortFirstXYZ();
		final byte dim2 = coordinateSystem.getPortSecondXYZ();
		final GeosetVertex[] verts = triangle.getVerts();
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(verts[0].getTVertex(uvLayerIndex).getCoord(dim1), verts[0].getTVertex(uvLayerIndex).getCoord(dim2));
		for (int i = 1; i < verts.length; i++) {
			path.lineTo(verts[i].getTVertex(uvLayerIndex).getCoord(dim1), verts[i].getTVertex(uvLayerIndex).getCoord(dim2));
		}
		return rectangle.contains(verts[0].getTVertex(uvLayerIndex).getCoord(dim1), verts[0].getTVertex(uvLayerIndex).getCoord(dim2))
				|| rectangle.contains(verts[1].getTVertex(uvLayerIndex).getCoord(dim1), verts[1].getTVertex(uvLayerIndex).getCoord(dim2))
				|| rectangle.contains(verts[2].getTVertex(uvLayerIndex).getCoord(dim1), verts[2].getTVertex(uvLayerIndex).getCoord(dim2))
				|| path.intersects(rectangle);
	}

	@Override
	public void selectByVertices(final Collection<? extends Vec3> newSelection) {
		final Set<Triangle> newlySelectedFaces = new HashSet<>();
		for (final Geoset geoset : model.getModel().getGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				boolean allInSelection = true;
				for (final GeosetVertex vertex : triangle.getVerts()) {
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
		final Set<Triangle> oldSelection = new HashSet<>(selectionManager.getSelection());
		final Set<Triangle> expandedSelection = new HashSet<>(selectionManager.getSelection());
		for (final Triangle triangle : new ArrayList<>(selectionManager.getSelection())) {
			expandSelection(triangle, expandedSelection);
		}
		selectionManager.addSelection(expandedSelection);
		return new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection");
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
	public UndoAction invertSelection() {
		final Set<Triangle> oldSelection = new HashSet<>(selectionManager.getSelection());
		final Set<Triangle> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
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
		final Set<Triangle> oldSelection = new HashSet<>(selectionManager.getSelection());
		final Set<Triangle> allSelection = new HashSet<>();
		for (final Geoset geoset : model.getEditableGeosets()) {
			allSelection.addAll(geoset.getTriangles());
		}
		selectionManager.setSelection(allSelection);
		return new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all");
	}

	@Override
	protected List<Triangle> genericSelect(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<Triangle> newSelection = new ArrayList<>();
		final double startingClickX = region.getX();
		final double startingClickY = region.getY();
		final double endingClickX = region.getX() + region.getWidth();
		final double endingClickY = region.getY() + region.getHeight();

		final double minX = Math.min(startingClickX, endingClickX);
		final double minY = Math.min(startingClickY, endingClickY);
		final double maxX = Math.max(startingClickX, endingClickX);
		final double maxY = Math.max(startingClickY, endingClickY);
		final Rectangle2D area = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				if (hitTest(triangle, new Point2D.Double(area.getX(), area.getY()), coordinateSystem, uvLayerIndex)
						|| hitTest(triangle, new Point2D.Double(area.getX() + area.getWidth(), area.getY() + area.getHeight()), coordinateSystem, uvLayerIndex)
						|| hitTest(triangle, area, coordinateSystem, uvLayerIndex)) {
					newSelection.add(triangle);
				}
			}
		}
		return newSelection;
	}

	@Override
	public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
		boolean canSelect = false;
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				if (hitTest(triangle, CoordinateSystem.Util.geom(axes, point), axes, uvLayerIndex)) {
					canSelect = true;
				}
			}
		}
		return canSelect;
	}

	@Override
	protected UndoAction buildHideComponentAction(final List<? extends SelectableComponent> selectableComponents,
                                                  final EditabilityToggleHandler editabilityToggleHandler,
                                                  final Runnable refreshGUIRunnable) {
		final List<Triangle> previousSelection = new ArrayList<>(selectionManager.getSelection());
		final List<Triangle> possibleTrianglesToTruncate = new ArrayList<>();
		final List<Vec3> possibleVerticesToTruncate = new ArrayList<>();
		for (final SelectableComponent component : selectableComponents) {
			component.visit(new SelectableComponentVisitor() {
				@Override
				public void accept(final Camera camera) {
					possibleVerticesToTruncate.add(camera.getPosition());
					possibleVerticesToTruncate.add(camera.getTargetPosition());
				}

				@Override
				public void accept(final IdObject node) {
					possibleVerticesToTruncate.add(node.getPivotPoint());
				}

				@Override
				public void accept(final Geoset geoset) {
					possibleTrianglesToTruncate.addAll(geoset.getTriangles());
				}
			});
		}
		final Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(possibleTrianglesToTruncate);
		final Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
