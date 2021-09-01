package com.hiveworkshop.rms.editor.actions.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.Arrays;

public class EditCommentAction implements UndoAction {
	private final String stringOld;
	private final String stringNew;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;

	public EditCommentAction(String stringOld, String stringNew, EditableModel model,
	                         ModelStructureChangeListener changeListener) {
		this.stringOld = stringOld;
		this.stringNew = stringNew;
		this.model = model;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.setHeader(getCommentContent(stringOld));
		if (changeListener != null) {
			changeListener.headerChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.setHeader(getCommentContent(stringNew));
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
