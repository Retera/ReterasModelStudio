package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;

public class ModelEditorManager extends AbstractModelEditorManager {
	public static boolean MOVE_LINKED;
	GeometryModelEditor geometryModelEditor;
	NodeAnimationModelEditor nodeAnimationModelEditor;
	TPoseModelEditor tPoseModelEditor;

	public ModelEditorManager(ModelHandler modelHandler) {
		super(modelHandler);
		selectionManager = new SelectionManager(modelHandler.getRenderModel(), modelHandler.getModelView(), MOVE_LINKED, SelectionItemTypes.VERTEX);
		geometryModelEditor = new GeometryModelEditor((SelectionManager) selectionManager, modelHandler);
		nodeAnimationModelEditor = new NodeAnimationModelEditor((SelectionManager) selectionManager, modelHandler);
		tPoseModelEditor = new TPoseModelEditor((SelectionManager) selectionManager, modelHandler);
		setSelectionItemType(SelectionItemTypes.VERTEX);
	}

	public void setSelectionItemType(SelectionItemTypes selectionMode) {
		selectionManager.setSelectionMode(selectionMode);
		switch (selectionMode) {
//			case VERTEX, FACE, GROUP, CLUSTER -> modelEditor = geometryModelEditor.setSelectionMode(selectionMode);
			case VERTEX, FACE, GROUP, CLUSTER -> modelEditor = geometryModelEditor;
			case TPOSE -> modelEditor = tPoseModelEditor;
			case ANIMATE -> modelEditor = nodeAnimationModelEditor;
		}

		if(selectionListener != null){
			selectionListener.onSelectionChanged(selectionManager);
		}
		changeNotifier.modelEditorChanged(modelEditor);
	}
}
