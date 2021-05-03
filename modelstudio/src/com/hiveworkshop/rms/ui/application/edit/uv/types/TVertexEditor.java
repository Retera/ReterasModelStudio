package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.actions.uv.UVRemapAction;
import com.hiveworkshop.rms.ui.application.actions.uv.UVSnapAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.AddSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.RemoveSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ComponentVisibilityListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.*;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

/**
 * So, in some ideal future this would be an implementation of the ModelEditor
 * interface, I believe, and the editor would be operating on an interface who
 * could capture clicks and convert them into 2D operations regardless of
 * whether the underlying thing being editor was UV or Mesh.
 *
 * It isn't like that right now, though, so this is just going to be a 2D copy pasta.
 */
public abstract class TVertexEditor<T> implements ComponentVisibilityListener {
	protected final SelectionManager<T> selectionManager;
	protected final ModelView model;
	protected final VertexSelectionHelper vertexSelectionHelper;
	protected final ModelStructureChangeListener structureChangeListener;
	protected int uvLayerIndex;

	public TVertexEditor(SelectionManager<T> selectionManager, ModelView model, ModelStructureChangeListener structureChangeListener) {
		this.selectionManager = selectionManager;
		this.model = model;
		this.structureChangeListener = structureChangeListener;
		vertexSelectionHelper = this::selectByVertices;
	}

	public abstract UndoAction expandSelection();

	public abstract UndoAction invertSelection();

	public abstract UndoAction selectAll();

	public abstract void selectByVertices(Collection<? extends Vec3> newSelection);

	public abstract boolean canSelectAt(Point point, CoordinateSystem axes);

	public final UndoAction setSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem) {
		java.util.List<T> newSelection = genericSelect(region, coordinateSystem);
		return setSelectionWithAction(newSelection);
	}

	public final UndoAction removeSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem) {
		java.util.List<T> newSelection = genericSelect(region, coordinateSystem);
		return removeSelectionWithAction(newSelection);
	}

	public final UndoAction addSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem) {
		java.util.List<T> newSelection = genericSelect(region, coordinateSystem);
		return addSelectionWithAction(newSelection);
	}

	protected final UndoAction setSelectionWithAction(java.util.List<T> newSelection) {
		Set<T> previousSelection = new HashSet<>(selectionManager.getSelection());
		selectionManager.setSelection(newSelection);
		return new SetSelectionAction<>(newSelection, previousSelection, selectionManager, "select");
	}

	protected final UndoAction removeSelectionWithAction(java.util.List<T> newSelection) {
		Set<T> previousSelection = new HashSet<>(selectionManager.getSelection());
		selectionManager.removeSelection(newSelection);
		return new RemoveSelectionAction<>(previousSelection, newSelection, selectionManager);
	}

	protected final UndoAction addSelectionWithAction(java.util.List<T> newSelection) {
		Set<T> previousSelection = new HashSet<>(selectionManager.getSelection());
		selectionManager.addSelection(newSelection);
		return new AddSelectionAction<>(previousSelection, newSelection, selectionManager);
	}

	protected abstract java.util.List<T> genericSelect(Rectangle2D region, CoordinateSystem coordinateSystem);

	protected abstract UndoAction buildHideComponentAction(java.util.List<? extends SelectableComponent> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable);

	@Override
	public UndoAction hideComponent(java.util.List<? extends SelectableComponent> selectableComponent, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		UndoAction hideComponentAction = buildHideComponentAction(selectableComponent, editabilityToggleHandler,
				refreshGUIRunnable);
		hideComponentAction.redo();
		return hideComponentAction;
	}

	@Override
	public UndoAction showComponent(EditabilityToggleHandler editabilityToggleHandler) {
		editabilityToggleHandler.makeEditable();
		return new MakeEditableAction(editabilityToggleHandler);
	}

	public UndoAction mirror(byte dim, double centerX, double centerY) {
		MirrorTVerticesAction mirror = new MirrorTVerticesAction(TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex), dim, centerX, centerY);
		// super weird passing of currently editable id Objects, works because mirror action checks selected vertices against pivot points from this list
		mirror.redo();
		return mirror;
	}

	public UndoAction remap(byte xDim, byte yDim, UVPanel.UnwrapDirection unwrapDirection) {
		java.util.List<Vec2> tVertices = new ArrayList<>();
		java.util.List<Vec2> newValueHolders = new ArrayList<>();
		java.util.List<Vec2> oldValueHolders = new ArrayList<>();

		Vec2 min = new Vec2(Float.MAX_VALUE, Float.MAX_VALUE);
		Vec2 max = new Vec2(Float.MIN_VALUE, Float.MIN_VALUE);

		for (Vec3 vertex : selectionManager.getSelectedVertices()) {
			if (vertex instanceof GeosetVertex) {
				GeosetVertex geosetVertex = (GeosetVertex) vertex;
				if (uvLayerIndex < geosetVertex.getTverts().size()) {
					Vec2 modelDataTVertex = geosetVertex.getTVertex(uvLayerIndex);
					tVertices.add(modelDataTVertex);
					oldValueHolders.add(new Vec2(modelDataTVertex.x, modelDataTVertex.y));
					Vec2 newCoordValue = new Vec2(vertex.getCoord(xDim), vertex.getCoord(yDim));

					max.set(Math.max(max.x, newCoordValue.x), Math.max(max.y, newCoordValue.y));
					min.set(Math.min(min.x, newCoordValue.x), Math.min(min.y, newCoordValue.y));

					newValueHolders.add(newCoordValue);
				}
			}
		}
		Vec2 width = Vec2.getDif(max, min);

		if (width.x == 0) {
			width.x = 0.01f;
		}
		if (width.y == 0) {
			width.y = 0.01f;
		}
		for (Vec2 tv : newValueHolders) {
			tv.sub(min).div(width);
		}
		UVRemapAction uvRemapAction = new UVRemapAction(tVertices, newValueHolders, oldValueHolders, unwrapDirection);
		uvRemapAction.redo();
		return uvRemapAction;
	}

	public UndoAction snapSelectedVertices() {
		Collection<? extends Vec2> selection = TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex);
		List<Vec2> oldLocations = new ArrayList<>();
		Vec2 cog = Vec2.centerOfGroup(selection);
		for (Vec2 vertex : selection) {
			oldLocations.add(new Vec2(vertex));
		}
		UVSnapAction temp = new UVSnapAction(selection, oldLocations, cog);
		temp.redo();
		return temp;
	}

	public void rawTranslate(double x, double y) {
		for (Vec2 vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.translate(x, y);
		}
	}

	public void rawScale(double centerX, double centerY,
	                     double scaleX, double scaleY) {
		for (Vec2 vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.scale(centerX, centerY, scaleX, scaleY);
		}
	}

	public void rawRotate2d(double centerX, double centerY,
	                        double radians,
	                        byte firstXYZ, byte secondXYZ) {
		for (Vec2 vertex : TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex)) {
			vertex.rotate(centerX, centerY, radians, firstXYZ, secondXYZ);
		}
	}

	public UndoAction translate(double x, double y) {
		Vec2 delta = new Vec2(x, y);
		StaticMeshUVMoveAction moveAction = new StaticMeshUVMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	public UndoAction setPosition(Vec2 center, double x, double y) {
		Vec2 delta = new Vec2(x - center.x, y - center.y);
		StaticMeshUVMoveAction moveAction = new StaticMeshUVMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	public UndoAction rotate(Vec2 center, double rotateRadians) {
		SimpleRotateUVAction compoundAction = new SimpleRotateUVAction(this, center, rotateRadians);
		compoundAction.redo();
		return compoundAction;
	}

	public Vec2 getSelectionCenter() {
//		return selectionManager.getCenter();
		Set<Vec2> tvertices = new HashSet<>(TVertexUtils.getTVertices(selectionManager.getSelectedVertices(), uvLayerIndex));
		return Vec2.centerOfGroup(tvertices); // TODO is this correct?
	}

	public UndoAction selectFromViewer(SelectionView viewerSelectionView) {
		Set<T> previousSelection = selectionManager.getSelection();
		selectByVertices(viewerSelectionView.getSelectedVertices());
		SetSelectionAction<T> setSelectionAction = new SetSelectionAction<>(selectionManager.getSelection(), previousSelection, selectionManager, "select UV from viewer");
		return setSelectionAction;
	}

	public GenericMoveAction beginTranslation() {
		return new StaticMeshUVMoveAction(this, Vec2.ORIGIN);
	}

	public GenericRotateAction beginRotation(double centerX, double centerY,
	                                         byte dim1, byte dim2) {
		return new StaticMeshUVRotateAction(this, new Vec2(centerX, centerY), dim1, dim2);
	}

	public GenericScaleAction beginScaling(double centerX, double centerY) {
		return new StaticMeshUVScaleAction(this, centerX, centerY);
	}

	public int getUVLayerIndex() {
		return uvLayerIndex;
	}

	public void setUVLayerIndex(int uvLayerIndex) {
		this.uvLayerIndex = uvLayerIndex;
		// TODO deselect vertices with no such layer
	}

}
