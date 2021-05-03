package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
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

public class FaceTVertexEditor extends TVertexEditor<Triangle> {
	private final ProgramPreferences programPreferences;

	public FaceTVertexEditor(ModelView model, ProgramPreferences programPreferences, SelectionManager<Triangle> selectionManager, ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
	}

	public static boolean hitTest(Triangle triangle, Point2D point, CoordinateSystem coordinateSystem, int uvLayerIndex) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		GeosetVertex[] verts = triangle.getVerts();
		Path2D.Double path = new Path2D.Double();
		path.moveTo(verts[0].getTVertex(uvLayerIndex).getCoord(dim1), verts[0].getTVertex(uvLayerIndex).getCoord(dim2));
		for (int i = 1; i < verts.length; i++) {
			path.lineTo(verts[i].getTVertex(uvLayerIndex).getCoord(dim1), verts[i].getTVertex(uvLayerIndex).getCoord(dim2));
		} // TODO fix bad performance allocation
		path.closePath();
		return path.contains(point);
	}

	public static boolean hitTest(Triangle triangle, Rectangle2D rectangle, CoordinateSystem coordinateSystem, int uvLayerIndex) {
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		GeosetVertex[] verts = triangle.getVerts();
		Path2D.Double path = new Path2D.Double();
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
	protected List<Triangle> genericSelect(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<Triangle> newSelection = new ArrayList<>();
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
			for (Triangle triangle : geoset.getTriangles()) {
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
	public boolean canSelectAt(Point point, CoordinateSystem axes) {
		boolean canSelect = false;
		for (Geoset geoset : model.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (hitTest(triangle, CoordSysUtils.geom(axes, point), axes, uvLayerIndex)) {
					canSelect = true;
				}
			}
		}
		return canSelect;
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends SelectableComponent> selectableComponents,
	                                              EditabilityToggleHandler editabilityToggleHandler,
	                                              Runnable refreshGUIRunnable) {
		List<Triangle> previousSelection = new ArrayList<>(selectionManager.getSelection());
		List<Triangle> possibleTrianglesToTruncate = new ArrayList<>();
		List<Vec3> possibleVerticesToTruncate = new ArrayList<>();
		for (SelectableComponent component : selectableComponents) {
			component.visit(new SelectableComponentVisitor() {
				@Override
				public void accept(Camera camera) {
					possibleVerticesToTruncate.add(camera.getPosition());
					possibleVerticesToTruncate.add(camera.getTargetPosition());
				}

				@Override
				public void accept(IdObject node) {
					possibleVerticesToTruncate.add(node.getPivotPoint());
				}

				@Override
				public void accept(Geoset geoset) {
					possibleTrianglesToTruncate.addAll(geoset.getTriangles());
				}
			});
		}
		Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(possibleTrianglesToTruncate);
		Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
