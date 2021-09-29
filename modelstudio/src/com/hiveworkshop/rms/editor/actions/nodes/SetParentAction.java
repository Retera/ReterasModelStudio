package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.*;

public final class SetParentAction implements UndoAction {
	private final Map<IdObject, IdObject> nodeToOldParent;
	private final IdObject newParent;
	private final ModelStructureChangeListener changeListener;
	private final Set<IdObject> nodes;

	public SetParentAction(Collection<IdObject> nodes, IdObject newParent, ModelStructureChangeListener changeListener) {
		this.nodeToOldParent = new HashMap<>();
		for (IdObject idObject : nodes) {
			nodeToOldParent.put(idObject, idObject.getParent());
		}

		this.newParent = newParent;
		this.changeListener = changeListener;
		this.nodes = new HashSet<>(nodes);
	}

	public SetParentAction(IdObject node, IdObject newParent, ModelStructureChangeListener changeListener) {
		this(Collections.singleton(node), newParent, changeListener);
	}

	@Override
	public UndoAction undo() {
		for (IdObject idObject : nodeToOldParent.keySet()) {
			idObject.setParent(nodeToOldParent.get(idObject));
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (IdObject idObject : nodes) {
			idObject.setParent(newParent);
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "re-assign matrix";
	}

}
