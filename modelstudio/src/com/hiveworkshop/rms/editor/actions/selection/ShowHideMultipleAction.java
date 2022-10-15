package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShowHideMultipleAction implements UndoAction {
	private final boolean visible;
	private final Map<Object, Boolean> oldVis = new HashMap<>();
	private final ModelView modelView;
	private final ModelStructureChangeListener changeListener;
	private final String actionName;

	public ShowHideMultipleAction(Collection<Object> objects, boolean visible, ModelView modelView, ModelStructureChangeListener changeListener) {
		this.visible = visible;
		this.modelView = modelView;
		this.changeListener = changeListener;

		for(Object object : objects){
			oldVis.put(object, modelView.isInVisible(object));
		}

		String showHide = visible ? "show " : "hide ";
		if(objects.size() == 1){
			Object object = objects.stream().findFirst().get();
			if (object instanceof Named) {
				actionName = showHide + ((Named) object).getName();
			} else {
				actionName = showHide + "object";
			}
		} else {
			actionName = showHide + objects.size() + " objects";
		}
	}

	@Override
	public UndoAction undo() {
		for (Object object : oldVis.keySet()) {
			modelView.makeVisible(object, oldVis.get(object));
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Object object : oldVis.keySet()) {
			modelView.makeVisible(object, visible);
		}
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
