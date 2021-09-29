package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec3;

public class SetPivotAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final IdObject node;
	private final Vec3 oldPivot;
	private final Vec3 newPivot;

	public SetPivotAction(IdObject node, Vec3 newPivot, ModelStructureChangeListener changeListener){
		this.changeListener = changeListener;
		this.node = node;
		this.oldPivot = node.getPivotPoint();
		this.newPivot = newPivot;
	}

	@Override
	public UndoAction undo() {
		node.setPivotPoint(oldPivot);
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		node.setPivotPoint(newPivot);
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Set pivot for " + node.getName();
	}
}
