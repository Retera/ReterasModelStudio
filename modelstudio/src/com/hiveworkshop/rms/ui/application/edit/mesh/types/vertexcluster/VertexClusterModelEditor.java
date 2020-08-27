package com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexcluster;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.actions.mesh.SplitGeosetAction;
import com.hiveworkshop.rms.ui.application.actions.mesh.TeamColorAddAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.faces.FaceModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.geosetvertex.GeosetVertexModelEditor;
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

public final class VertexClusterModelEditor extends AbstractModelEditor<VertexClusterModelEditor.VertexGroupBundle> {
	private final ProgramPreferences programPreferences;
	private final Map<Vec3, Integer> vertexToClusterId = new HashMap<>();
	private final VertexClusterDefinitions vertexClusterDefinitions;

	public VertexClusterModelEditor(final ModelView model, final ProgramPreferences programPreferences,
									final SelectionManager<VertexGroupBundle> selectionManager,
									final ModelStructureChangeListener structureChangeListener,
									final VertexClusterDefinitions vertexClusterDefinitions) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
		this.vertexClusterDefinitions = vertexClusterDefinitions;
	}

	public static final class VertexGroupBundle {
		private final Geoset geoset;
		private final int vertexGroupId;

		public VertexGroupBundle(final Geoset geoset, final int vertexGroupId) {
			this.geoset = geoset;
			this.vertexGroupId = vertexGroupId;
		}

		public Geoset getGeoset() {
			return geoset;
		}

		public int getVertexGroupId() {
			return vertexGroupId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((geoset == null) ? 0 : geoset.hashCode());
			result = (prime * result) + vertexGroupId;
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final VertexGroupBundle other = (VertexGroupBundle) obj;
			if (geoset == null) {
				if (other.geoset != null) {
					return false;
				}
			} else if (!geoset.equals(other.geoset)) {
				return false;
			}
			return vertexGroupId == other.vertexGroupId;
		}

	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		throw new UnsupportedOperationException("This feature is not available in Vertex Group mode");
	}

	@Override
	public UndoAction setSelectedBoneName(final String name) {
		throw new UnsupportedOperationException("This feature is not available in Vertex Group mode");
	}

	@Override
	public UndoAction addSelectedBoneSuffix(final String name) {
		throw new UnsupportedOperationException("This feature is not available in Vertex Group mode");
	}

	@Override
	public UndoAction addTeamColor() {
		final TeamColorAddAction<VertexGroupBundle> teamColorAddAction = new TeamColorAddAction<>(
				selectionManager.getSelectedFaces(), model.getModel(), structureChangeListener, selectionManager,
				vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public UndoAction splitGeoset() {
		final SplitGeosetAction<VertexGroupBundle> teamColorAddAction = new SplitGeosetAction<>(
				selectionManager.getSelectedFaces(), model.getModel(), structureChangeListener, selectionManager,
				vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public UndoAction expandSelection() {
		return new DoNothingAction("expand selection");
	}

	@Override
	public UndoAction invertSelection() {
		final Set<VertexGroupBundle> oldSelection = new HashSet<>(selectionManager.getSelection());
		final Set<VertexGroupBundle> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (int vertexGroupId = -1; vertexGroupId < vertexClusterDefinitions
					.getMaxClusterIdKnown(); vertexGroupId++) {
				final VertexGroupBundle bundle = new VertexGroupBundle(geoset, vertexGroupId);
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
		final Set<VertexGroupBundle> oldSelection = new HashSet<>(selectionManager.getSelection());
		final Set<VertexGroupBundle> allSelection = new HashSet<>();
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (int vertexGroupId = -1; vertexGroupId < vertexClusterDefinitions
					.getMaxClusterIdKnown(); vertexGroupId++) {
				final VertexGroupBundle bundle = new VertexGroupBundle(geoset, vertexGroupId);
				allSelection.add(bundle);
			}
		}
		selectionManager.setSelection(allSelection);
		return (new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all"));
	}

	@Override
	public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
		boolean canSelect = false;
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				if (FaceModelEditor.hitTest(triangle, CoordinateSystem.Util.geom(axes, point), axes)) {
					canSelect = true;
				}
			}
		}
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geoset.getVertices()) {
				if (GeosetVertexModelEditor.hitTest(geosetVertex, CoordinateSystem.Util.geom(axes, point), axes,
						programPreferences.getVertexSize())) {
					canSelect = true;
				}
			}
		}
		return canSelect;
	}

	@Override
	protected List<VertexGroupBundle> genericSelect(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<VertexGroupBundle> newSelection = new ArrayList<>();
		final double startingClickX = region.getX();
		final double startingClickY = region.getY();
		final double endingClickX = region.getX() + region.getWidth();
		final double endingClickY = region.getY() + region.getHeight();

		final double minX = Math.min(startingClickX, endingClickX);
		final double minY = Math.min(startingClickY, endingClickY);
		final double maxX = Math.max(startingClickX, endingClickX);
		final double maxY = Math.max(startingClickY, endingClickY);
		final Rectangle2D area = new Rectangle2D.Double(minX, minY, (maxX - minX), (maxY - minY));
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				if (FaceModelEditor.hitTest(triangle, new Point2D.Double(area.getX(), area.getY()), coordinateSystem)
						|| FaceModelEditor.hitTest(triangle,
								new Point2D.Double(area.getX() + area.getWidth(), area.getY() + area.getHeight()),
								coordinateSystem)
						|| FaceModelEditor.hitTest(triangle, area, coordinateSystem)) {
					for (final GeosetVertex vertex : triangle.getAll()) {
						newSelection.add(new VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(vertex)));
					}
				}
			}
		}
		final List<GeosetVertex> geosetVerticesSelected = new ArrayList<>();
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geoset.getVertices()) {
				GeosetVertexModelEditor.hitTest(geosetVerticesSelected, area, geosetVertex, coordinateSystem,
						programPreferences.getVertexSize());
			}
		}
		for (final GeosetVertex vertex : geosetVerticesSelected) {
			newSelection.add(new VertexGroupBundle(vertex.getGeoset(), vertexClusterDefinitions.getClusterId(vertex)));
		}
		return newSelection;
	}

	@Override
	protected UndoAction buildHideComponentAction(final List<? extends SelectableComponent> selectableComponents,
                                                  final EditabilityToggleHandler editabilityToggleHandler, final Runnable refreshGUIRunnable) {
		final List<VertexGroupBundle> previousSelection = new ArrayList<>(selectionManager.getSelection());
		final List<VertexGroupBundle> vertexBundlesToTruncate = new ArrayList<>(selectionManager.getSelection());
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
					for (final VertexGroupBundle bundle : previousSelection) {
						if (bundle.getGeoset() == geoset) {
							vertexBundlesToTruncate.add(bundle);
						}
					}
				}
			});
		}
		final Runnable truncateSelectionRunnable = new Runnable() {
			@Override
			public void run() {
				selectionManager.removeSelection(vertexBundlesToTruncate);
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

	@Override
	public void selectByVertices(final Collection<? extends Vec3> newSelection) {
		final List<VertexGroupBundle> newSelectionGroups = new ArrayList<>();
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geoset.getVertices()) {
				if (newSelection.contains(geosetVertex)) {
					newSelectionGroups.add(new VertexGroupBundle(geosetVertex.getGeoset(),
							vertexClusterDefinitions.getClusterId(geosetVertex)));
				}
			}
		}
		selectionManager.setSelection(newSelectionGroups);
	}

	@Override
	public CopiedModelData copySelection() {
		// TODO fix heavy overlap with other model editor code
		final Set<VertexGroupBundle> selection = selectionManager.getSelection();
		final List<Geoset> copiedGeosets = new ArrayList<>();
		for (final Geoset geoset : model.getEditableGeosets()) {
			final Geoset copy = new Geoset();
			copy.setSelectionGroup(geoset.getSelectionGroup());
			copy.setAnims(geoset.getAnims());
			copy.setMaterial(geoset.getMaterial());
			final Set<Triangle> copiedTriangles = new HashSet<>();
			final Set<GeosetVertex> copiedVertices = new HashSet<>();
			for (final Triangle triangle : geoset.getTriangles()) {
				boolean triangleIsFullySelected = true;
				final List<GeosetVertex> triangleVertices = new ArrayList<>(3);
				for (final GeosetVertex geosetVertex : triangle.getAll()) {
					if (selection.contains(
							new VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(geosetVertex)))) {
						final GeosetVertex newGeosetVertex = new GeosetVertex(geosetVertex);
						newGeosetVertex.getTriangles().clear();
						copiedVertices.add(newGeosetVertex);
						triangleVertices.add(newGeosetVertex);
					} else {
						triangleIsFullySelected = false;
					}
				}
				if (triangleIsFullySelected) {
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
	public UndoAction createFaceFromSelection(final Vec3 preferredFacingVector) {
		throw new WrongModeException("Unable to create face from vertices in vertex group selection mode");
	}

	@Override
	public UndoAction addVertex(final double x, final double y, final double z,
			final Vec3 preferredNormalFacingVector) {
		throw new WrongModeException("Unable to draw vertices in vertex group selection mode");
	}

	@Override
	public UndoAction setParent(final IdObject node) {
		throw new UnsupportedOperationException("This feature is not available in Vertex Group mode");
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
