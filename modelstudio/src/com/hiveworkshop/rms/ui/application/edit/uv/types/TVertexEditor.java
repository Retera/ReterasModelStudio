package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.actions.uv.UVRemapAction;
import com.hiveworkshop.rms.ui.application.actions.uv.UVSnapAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.AddSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.RemoveSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ComponentVisibilityListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.MirrorTVerticesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.StaticMeshUVMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.StaticMeshUVRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.actions.StaticMeshUVScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

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

	public abstract boolean canSelectAt(Vec2 point, CoordinateSystem axes);

	public final UndoAction addSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<T> newSelection = genericSelect(min, max, coordinateSystem);
		return addSelectionWithAction(newSelection);
	}

	public final UndoAction setSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<T> newSelection = genericSelect(min, max, coordinateSystem);
		return setSelectionWithAction(newSelection);
	}

	public final UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<T> newSelection = genericSelect(min, max, coordinateSystem);
		return removeSelectionWithAction(newSelection);
	}

	protected final UndoAction setSelectionWithAction(List<T> newSelection) {
		Set<T> previousSelection = new HashSet<>(selectionManager.getSelection());
		selectionManager.setSelection(newSelection);
		return new SetSelectionAction<>(newSelection, previousSelection, selectionManager, "select");
	}

	protected final UndoAction removeSelectionWithAction(List<T> newSelection) {
		Set<T> previousSelection = new HashSet<>(selectionManager.getSelection());
		selectionManager.removeSelection(newSelection);
		return new RemoveSelectionAction<>(previousSelection, newSelection, selectionManager);
	}

	protected final UndoAction addSelectionWithAction(List<T> newSelection) {
		Set<T> previousSelection = new HashSet<>(selectionManager.getSelection());
		selectionManager.addSelection(newSelection);
		return new AddSelectionAction<>(previousSelection, newSelection, selectionManager);
	}

	protected abstract List<T> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem);

	protected abstract UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement<?>> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable);

	@Override
	public UndoAction hideComponent(List<? extends CheckableDisplayElement<?>> selectableComponent, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		UndoAction hideComponentAction = buildHideComponentAction(selectableComponent, editabilityToggleHandler, refreshGUIRunnable);
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
		UVRemapAction uvRemapAction = new UVRemapAction(selectionManager.getSelectedVertices(), uvLayerIndex, xDim, yDim, unwrapDirection);
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
		return new StaticMeshUVMoveAction(selectionManager.getSelectedVertices(), uvLayerIndex, Vec2.ORIGIN);
	}

	public GenericRotateAction beginRotation(Vec2 center, byte dim1, byte dim2) {
		return new StaticMeshUVRotateAction(selectionManager.getSelectedVertices(), uvLayerIndex, center, dim1, dim2);
	}

	public GenericScaleAction beginScaling(Vec2 center) {
		return new StaticMeshUVScaleAction(selectionManager.getSelectedVertices(), uvLayerIndex, center);
	}

	public int getUVLayerIndex() {
		return uvLayerIndex;
	}

	public void setUVLayerIndex(int uvLayerIndex) {
		this.uvLayerIndex = uvLayerIndex;
		// TODO deselect vertices with no such layer
	}

}
