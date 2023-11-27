package com.hiveworkshop.rms.editor.actions.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.List;

public class EditCommentAction implements UndoAction {
	private final List<String> stringsOld;
	private final List<String> stringsNew;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;

	public EditCommentAction(List<String> stringsNew, EditableModel model,
	                         ModelStructureChangeListener changeListener) {
		this.stringsOld = new ArrayList<>(model.getComments());
		this.stringsNew = stringsNew;
		this.model = model;
		this.changeListener = changeListener;
	}

	@Override
	public EditCommentAction undo() {
		model.clearComments();
		for (String s : stringsOld) {
			model.addComment(s);
		}
		if (changeListener != null) {
			changeListener.headerChanged();
		}
		return this;
	}

	@Override
	public EditCommentAction redo() {
		model.clearComments();
		for (String s : stringsNew) {
			model.addComment(s);
		}
		if (changeListener != null) {
			changeListener.headerChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Edit Comment";
	}

}
