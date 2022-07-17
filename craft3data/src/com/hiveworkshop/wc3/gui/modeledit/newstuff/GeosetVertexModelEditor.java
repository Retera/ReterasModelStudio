package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.FaceCreationException;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.SplitGeosetAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.TeamColorAddAction;
import com.hiveworkshop.wc3.gui.modeledit.creator.actions.DrawVertexAction;
import com.hiveworkshop.wc3.gui.modeledit.creator.actions.NewGeosetAction;
import com.hiveworkshop.wc3.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools.AddTriangleAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Layer.FilterMode;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class GeosetVertexModelEditor extends AbstractModelEditor<GeosetVertex> {
	private final ProgramPreferences programPreferences;

	public GeosetVertexModelEditor(final ModelView model, final ProgramPreferences programPreferences,
			final SelectionManager<GeosetVertex> selectionManager,
			final ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		throw new UnsupportedOperationException("This feature is not available in Geoset Vertex mode");
	}

	@Override
	public UndoAction setSelectedBoneName(final String name) {
		throw new UnsupportedOperationException("This feature is not available in Geoset Vertex mode");
	}

	@Override
	public UndoAction addSelectedBoneSuffix(final String name) {
		throw new UnsupportedOperationException("This feature is not available in Geoset Vertex mode");
	}

	@Override
	public UndoAction addTeamColor() {
		final TeamColorAddAction<GeosetVertex> teamColorAddAction = new TeamColorAddAction<>(
				selectionManager.getSelectedFaces(), model.getModel(), structureChangeListener, selectionManager,
				vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public UndoAction splitGeoset() {
		final SplitGeosetAction<GeosetVertex> teamColorAddAction = new SplitGeosetAction<>(
				selectionManager.getSelectedFaces(), model.getModel(), structureChangeListener, selectionManager,
				vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public void selectByVertices(final Collection<? extends Vertex> newSelection) {
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
		final ArrayList<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		for (final GeosetVertex v : oldSelection) {
			expandSelection(v, expandedSelection);
		}
		selectionManager.setSelection(expandedSelection);
		return (new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection"));
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
		final ArrayList<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<GeosetVertex> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (final Geoset geo : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geo.getVertices()) {
				toggleSelection(invertedSelection, geosetVertex);
			}
		}
		selectionManager.setSelection(invertedSelection);
		return (new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection"));
	}

	private void toggleSelection(final Set<GeosetVertex> selection, final GeosetVertex position) {
		if (selection.contains(position)) {
			selection.remove(position);
		}
		else {
			selection.add(position);
		}
	}

	@Override
	public UndoAction selectAll() {
		final ArrayList<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<GeosetVertex> allSelection = new HashSet<>();
		for (final Geoset geo : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geo.getVertices()) {
				allSelection.add(geosetVertex);
			}
		}
		selectionManager.setSelection(allSelection);
		return (new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all"));
	}

	@Override
	public UndoAction selectHDUnusedNodes() {
		final ArrayList<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<GeosetVertex> allSelection = new HashSet<>();
		selectionManager.setSelection(allSelection);
		return (new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select HD unused"));
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
		final Rectangle2D area = new Rectangle2D.Double(minX, minY, (maxX - minX), (maxY - minY));
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geoset.getVertices()) {
				hitTest(selectedItems, area, geosetVertex, coordinateSystem, programPreferences.getVertexSize());
			}
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
		boolean canSelect = false;
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geoset.getVertices()) {
				if (hitTest(geosetVertex, CoordinateSystem.Util.geom(axes, point), axes,
						programPreferences.getVertexSize())) {
					canSelect = true;
				}
			}
		}
		return canSelect;
	}

	public static void hitTest(final List<GeosetVertex> selectedItems, final Rectangle2D area,
			final GeosetVertex geosetVertex, final CoordinateSystem coordinateSystem, final double vertexSize) {
		final byte dim1 = coordinateSystem.getPortFirstXYZ();
		final byte dim2 = coordinateSystem.getPortSecondXYZ();
		final double minX = coordinateSystem.convertX(area.getMinX());
		final double minY = coordinateSystem.convertY(area.getMinY());
		final double maxX = coordinateSystem.convertX(area.getMaxX());
		final double maxY = coordinateSystem.convertY(area.getMaxY());
		final double vertexX = geosetVertex.getCoord(dim1);
		final double x = coordinateSystem.convertX(vertexX);
		final double vertexY = geosetVertex.getCoord(dim2);
		final double y = coordinateSystem.convertY(vertexY);
		if ((distance(x, y, minX, minY) <= (vertexSize / 2.0)) || (distance(x, y, maxX, maxY) <= (vertexSize / 2.0))
				|| area.contains(vertexX, vertexY)) {
			selectedItems.add(geosetVertex);
		}
	}

	public static boolean hitTest(final Vertex vertex, final Point2D point, final CoordinateSystem coordinateSystem,
			final double vertexSize) {
		final double x = coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
		final double y = coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
		final double px = coordinateSystem.convertX(point.getX());
		final double py = coordinateSystem.convertY(point.getY());
		return Point2D.distance(px, py, x, y) <= (vertexSize / 2.0);
	}

	public static double distance(final double vertexX, final double vertexY, final double x, final double y) {
		final double dx = x - vertexX;
		final double dy = y - vertexY;
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	@Override
	protected UndoAction buildHideComponentAction(final ListView<? extends SelectableComponent> selectableComponents,
			final EditabilityToggleHandler editabilityToggleHandler, final Runnable refreshGUIRunnable) {
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
		final Runnable truncateSelectionRunnable = new Runnable() {
			@Override
			public void run() {
				selectionManager.removeSelection(possibleVerticesToTruncate);
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
	public CopiedModelData copySelection() {
		final Set<GeosetVertex> selection = selectionManager.getSelection();
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
					if (selection.contains(geosetVertex)) {
						final GeosetVertex newGeosetVertex = new GeosetVertex(geosetVertex);
						newGeosetVertex.getTriangles().clear();
						copiedVertices.add(newGeosetVertex);
						triangleVertices.add(newGeosetVertex);
					}
					else {
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
	public UndoAction addVertex(final double x, final double y, final double z,
			final Vertex preferredNormalFacingVector) {
		final ArrayList<Geoset> geosets = model.getModel().getGeosets();
		Geoset solidWhiteGeoset = null;
		for (final Geoset geoset : geosets) {
			final Layer firstLayer = geoset.getMaterial().firstLayer();
			if ((geoset.getMaterial() != null) && (firstLayer != null)
					&& (firstLayer.getFilterMode() == FilterMode.NONE)
					&& "Textures\\white.blp".equalsIgnoreCase(firstLayer.getTextureBitmap().getPath())) {
				solidWhiteGeoset = geoset;
			}
		}
		boolean needsGeosetAction = false;
		if (solidWhiteGeoset == null) {
			solidWhiteGeoset = new Geoset();
			solidWhiteGeoset.setMaterial(new Material(new Layer("None", new Bitmap("Textures\\white.blp"))));
			needsGeosetAction = true;
		}
		final GeosetVertex geosetVertex = new GeosetVertex(x, y, z, new Normal(preferredNormalFacingVector.x,
				preferredNormalFacingVector.y, preferredNormalFacingVector.z));
		geosetVertex.setGeoset(solidWhiteGeoset);
		geosetVertex.addTVertex(new TVertex(0, 0));
		UndoAction action;
		final DrawVertexAction drawVertexAction = new DrawVertexAction(geosetVertex);
		if (needsGeosetAction) {
			final NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, model.getModel(),
					structureChangeListener);
			action = new CompoundAction("add vertex", ListView.Util.of(newGeosetAction, drawVertexAction));
		}
		else {
			action = drawVertexAction;
		}
		action.redo();
		return action;
	}

	@Override
	public UndoAction createFaceFromSelection(final Vertex preferredFacingVector) {
		final Set<GeosetVertex> selection = selectionManager.getSelection();
		if (selection.size() != 3) {
			throw new FaceCreationException(
					"A face can only be created from exactly 3 vertices (you have " + selection.size() + " selected)");
		}
		int index = 0;
		final GeosetVertex[] verticesArray = new GeosetVertex[3];
		Geoset geoset = null;
		for (final GeosetVertex vertex : selection) {
			verticesArray[index++] = vertex;
			if (geoset == null) {
				geoset = vertex.getGeoset();
			}
			else if (geoset != vertex.getGeoset()) {
				throw new FaceCreationException(
						"All three vertices to create a face must be a part of the same Geoset");
			}
		}
		for (final Triangle existingTriangle : verticesArray[0].getTriangles()) {
			if (existingTriangle.contains(verticesArray[0]) && existingTriangle.contains(verticesArray[1])
					&& existingTriangle.contains(verticesArray[2])) {
				throw new FaceCreationException("Triangle already exists");
			}
		}

		final Triangle newTriangle = new Triangle(verticesArray[0], verticesArray[1], verticesArray[2], geoset);
		final Vertex facingVector = newTriangle.getFacingVector();
		final double cosine = facingVector.dotProduct(preferredFacingVector)
				/ (facingVector.vectorMagnitude() * preferredFacingVector.vectorMagnitude());
		if (cosine < 0) {
			newTriangle.flip(false);
		}

		final AddTriangleAction addTriangleAction = new AddTriangleAction(geoset,
				Collections.singletonList(newTriangle));
		addTriangleAction.redo();
		return addTriangleAction;
	}

	@Override
	public UndoAction setParent(final IdObject node) {
		throw new UnsupportedOperationException("This feature is not available in Geoset Vertex mode");
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
