package com.hiveworkshop.rms.ui.application.actions.model;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.ArrayList;
import java.util.Arrays;

public class EditCommentAction implements UndoAction {
	private final String stringOld;
	private final String stringNew;
	private final ModelView modelViewManager;
	private final ModelStructureChangeListener structureChangeListener;

	public EditCommentAction(final String stringOld,
	                         String stringNew,
	                         final ModelView modelViewManager,
	                         final ModelStructureChangeListener modelStructureChangeListener) {
		this.stringOld = stringOld;
		this.stringNew = stringNew;
		this.modelViewManager = modelViewManager;
		this.structureChangeListener = modelStructureChangeListener;
	}

	@Override
	public UndoAction undo() {
		modelViewManager.getModel().setHeader(getCommentContent(stringOld));
		structureChangeListener.headerChanged();
		return this;
	}

	@Override
	public UndoAction redo() {
		modelViewManager.getModel().setHeader(getCommentContent(stringNew));
		structureChangeListener.headerChanged();
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
