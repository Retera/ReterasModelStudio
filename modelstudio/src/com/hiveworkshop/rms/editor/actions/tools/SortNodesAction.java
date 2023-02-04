package com.hiveworkshop.rms.editor.actions.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SortNodesAction implements UndoAction {
	private final List<IdObject> orgList;
	private final List<IdObject> sortedList;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;

	public SortNodesAction(EditableModel model, ModelStructureChangeListener changeListener){
		this.model = model;
		this.changeListener = changeListener;
		orgList = new ArrayList<>(model.getIdObjects());

		List<IdObject> roots = new ArrayList<>();
		for (IdObject object : model.getIdObjects()) {
			if (object.getParent() == null) {
				roots.add(object);
			}
		}

		Queue<IdObject> bfsQueue = new LinkedList<>(roots);
		sortedList = new ArrayList<>();
		while (!bfsQueue.isEmpty()) {
			IdObject nextItem = bfsQueue.poll();
			bfsQueue.addAll(nextItem.getChildrenNodes());
			sortedList.add(nextItem);
		}

	}

	@Override
	public SortNodesAction undo() {
		model.clearAllIdObjects();
		for (IdObject node : orgList) {
			model.add(node);
		}
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public SortNodesAction redo() {
		model.clearAllIdObjects();
		for (IdObject node : sortedList) {
			model.add(node);
		}
		model.sortIdObjects();
		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Sort Nodes";
	}
}
