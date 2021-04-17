package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.editor.model.IdObject;

public final class SetParentAction implements UndoAction {
	private final Map<IdObject, IdObject> nodeToOldParent;
	private final IdObject newParent;
	private final ModelStructureChangeListener changeListener;
	private final List<IdObject> nodes;

	public SetParentAction(final Map<IdObject, IdObject> nodeToOldParent, final IdObject newParent,
			final ModelStructureChangeListener changeListener) {
		this.nodeToOldParent = nodeToOldParent;
		this.newParent = newParent;
		this.changeListener = changeListener;
		this.nodes = new ArrayList<>();
		for (final Map.Entry<IdObject, IdObject> entry : nodeToOldParent.entrySet()) {
			nodes.add(entry.getKey());
		}
	}

	@Override
	public void undo() {
		for (final Map.Entry<IdObject, IdObject> entry : nodeToOldParent.entrySet()) {
			entry.getKey().setParent(entry.getValue());
		}
		changeListener.nodesRemoved(nodes);
		changeListener.nodesAdded(nodes);
	}

	@Override
	public void redo() {
		for (final Map.Entry<IdObject, IdObject> entry : nodeToOldParent.entrySet()) {
			entry.getKey().setParent(newParent);
		}
		changeListener.nodesRemoved(nodes);
		changeListener.nodesAdded(nodes);
	}

	@Override
	public String actionName() {
		return "re-assign matrix";
	}

}
