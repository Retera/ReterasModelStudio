package com.hiveworkshop.rms.ui.application.edit.mesh.types.faces;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.actions.mesh.SplitGeosetAction;
import com.hiveworkshop.rms.ui.application.actions.mesh.TeamColorAddAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
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
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class FaceModelEditor extends AbstractModelEditor<Triangle> {
	private final ProgramPreferences programPreferences;

	public FaceModelEditor(ModelView model, ProgramPreferences programPreferences,
	                       SelectionManager<Triangle> selectionManager,
	                       ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		throw new UnsupportedOperationException("This feature is not available in Face mode");
	}

	@Override
	public UndoAction setSelectedBoneName(String name) {
		throw new UnsupportedOperationException("This feature is not available in Face mode");
	}

	@Override
	public UndoAction addSelectedBoneSuffix(String name) {
		throw new UnsupportedOperationException("This feature is not available in Face mode");
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
	public UndoAction addTeamColor() {
		// copy the selection before we hand it off, so the we can't strip the stored action's list of faces to add/remove
		TeamColorAddAction<Triangle> teamColorAddAction = new TeamColorAddAction<>(new ArrayList<>(selectionManager.getSelection()), model.getModel(), structureChangeListener, selectionManager, vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public boolean canSelectAt(Point point, CoordinateSystem axes) {
		boolean canSelect = false;
		for (Geoset geoset : model.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (triHitTest(triangle, CoordinateSystem.Util.geom(axes, point), axes)) {
					canSelect = true;
				}
			}
		}
		return canSelect;
	}

	@Override
	protected List<Triangle> genericSelect(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<Triangle> newSelection = new ArrayList<>();
		Rectangle2D area = getArea(region);

		for (Geoset geoset : model.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (triHitTest(triangle, new Point2D.Double(area.getX(), area.getY()), coordinateSystem)
						|| triHitTest(triangle, new Point2D.Double(area.getX() + area.getWidth(), area.getY() + area.getHeight()), coordinateSystem)
						|| triHitTest(triangle, area, coordinateSystem)) {
					newSelection.add(triangle);
				}
			}
		}
		return newSelection;
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends SelectableComponent> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
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

	@Override
	public UndoAction splitGeoset() {
		// copy the selection before we hand it off, so the we can't strip the stored action's list of faces to add/remove
		SplitGeosetAction<Triangle> teamColorAddAction = new SplitGeosetAction<>(new ArrayList<>(selectionManager.getSelection()), model.getModel(), structureChangeListener, selectionManager, vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public CopiedModelData copySelection() {
		// TODO heavy overlap with GeosetVertexModelEditor's code
		Set<Triangle> selection = selectionManager.getSelection();
		List<Geoset> copiedGeosets = new ArrayList<>();
		for (Geoset geoset : model.getEditableGeosets()) {
			Geoset copy = new Geoset();
			copy.setSelectionGroup(geoset.getSelectionGroup());
			copy.setAnims(geoset.getAnims());
			copy.setMaterial(geoset.getMaterial());
			Set<Triangle> copiedTriangles = new HashSet<>();
			Set<GeosetVertex> copiedVertices = new HashSet<>();
			for (Triangle triangle : geoset.getTriangles()) {
				if (selection.contains(triangle)) {
					List<GeosetVertex> triangleVertices = new ArrayList<>(3);
					for (GeosetVertex geosetVertex : triangle.getAll()) {
						GeosetVertex newGeosetVertex = new GeosetVertex(geosetVertex);
						newGeosetVertex.clearTriangles();
						copiedVertices.add(newGeosetVertex);
						triangleVertices.add(newGeosetVertex);
					}
					Triangle newTriangle = new Triangle(triangleVertices.get(0), triangleVertices.get(1), triangleVertices.get(2), copy);
					copiedTriangles.add(newTriangle);
				}
			}
			for (Triangle triangle : copiedTriangles) {
				copy.add(triangle);
			}
			for (GeosetVertex geosetVertex : copiedVertices) {
				copy.add(geosetVertex);
			}
			if ((copiedTriangles.size() > 0) || (copiedVertices.size() > 0)) {
				copiedGeosets.add(copy);
			}
		}
		return new CopiedModelData(copiedGeosets, new ArrayList<>(), new ArrayList<>());
	}

	@Override
	public UndoAction addVertex(double x, double y, double z, Vec3 preferredNormalFacingVector) {
		throw new WrongModeException("Unable to add vertex in face selection mode");
	}

	@Override
	public UndoAction createFaceFromSelection(Vec3 preferredFacingVector) {
		throw new WrongModeException("Unable to create face from vertices in face selection mode");
	}

	@Override
	public UndoAction setParent(IdObject node) {
		throw new UnsupportedOperationException("This feature is not available in Face mode");
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
