package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;

public class ModelEditorManager extends AbstractModelEditorManager {
	public static boolean MOVE_LINKED;
	AbstractModelEditor abstractModelEditor;
	NodeAnimationModelEditor nodeAnimationModelEditor;

	public ModelEditorManager(ModelHandler modelHandler,
	                          ModelEditorChangeNotifier changeNotifier,
	                          SelectionListener selectionListener) {
		super(modelHandler, changeNotifier, selectionListener);
		selectionManager = new SelectionManager(modelHandler.getModelView(), MOVE_LINKED, SelectionItemTypes.VERTEX);
		abstractModelEditor = new AbstractModelEditor((SelectionManager) selectionManager, modelHandler, SelectionItemTypes.VERTEX);
		nodeAnimationModelEditor = new NodeAnimationModelEditor((SelectionManager) selectionManager, modelHandler, SelectionItemTypes.ANIMATE);
		setSelectionItemType(SelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(SelectionItemTypes selectionMode) {
		selectionManager.setSelectionMode(selectionMode);
		switch (selectionMode) {
			case VERTEX, FACE, GROUP, CLUSTER, TPOSE -> modelEditor = abstractModelEditor.setSelectionMode(selectionMode);
			case ANIMATE -> modelEditor = nodeAnimationModelEditor;
		}

		selectionListener.onSelectionChanged(selectionManager);
		viewportSelectionHandler.setSelectionManager(selectionManager);
		changeNotifier.modelEditorChanged(modelEditor);
	}
}
