package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Attachment;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetAttachmentPathAction implements UndoAction {
	private final Attachment attachment;
	private final String prevPath;
	private final String newPath;
	private final ModelStructureChangeListener changeListener;

	public SetAttachmentPathAction(Attachment attachment, String newPath, ModelStructureChangeListener changeListener) {
		this.attachment = attachment;
		this.prevPath = attachment.getPath();
		this.newPath = newPath;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		attachment.setPath(prevPath);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		attachment.setPath(newPath);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change texture Path";
	}
}
