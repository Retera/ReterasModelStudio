package com.hiveworkshop.rms.ui.application.edit.mesh.types.geosetvertex;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.ui.application.actions.mesh.SplitGeosetAction;
import com.hiveworkshop.rms.ui.application.actions.mesh.TeamColorAddAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.DrawVertexAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.NewGeosetAction;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.AddTriangleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class GeosetVertexModelEditor extends AbstractModelEditor<GeosetVertex> {
	private final ProgramPreferences programPreferences;

	public GeosetVertexModelEditor(ModelView model, ProgramPreferences programPreferences,
	                               SelectionManager<GeosetVertex> selectionManager,
	                               ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		List<GeosetVertex> newGeosetVertices = new ArrayList<>();
		for (Geoset geoset : model.getEditableGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				if (newSelection.contains(vertex)) {
					newGeosetVertices.add(vertex);
				}
			}
		}
		selectionManager.setSelection(newGeosetVertices);
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
	public UndoAction expandSelection() {
		Set<GeosetVertex> expandedSelection = new HashSet<>(selectionManager.getSelection());
		List<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		for (GeosetVertex v : oldSelection) {
			expandSelection(v, expandedSelection);
		}
		selectionManager.setSelection(expandedSelection);
		return (new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection"));
	}

	private void expandSelection(GeosetVertex currentVertex, Set<GeosetVertex> selection) {
		selection.add(currentVertex);
		for (Triangle tri : currentVertex.getTriangles()) {
			for (final GeosetVertex other : tri.getVerts()) {
				if (!selection.contains(other)) {
					expandSelection(other, selection);
				}
			}
		}
	}

	@Override
	public UndoAction invertSelection() {
		List<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		Set<GeosetVertex> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (Geoset geo : model.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geo.getVertices()) {
				toggleSelection(invertedSelection, geosetVertex);
			}
		}
		selectionManager.setSelection(invertedSelection);
		return (new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection"));
	}

	private void toggleSelection(Set<GeosetVertex> selection, GeosetVertex position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public UndoAction selectAll() {
		List<GeosetVertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		Set<GeosetVertex> allSelection = new HashSet<>();
		for (Geoset geo : model.getEditableGeosets()) {
			allSelection.addAll(geo.getVertices());
		}
		selectionManager.setSelection(allSelection);
		return (new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all"));
	}

	@Override
	protected List<GeosetVertex> genericSelect(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<GeosetVertex> selectedItems = new ArrayList<>();

		Rectangle2D area = getArea(region);

		for (Geoset geoset : model.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (hitTest(area, geosetVertex, coordinateSystem, programPreferences.getVertexSize()))
					selectedItems.add(geosetVertex);
			}
		}
		return selectedItems;
	}

	@Override
	public UndoAction addTeamColor() {
		TeamColorAddAction<GeosetVertex> teamColorAddAction = new TeamColorAddAction<>(selectionManager.getSelectedFaces(), model.getModel(), structureChangeListener, selectionManager, vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public UndoAction splitGeoset() {
		SplitGeosetAction<GeosetVertex> teamColorAddAction = new SplitGeosetAction<>(selectionManager.getSelectedFaces(), model.getModel(), structureChangeListener, selectionManager, vertexSelectionHelper);
		teamColorAddAction.redo();
		return teamColorAddAction;
	}

	@Override
	public boolean canSelectAt(Point point, CoordinateSystem axes) {
		boolean canSelect = false;
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
	protected UndoAction buildHideComponentAction(List<? extends SelectableComponent> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<GeosetVertex> previousSelection = new ArrayList<>(selectionManager.getSelection());
		List<GeosetVertex> possibleVerticesToTruncate = new ArrayList<>();
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
					possibleVerticesToTruncate.addAll(geoset.getVertices());
				}
			});
		}
		Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(possibleVerticesToTruncate);
		Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	@Override
	public CopiedModelData copySelection() {
		Set<GeosetVertex> selection = selectionManager.getSelection();
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
					if (selection.contains(geosetVertex)) {
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
	public UndoAction addVertex(double x, double y, double z, Vec3 preferredNormalFacingVector) {
		List<Geoset> geosets = model.getModel().getGeosets();
		Geoset solidWhiteGeoset = null;
		for (Geoset geoset : geosets) {
			Layer firstLayer = geoset.getMaterial().firstLayer();
			if ((geoset.getMaterial() != null)
					&& (firstLayer != null)
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
		GeosetVertex geosetVertex = new GeosetVertex(x, y, z, new Vec3(preferredNormalFacingVector.x, preferredNormalFacingVector.y, preferredNormalFacingVector.z));
		geosetVertex.setGeoset(solidWhiteGeoset);
		geosetVertex.addTVertex(new Vec2(0, 0));
		UndoAction action;
		DrawVertexAction drawVertexAction = new DrawVertexAction(geosetVertex);
		if (needsGeosetAction) {
			NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, model.getModel(), structureChangeListener);
			action = new CompoundAction("add vertex", Arrays.asList(newGeosetAction, drawVertexAction));
		} else {
			action = drawVertexAction;
		}
		action.redo();
		return action;
	}

	@Override
	public UndoAction createFaceFromSelection(Vec3 preferredFacingVector) {
		Set<GeosetVertex> selection = selectionManager.getSelection();
		if (selection.size() != 3) {
			throw new FaceCreationException(
					"A face can only be created from exactly 3 vertices (you have " + selection.size() + " selected)");
		}
		int index = 0;
		GeosetVertex[] verticesArray = new GeosetVertex[3];
		Geoset geoset = null;
		for (GeosetVertex vertex : selection) {
			verticesArray[index++] = vertex;
			if (geoset == null) {
				geoset = vertex.getGeoset();
			} else if (geoset != vertex.getGeoset()) {
				throw new FaceCreationException(
						"All three vertices to create a face must be a part of the same Geoset");
			}
		}
		for (Triangle existingTriangle : verticesArray[0].getTriangles()) {
			if (existingTriangle.contains(verticesArray[0])
					&& existingTriangle.contains(verticesArray[1])
					&& existingTriangle.contains(verticesArray[2])) {
				throw new FaceCreationException("Triangle already exists");
			}
		}

		Triangle newTriangle = new Triangle(verticesArray[0], verticesArray[1], verticesArray[2], geoset);
		Vec3 facingVector = newTriangle.getNormal();
		double cosine = facingVector.dot(preferredFacingVector) / (facingVector.length() * preferredFacingVector.length());
		if (cosine < 0) {
			newTriangle.flip(false);
		}

		AddTriangleAction addTriangleAction = new AddTriangleAction(geoset, Collections.singletonList(newTriangle));
		addTriangleAction.redo();
		return addTriangleAction;
	}

	@Override
	public UndoAction setParent(IdObject node) {
		throw new UnsupportedOperationException("This feature is not available in Geoset Vertex mode");
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
