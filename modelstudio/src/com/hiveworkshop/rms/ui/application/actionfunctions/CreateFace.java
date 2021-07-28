package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ModelEditActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class CreateFace extends ActionFunction {
	public CreateFace(){
		super(TextKey.CREATE_FACE, CreateFace::createFace);
	}

	public static void createFace() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		if (!isTextField() && !(ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE)) {
			try {
				ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
				if (modelPanel != null) {
					Viewport viewport = mainPanel.getViewportListener().getViewport();
					Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
					UndoAction createFaceFromSelection = ModelEditActions.createFaceFromSelection(modelPanel.getModelView(), facingVector);

					modelPanel.getUndoManager().pushAction(createFaceFromSelection);
				}
			} catch (final FaceCreationException exc) {
				JOptionPane.showMessageDialog(mainPanel, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} catch (final Exception exc) {
				ExceptionPopup.display(exc);
			}
		}
	}

	private static boolean isTextField() {
		Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		return (focusedComponent instanceof JTextComponent);
	}
}
