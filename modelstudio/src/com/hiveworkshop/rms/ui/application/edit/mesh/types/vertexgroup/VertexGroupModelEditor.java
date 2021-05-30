package com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexgroup;

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
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.DoNothingAction;
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

public final class VertexGroupModelEditor extends AbstractModelEditor<VertexGroupBundle> {
	private final ProgramPreferences programPreferences;

	public VertexGroupModelEditor(ModelView model, ProgramPreferences programPreferences,
	                              SelectionManager<VertexGroupBundle> selectionManager,
	                              ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		throw new UnsupportedOperationException("This feature is not available in Vertex Group mode");
	}

	@Override
	public UndoAction setSelectedBoneName(String name) {
		throw new UnsupportedOperationException("This feature is not available in Vertex Group mode");
	}

	@Override
	public UndoAction addSelectedBoneSuffix(String name) {
		throw new UnsupportedOperationException("This feature is not available in Vertex Group mode");
	}

	@Override
	public UndoAction addTeamColor() {
		TeamColorAddAction<VertexGroupBundle> teamColorAddAction = new TeamColorAddAction<>(selectionManager.getSelectedFaces(), model.getModel(), structureChangeListener, selectionManager, vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public UndoAction splitGeoset() {
		SplitGeosetAction<VertexGroupBundle> teamColorAddAction = new SplitGeosetAction<>(selectionManager.getSelectedFaces(), model.getModel(), structureChangeListener, selectionManager, vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public UndoAction expandSelection() {
		return new DoNothingAction("expand selection");
	}

	@Override
	public UndoAction invertSelection() {
		Set<VertexGroupBundle> oldSelection = new HashSet<>(selectionManager.getSelection());
		Set<VertexGroupBundle> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (Geoset geoset : model.getEditableGeosets()) {
			for (int vertexGroupId = 0; vertexGroupId < geoset.getMatrix().size(); vertexGroupId++) {
				VertexGroupBundle bundle = new VertexGroupBundle(geoset, vertexGroupId);
				if (invertedSelection.contains(bundle)) {
					invertedSelection.remove(bundle);
				} else {
					invertedSelection.add(bundle);
				}
			}
		}
		selectionManager.setSelection(invertedSelection);
		return (new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection"));
	}

	@Override
	public UndoAction selectAll() {
		Set<VertexGroupBundle> oldSelection = new HashSet<>(selectionManager.getSelection());
		Set<VertexGroupBundle> allSelection = new HashSet<>();
		for (Geoset geoset : model.getEditableGeosets()) {
			for (int vertexGroupId = 0; vertexGroupId < geoset.getMatrix().size(); vertexGroupId++) {
				VertexGroupBundle bundle = new VertexGroupBundle(geoset, vertexGroupId);
				allSelection.add(bundle);
			}
		}
		selectionManager.setSelection(allSelection);
		return (new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all"));
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
		for (Geoset geoset : model.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (hitTest(geosetVertex, CoordinateSystem.Util.geom(axes, point), axes, programPreferences.getVertexSize())) {
					canSelect = true;
				}
			}
		}
		return canSelect;
	}

	@Override
	protected List<VertexGroupBundle> genericSelect(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<VertexGroupBundle> newSelection = new ArrayList<>();
		Rectangle2D area = getArea(region);

		for (Geoset geoset : model.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (triHitTest(triangle, new Point2D.Double(area.getX(), area.getY()), coordinateSystem)
						|| triHitTest(triangle, new Point2D.Double(area.getX() + area.getWidth(), area.getY() + area.getHeight()), coordinateSystem)
						|| triHitTest(triangle, area, coordinateSystem)) {
					for (GeosetVertex vertex : triangle.getAll()) {
						newSelection.add(new VertexGroupBundle(geoset, vertex.getVertexGroup()));
					}
				}
			}
		}

		List<GeosetVertex> geosetVerticesSelected = new ArrayList<>();
		for (Geoset geoset : model.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (hitTest(area, geosetVertex, coordinateSystem, programPreferences.getVertexSize())) {
					geosetVerticesSelected.add(geosetVertex);
				}
			}
		}
		for (GeosetVertex vertex : geosetVerticesSelected) {
			newSelection.add(new VertexGroupBundle(vertex.getGeoset(), vertex.getVertexGroup()));
		}
		return newSelection;
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends SelectableComponent> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<VertexGroupBundle> previousSelection = new ArrayList<>(selectionManager.getSelection());
		List<VertexGroupBundle> vertexBundlesToTruncate = new ArrayList<>(selectionManager.getSelection());
		for (SelectableComponent component : selectableComponents) {
			component.visit(new SelectableComponentVisitor() {
				@Override
				public void accept(Camera camera) {
				}

				@Override
				public void accept(IdObject node) {
				}

				@Override
				public void accept(Geoset geoset) {
					for (VertexGroupBundle bundle : previousSelection) {
						if (bundle.getGeoset() == geoset) {
							vertexBundlesToTruncate.add(bundle);
						}
					}
				}
			});
		}
		Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(vertexBundlesToTruncate);
		Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		List<VertexGroupBundle> newSelectionGroups = new ArrayList<>();
		for (Geoset geoset : model.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (newSelection.contains(geosetVertex)) {
					newSelectionGroups.add(new VertexGroupBundle(geosetVertex.getGeoset(), geosetVertex.getVertexGroup()));
				}
			}
		}
		selectionManager.setSelection(newSelectionGroups);
	}

	@Override
	public CopiedModelData copySelection() {
		// TODO fix heavy overlap with other model editor code
		Set<VertexGroupBundle> selection = selectionManager.getSelection();
		List<Geoset> copiedGeosets = new ArrayList<>();
		for (Geoset geoset : model.getEditableGeosets()) {
			Geoset copy = new Geoset();
			copy.setSelectionGroup(geoset.getSelectionGroup());
			copy.setAnims(geoset.getAnims());
			copy.setMaterial(geoset.getMaterial());
			Set<Triangle> copiedTriangles = new HashSet<>();
			Set<GeosetVertex> copiedVertices = new HashSet<>();
			for (Triangle triangle : geoset.getTriangles()) {
				boolean triangleIsFullySelected = true;
				List<GeosetVertex> triangleVertices = new ArrayList<>(3);
				for (GeosetVertex geosetVertex : triangle.getAll()) {
					if (selection.contains(new VertexGroupBundle(geoset, geosetVertex.getVertexGroup()))) {
						GeosetVertex newGeosetVertex = new GeosetVertex(geosetVertex);
						newGeosetVertex.clearTriangles();
						copiedVertices.add(newGeosetVertex);
						triangleVertices.add(newGeosetVertex);
					} else {
						triangleIsFullySelected = false;
					}
				}
				if (triangleIsFullySelected) {
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
	public UndoAction createFaceFromSelection(Vec3 preferredFacingVector) {
		throw new WrongModeException("Unable to create face from vertices in vertex group selection mode");
	}

	@Override
	public UndoAction addVertex(double x, double y, double z, Vec3 preferredNormalFacingVector) {
		throw new WrongModeException("Unable to draw vertices in vertex group selection mode");
	}

	@Override
	public UndoAction setParent(IdObject node) {
		throw new UnsupportedOperationException("This feature is not available in Vertex Group mode");
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
