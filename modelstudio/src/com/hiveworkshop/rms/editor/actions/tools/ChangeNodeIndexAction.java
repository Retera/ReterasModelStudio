package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.List;

public class ChangeNodeIndexAction implements UndoAction {
	private final IdObject node;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;
	private final int orgIndex;
	private final int newIndex;
	private final IdObject parent;

	public ChangeNodeIndexAction(IdObject node, int adjustment, EditableModel model, ModelStructureChangeListener changeListener) {
		this.node = node;
		this.model = model;
		this.changeListener = changeListener;

		parent = node.getParent();
		if(parent != null) {
			List<IdObject> childrenNodes = parent.getChildrenNodes();
			orgIndex = childrenNodes.indexOf(node);
			newIndex = Math.max(0, Math.min(childrenNodes.size()-1, orgIndex + adjustment));
		} else {
			List<? extends IdObject> list = model.listForIdObjects(node.getClass());
			orgIndex = list.indexOf(node);
			newIndex = Math.max(0, Math.min(list.size()-1, orgIndex + adjustment));
		}
	}

	@Override
	public ChangeNodeIndexAction undo() {
		if(parent != null){
			List<IdObject> childrenNodes = parent.getChildrenNodes();
			childrenNodes.remove(node);
			childrenNodes.add(orgIndex, node);
		} else {
			model.remove(node);
			model.add(node, orgIndex);
		}

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public ChangeNodeIndexAction redo() {
		if(parent != null){
			List<IdObject> childrenNodes = parent.getChildrenNodes();
			childrenNodes.remove(node);
			childrenNodes.add(newIndex, node);
		} else {
			model.remove(node);
			model.add(node, newIndex);
		}

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Change Node Index";
	}
}
