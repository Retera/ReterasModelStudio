package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.Collection;

public class ShowHideMultipleAction implements UndoAction {
	private Collection<Object> objects;
	private boolean visible;
	private ModelView modelView;
	private ModelStructureChangeListener changeListener;

	public ShowHideMultipleAction(Collection<Object> objects, boolean visible, ModelView modelView, ModelStructureChangeListener changeListener) {
		this.objects = objects;
		this.visible = visible;
		this.modelView = modelView;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		for (Object object : objects) {
			modelView.makeVisible(object, !visible);
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Object object : objects) {
			modelView.makeVisible(object, visible);
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		String showHide = visible ? "show " : "hide ";
		return showHide + objects.size() + " objects";
	}
}
