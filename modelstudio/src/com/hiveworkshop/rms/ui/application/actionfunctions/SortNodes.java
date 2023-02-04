package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.tools.SortNodesAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.awt.event.KeyEvent;

public class SortNodes extends ActionFunction {
	public SortNodes(){
		super(TextKey.SORT_NODES, SortNodes::sortNodes);
		setMenuItemMnemonic(KeyEvent.VK_S);
	}

	public static void sortNodes(ModelHandler modelHandler) {
		SortNodesAction sortNodesAction = new SortNodesAction(modelHandler.getModel(), ModelStructureChangeListener.changeListener);
		modelHandler.getUndoManager().pushAction(sortNodesAction.redo());
	}
}
