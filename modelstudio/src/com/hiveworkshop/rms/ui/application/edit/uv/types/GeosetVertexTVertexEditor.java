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
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class GeosetVertexTVertexEditor extends AbstractTVertexEditor<GeosetVertex> {
	private final ProgramPreferences programPreferences;

	public GeosetVertexTVertexEditor(final ModelView model, final ProgramPreferences programPreferences, final SelectionManager<GeosetVertex> selectionManager, final ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
	}

	public static void hitTest(final List<GeosetVertex> selectedItems, final Rectangle2D area, final GeosetVertex geosetVertex, final Vec2 tVertex, final CoordinateSystem coordinateSystem, final double vertexSize) {
		final byte dim1 = coordinateSystem.getPortFirstXYZ();
		final byte dim2 = coordinateSystem.getPortSecondXYZ();
		final double minX = coordinateSystem.viewX(area.getMinX());
		final double minY = coordinateSystem.viewY(area.getMinY());
		final double maxX = coordinateSystem.viewX(area.getMaxX());
		final double maxY = coordinateSystem.viewY(area.getMaxY());
		final double vertexX = tVertex.getCoord(dim1);
		final double x = coordinateSystem.viewX(vertexX);
		final double vertexY = tVertex.getCoord(dim2);
		final double y = coordinateSystem.viewY(vertexY);
		if (distance(x, y, minX, minY) <= vertexSize / 2.0
				|| distance(x, y, maxX, maxY) <= vertexSize / 2.0
				|| area.contains(vertexX, vertexY)) {
			selectedItems.add(geosetVertex);
		}
	}

	public static boolean hitTest(final Vec2 vertex, final Point2D point, final CoordinateSystem coordinateSystem, final double vertexSize) {
		final double x = coordinateSystem.viewX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
		final double y = coordinateSystem.viewY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
		final double px = coordinateSystem.viewX(point.getX());
		final double py = coordinateSystem.viewY(point.getY());
		return Point2D.distance(px, py, x, y) <= vertexSize / 2.0;
	}

	public static double distance(final double vertexX, final double vertexY, final double x, final double y) {
		final double dx = x - vertexX;
		final double dy = y - vertexY;
		return Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public void selectByVertices(final Collection<? extends Vec3> newSelection) {
		final List<GeosetVertex> newGeosetVertices = new ArrayList<>();
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final GeosetVertex vertex : geoset.getVertices()) {
				if (newSelection.contains(vertex)) {
					newGeosetVertices.add(vertex);
				}
			}
		}
		selectionManager.setSelection(newGeosetVertices);
	}

	@Override
	public UndoAction expandSelection() {
		final Set<GeosetVertex> expandedSelection = new HashSet<>(selectionManager.getSelection());
		final List<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		for (final GeosetVertex v : oldSelection) {
			expandSelection(v, expandedSelection);
		}
		selectionManager.setSelection(expandedSelection);
		return new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection");
	}

	private void expandSelection(final GeosetVertex currentVertex, final Set<GeosetVertex> selection) {
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
	public UndoAction invertSelection() {
		final List<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<GeosetVertex> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (final Geoset geo : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geo.getVertices()) {
				toggleSelection(invertedSelection, geosetVertex);
			}
		}
		selectionManager.setSelection(invertedSelection);
		return new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection");
	}

	private void toggleSelection(final Set<GeosetVertex> selection, final GeosetVertex position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public UndoAction selectAll() {
		final List<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<GeosetVertex> allSelection = new HashSet<>();
		for (final Geoset geo : model.getEditableGeosets()) {
			allSelection.addAll(geo.getVertices());
		}
		selectionManager.setSelection(allSelection);
		return new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all");
	}

	@Override
	protected List<GeosetVertex> genericSelect(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<GeosetVertex> selectedItems = new ArrayList<>();
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
			for (final GeosetVertex geosetVertex : geoset.getVertices()) {
				if (geosetVertex.getTverts().size() > uvLayerIndex) {
					hitTest(selectedItems, area, geosetVertex, geosetVertex.getTVertex(uvLayerIndex), coordinateSystem, programPreferences.getVertexSize());
				}
			}
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
		boolean canSelect = false;
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geoset.getVertices()) {
				if (geosetVertex.getTverts().size() > uvLayerIndex) {
					if (hitTest(geosetVertex.getTVertex(uvLayerIndex), CoordinateSystem.Util.geom(axes, point), axes, programPreferences.getVertexSize())) {
						canSelect = true;
					}
				}
			}
		}
		return canSelect;
	}

	@Override
	protected UndoAction buildHideComponentAction(final List<? extends SelectableComponent> selectableComponents,
                                                  final EditabilityToggleHandler editabilityToggleHandler,
                                                  final Runnable refreshGUIRunnable) {
		final List<GeosetVertex> previousSelection = new ArrayList<>(selectionManager.getSelection());
		final List<GeosetVertex> possibleVerticesToTruncate = new ArrayList<>();
		for (final SelectableComponent component : selectableComponents) {
			component.visit(new SelectableComponentVisitor() {
				@Override
				public void accept(final Camera camera) {
				}

				@Override
				public void accept(final IdObject node) {
				}

				@Override
				public void accept(final Geoset geoset) {
					possibleVerticesToTruncate.addAll(geoset.getVertices());
				}
			});
		}
		final Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(possibleVerticesToTruncate);

		final Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
