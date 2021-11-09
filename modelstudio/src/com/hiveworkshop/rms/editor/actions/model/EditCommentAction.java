package com.hiveworkshop.rms.editor.actions.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditCommentAction implements UndoAction {
	private final List<String> stringsOld;
	private final List<String> stringsNew;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;

	public EditCommentAction(String stringsNew, EditableModel model,
	                         ModelStructureChangeListener changeListener) {
		this.stringsOld = new ArrayList<>(model.getComments());
		this.stringsNew = Arrays.asList(stringsNew.split("\n"));
		this.model = model;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
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
	public UndoAction redo() {
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

	public ArrayList<String> getCommentContent(String string) {
		String[] text = string.split("\n");
		return new ArrayList<>(Arrays.asList(text));
	}

}
