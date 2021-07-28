package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.tools.CloneAction2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

public class Duplicate extends ActionFunction {

	public Duplicate(){
		super(TextKey.DUPLICATE, () -> cloneActionRes(), "control D");
	}




	public static void cloneActionRes() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			try {
				ModelView modelView = modelPanel.getModelView();
				CloneAction2 cloneAction = new CloneAction2(modelView, ModelStructureChangeListener.changeListener, modelView.getSelectedVertices(), modelView.getSelectedIdObjects(), modelView.getSelectedCameras());
				modelPanel.getUndoManager().pushAction(cloneAction.redo());
			} catch (final Exception exc) {
				ExceptionPopup.display(exc);
			}
			ProgramGlobals.getUndoHandler().refreshUndo();
			ProgramGlobals.getMainPanel().repaintSelfAndChildren();
			modelPanel.repaintSelfAndRelatedChildren();
		}
	}
}
