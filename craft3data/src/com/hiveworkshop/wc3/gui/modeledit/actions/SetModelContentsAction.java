package com.hiveworkshop.wc3.gui.modeledit.actions;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.util.Callback;

public class SetModelContentsAction implements UndoAction {
	private final EditableModel newModel;
	private final EditableModel oldModel;
	private final Callback<EditableModel> modelSwitchingCallback;

	public SetModelContentsAction(final EditableModel newModel, final EditableModel oldModel,
			final Callback<EditableModel> modelSwitchingCallback) {
		this.newModel = newModel;
		this.oldModel = oldModel;
		this.modelSwitchingCallback = modelSwitchingCallback;
	}

	@Override
	public void undo() {
		modelSwitchingCallback.run(oldModel);
	}

	@Override
	public void redo() {
		modelSwitchingCallback.run(newModel);
	}

	@Override
	public String actionName() {
		return "Apply edits from text";
	}

}
