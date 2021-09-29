package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.FlipFacesAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.awt.event.KeyEvent;

public class FlipFaces extends ActionFunction {
	public FlipFaces(){
		super(TextKey.FLIP_FACES, FlipFaces::insideOut, "control G");
		setMenuItemMnemonic(KeyEvent.VK_I);
		//(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK)
	}

	public static void insideOut(ModelHandler modelHandler) {
		FlipFacesAction flipFacesAction = new FlipFacesAction(modelHandler.getModelView().getSelectedVertices());
		modelHandler.getUndoManager().pushAction(flipFacesAction.redo());
		ProgramGlobals.getMainPanel().repaint();
	}
}
