package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.AddSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.RemoveSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SelectionManager<T> implements SelectionView {
//	protected final Set<T> selection = new HashSet<>();
	protected ModelView modelView;
	private final Set<SelectionListener> listeners = new HashSet<>();
	private SelectionItemTypes selectionMode;

	public SelectionManager(ModelView modelView, SelectionItemTypes selectionMode) {
		this.modelView = modelView;
		this.selectionMode = selectionMode;
	}

	protected void fireChangeListeners() {
		for (final SelectionListener listener : listeners) {
			listener.onSelectionChanged(this);
		}
	}

	public Collection<GeosetVertex> getSelectedVertices() {
		return modelView.getSelectedVertices();
	}

	@Override
	public Vec3 getCenter() {
		if(selectionMode == SelectionItemTypes.VERTEX
				|| selectionMode == SelectionItemTypes.FACE
				|| selectionMode == SelectionItemTypes.GROUP
				|| selectionMode == SelectionItemTypes.TPOSE){
			return modelView.getSelectionCenter();
		}
		if(selectionMode == SelectionItemTypes.ANIMATE){
			Vec3 centerOfGroupSumHeap = new Vec3(0, 0, 0);
			for (IdObject object : modelView.getSelectedIdObjects()) {
				Vec4 pivotHeap = new Vec4(object.getPivotPoint(), 1);
				pivotHeap.transform(modelView.getEditorRenderModel().getRenderNode(object).getWorldMatrix());
				centerOfGroupSumHeap.add(pivotHeap.getVec3());
			}
			if (modelView.getSelectedIdObjects().size() > 0) {
				centerOfGroupSumHeap.scale(1f / modelView.getSelectedIdObjects().size());
			}
			return centerOfGroupSumHeap;
		}
		return new Vec3();
	}

	public abstract Set<T> getSelection();

	public abstract List<T> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem);

	public final UndoAction setSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<T> newSelection = genericSelect(min, max, coordinateSystem);

		Set<T> previousSelection = new HashSet<>(getSelection());
		setSelection(newSelection);
		return (new SetSelectionAction<>(newSelection, previousSelection, this, "select"));
	}

	public final UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<T> newSelection = genericSelect(min, max, coordinateSystem);
		Set<T> previousSelection = new HashSet<>(getSelection());
		removeSelection(newSelection);
		return (new RemoveSelectionAction<>(previousSelection, newSelection, this));
	}

	public final UndoAction addSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<T> newSelection = genericSelect(min, max, coordinateSystem);
		Set<T> previousSelection = new HashSet<>(getSelection());
		addSelection(newSelection);
		return new AddSelectionAction<>(previousSelection, newSelection, this);
	}

	public abstract void setSelection(final Collection<? extends T> selectionItem);

	public abstract void addSelection(final Collection<? extends T> selectionItem);

	public abstract void removeSelection(final Collection<? extends T> selectionItem);

	public void addSelectionListener(final SelectionListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(final SelectionListener listener) {
		listeners.remove(listener);
	}

	@Override
	public abstract boolean isEmpty();


	private void ugg(){
		if (selectionMode == SelectionItemTypes.VERTEX){

		}

		if(selectionMode == SelectionItemTypes.FACE){

		}

		if(selectionMode == SelectionItemTypes.CLUSTER){
		}

		if(selectionMode == SelectionItemTypes.GROUP){

		}

		if(selectionMode == SelectionItemTypes.TPOSE){

		}

		if(selectionMode == SelectionItemTypes.ANIMATE){

		}

		if(selectionMode == SelectionItemTypes.VERTEX || selectionMode == SelectionItemTypes.FACE || selectionMode == SelectionItemTypes.GROUP || selectionMode == SelectionItemTypes.TPOSE){

		}
	}
}
