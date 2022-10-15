package com.hiveworkshop.rms.editor.actions.selection;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SetEditableMultipleAction implements UndoAction {
	private final boolean editable;
	private final Map<Object, Boolean> oldEd = new HashMap<>();
	private final ModelView modelView;
	private final ModelStructureChangeListener changeListener;
	private final String actionName;

	public SetEditableMultipleAction(Collection<?> objects, boolean editable, ModelView modelView, ModelStructureChangeListener changeListener) {
		this.editable = editable;
		this.modelView = modelView;
		this.changeListener = changeListener;

		for(Object object : objects){
			oldEd.put(object, modelView.isInEditable(object));
		}

		String edNotEd = editable ? " editable" : " not editable";
		if(objects.size() == 1){
			Object object = objects.stream().findFirst().get();
			if (object instanceof Named) {
				actionName = "make " + ((Named) object).getName() + edNotEd;
			} else {
				actionName = "make " + "object" + edNotEd;
			}
		} else {
			actionName = "make " + objects.size() + " objects" + edNotEd;
		}
	}

	@Override
	public UndoAction undo() {
		for (Object object : oldEd.keySet()) {
			modelView.makeEditable(object, oldEd.get(object));
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Object object : oldEd.keySet()) {
			modelView.makeEditable(object, editable);
		}
		if (changeListener != null) {
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}
}
