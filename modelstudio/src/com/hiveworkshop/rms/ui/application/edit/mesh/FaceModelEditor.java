package com.hiveworkshop.rms.ui.application.edit.mesh;

import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.actions.mesh.SplitGeosetAction;
import com.hiveworkshop.rms.ui.application.actions.mesh.TeamColorAddAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vector3;

public class FaceModelEditor extends AbstractModelEditor<Triangle> {
	private final ProgramPreferences programPreferences;

	public FaceModelEditor(final ModelView model, final ProgramPreferences programPreferences,
			final SelectionManager<Triangle> selectionManager,
			final ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		throw new UnsupportedOperationException("This feature is not available in Face mode");
	}

	@Override
	public UndoAction setSelectedBoneName(final String name) {
		throw new UnsupportedOperationException("This feature is not available in Face mode");
	}

	@Override
	public UndoAction addSelectedBoneSuffix(final String name) {
		throw new UnsupportedOperationException("This feature is not available in Face mode");
	}

	@Override
	public UndoAction addTeamColor() {
		// copy the selection before we hand it off, so the we can't strip the stored
		// action's
		// list of faces to add/remove
		final TeamColorAddAction<Triangle> teamColorAddAction = new TeamColorAddAction<>(
				new ArrayList<>(selectionManager.getSelection()), model.getModel(), structureChangeListener,
				selectionManager, vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public UndoAction splitGeoset() {
		// copy the selection before we hand it off, so the we can't strip the stored
		// action's
		// list of faces to add/remove
		final SplitGeosetAction<Triangle> teamColorAddAction = new SplitGeosetAction<>(
				new ArrayList<>(selectionManager.getSelection()), model.getModel(), structureChangeListener,
				selectionManager, vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public void selectByVertices(final Collection<? extends Vector3> newSelection) {
		final Set<Triangle> newlySelectedFaces = new HashSet<>();
		for (final Geoset geoset : model.getModel().getGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				boolean allInSelection = true;
				for (final GeosetVertex vertex : triangle.getVerts()) {
					if (!newSelection.contains(vertex)) {
						allInSelection = false;
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
			for (final Triangle triangle : geoset.getTriangles()) {
				allSelection.add(triangle);
			}
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
				if (hitTest(triangle, new Point2D.Double(area.getX(), area.getY()), coordinateSystem) || hitTest(
						triangle, new Point2D.Double(area.getX() + area.getWidth(), area.getY() + area.getHeight()),
						coordinateSystem) || hitTest(triangle, area, coordinateSystem)) {
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
				if (hitTest(triangle, CoordinateSystem.Util.geom(axes, point), axes)) {
					canSelect = true;
				}
			}
		}
		return canSelect;
	}

	@Override
	protected UndoAction buildHideComponentAction(final List<? extends SelectableComponent> selectableComponents,
                                                  final EditabilityToggleHandler editabilityToggleHandler, final Runnable refreshGUIRunnable) {
		final List<Triangle> previousSelection = new ArrayList<>(selectionManager.getSelection());
		final List<Triangle> possibleTrianglesToTruncate = new ArrayList<>();
		final List<Vector3> possibleVerticesToTruncate = new ArrayList<>();
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
		final Runnable truncateSelectionRunnable = new Runnable() {
			@Override
			public void run() {
				selectionManager.removeSelection(possibleTrianglesToTruncate);
			}
		};
		final Runnable unTruncateSelectionRunnable = new Runnable() {
			@Override
			public void run() {
				selectionManager.setSelection(previousSelection);
			}
		};
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable,
				unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	public static boolean hitTest(final Triangle triangle, final Point2D point,
			final CoordinateSystem coordinateSystem) {
		final byte dim1 = coordinateSystem.getPortFirstXYZ();
		final byte dim2 = coordinateSystem.getPortSecondXYZ();
		final GeosetVertex[] verts = triangle.getVerts();
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(verts[0].getCoord(dim1), verts[0].getCoord(dim2));
		for (int i = 1; i < verts.length; i++) {
			path.lineTo(verts[i].getCoord(dim1), verts[i].getCoord(dim2));
			// xpts[i] = (int)
			// (verts[i].getCoord(dim1));
			// ypts[i] = (int)
			// (verts[i].getCoord(dim2));
		} // TODO fix bad performance allocation
		path.closePath();
		return path.contains(point);
	}

	public static boolean hitTest(final Triangle triangle, final Rectangle2D rectangle,
			final CoordinateSystem coordinateSystem) {
		final byte dim1 = coordinateSystem.getPortFirstXYZ();
		final byte dim2 = coordinateSystem.getPortSecondXYZ();
		final GeosetVertex[] verts = triangle.getVerts();
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(verts[0].getCoord(dim1), verts[0].getCoord(dim2));
		for (int i = 1; i < verts.length; i++) {
			path.lineTo(verts[i].getCoord(dim1), verts[i].getCoord(dim2));
		}
		return rectangle.contains(verts[0].getCoord(dim1), verts[0].getCoord(dim2))
				|| rectangle.contains(verts[1].getCoord(dim1), verts[1].getCoord(dim2))
				|| rectangle.contains(verts[2].getCoord(dim1), verts[2].getCoord(dim2)) || path.intersects(rectangle);
	}

	@Override
	public CopiedModelData copySelection() {
		// TODO heavy overlap with GeosetVertexModelEditor's code
		final Set<Triangle> selection = selectionManager.getSelection();
		final List<Geoset> copiedGeosets = new ArrayList<>();
		for (final Geoset geoset : model.getEditableGeosets()) {
			final Geoset copy = new Geoset();
			copy.setSelectionGroup(geoset.getSelectionGroup());
			copy.setAnims(geoset.getAnims());
			copy.setMaterial(geoset.getMaterial());
			final Set<Triangle> copiedTriangles = new HashSet<>();
			final Set<GeosetVertex> copiedVertices = new HashSet<>();
			for (final Triangle triangle : geoset.getTriangles()) {
				if (selection.contains(triangle)) {
					final List<GeosetVertex> triangleVertices = new ArrayList<>(3);
					for (final GeosetVertex geosetVertex : triangle.getAll()) {
						final GeosetVertex newGeosetVertex = new GeosetVertex(geosetVertex);
						newGeosetVertex.getTriangles().clear();
						copiedVertices.add(newGeosetVertex);
						triangleVertices.add(newGeosetVertex);
					}
					final Triangle newTriangle = new Triangle(triangleVertices.get(0), triangleVertices.get(1),
							triangleVertices.get(2), copy);
					copiedTriangles.add(newTriangle);
				}
			}
			for (final Triangle triangle : copiedTriangles) {
				copy.add(triangle);
			}
			for (final GeosetVertex geosetVertex : copiedVertices) {
				copy.add(geosetVertex);
			}
			if ((copiedTriangles.size() > 0) || (copiedVertices.size() > 0)) {
				copiedGeosets.add(copy);
			}
		}
		return new CopiedModelData(copiedGeosets, new ArrayList<IdObject>(), new ArrayList<Camera>());
	}

	@Override
	public UndoAction addVertex(final double x, final double y, final double z,
			final Vector3 preferredNormalFacingVector) {
		throw new WrongModeException("Unable to add vertex in face selection mode");
	}

	@Override
	public UndoAction createFaceFromSelection(final Vector3 preferredFacingVector) {
		throw new WrongModeException("Unable to create face from vertices in face selection mode");
	}

	@Override
	public UndoAction setParent(final IdObject node) {
		throw new UnsupportedOperationException("This feature is not available in Face mode");
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
