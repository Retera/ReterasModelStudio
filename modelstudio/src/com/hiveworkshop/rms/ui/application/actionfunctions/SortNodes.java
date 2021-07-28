package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SortNodes extends ActionFunction {
	public SortNodes(){
		super(TextKey.SORT_NODES, () -> sortNodes());
		setMenuItemMnemonic(KeyEvent.VK_S);
	}

	public static void sortNodes() {
		EditableModel model = ProgramGlobals.getCurrentModelPanel().getModel();
		List<IdObject> roots = new ArrayList<>();
		List<IdObject> modelList = model.getIdObjects();
		for (IdObject object : modelList) {
			if (object.getParent() == null) {
				roots.add(object);
			}
		}
		Queue<IdObject> bfsQueue = new LinkedList<>(roots);
		List<IdObject> result = new ArrayList<>();
		while (!bfsQueue.isEmpty()) {
			IdObject nextItem = bfsQueue.poll();
			bfsQueue.addAll(nextItem.getChildrenNodes());
			result.add(nextItem);
		}
		model.clearAllIdObjects();
//		for (IdObject node : result) {
//			model.remove(node);
//		}
		ModelStructureChangeListener.changeListener.nodesUpdated();
		for (IdObject node : result) {
			model.add(node);
		}
		ModelStructureChangeListener.changeListener.nodesUpdated();
	}
}
