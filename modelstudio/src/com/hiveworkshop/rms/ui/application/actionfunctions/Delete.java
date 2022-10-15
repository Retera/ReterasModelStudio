package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.DeleteAction;
import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.util.Arrays;

public class Delete extends ActionFunction {
	public Delete(){
		super(TextKey.DELETE, () -> deleteActionRes(), "DELETE");
	}

	public static void deleteActionRes() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			if (ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
//				ProgramGlobals.getMainPanel().getMainLayoutCreator().getTimeSliderView().getTimeSliderPanel().getKeyframeHandler().deleteSelectedKeyframes();
				ProgramGlobals.getRootWindowUgg().getWindowHandler2().getTimeSliderView().getTimeSliderPanel().getKeyframeHandler().deleteSelectedKeyframes();
			} else {
				ModelView modelView = modelPanel.getModelView();
				DeleteAction deleteAction = new DeleteAction(modelView.getSelectedVertices(), modelView, ProgramGlobals.getSelectionItemType() == SelectionItemTypes.FACE, ModelStructureChangeListener.changeListener);
				DeleteNodesAction deleteNodesAction = new DeleteNodesAction(modelView.getSelectedIdObjects(), modelView.getSelectedCameras(), ModelStructureChangeListener.changeListener, modelView.getModel());
				CompoundAction compoundAction = new CompoundAction("deleted components", Arrays.asList(deleteAction, deleteNodesAction));
				compoundAction.redo();
				modelPanel.getUndoManager().pushAction(compoundAction);
			}
			ProgramGlobals.getMainPanel().repaintSelfAndChildren();
			modelPanel.repaintSelfAndRelatedChildren();
		}
	}
}
