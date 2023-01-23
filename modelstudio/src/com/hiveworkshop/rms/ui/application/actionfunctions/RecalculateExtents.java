package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.model.RecalculateExtentsAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class RecalculateExtents extends ActionFunction{

	public RecalculateExtents(){
		super(TextKey.RECALCULATE_EXTENTS, RecalculateExtents::recalculateExtents);
		setKeyStroke(KeyStroke.getKeyStroke("control shift E"));
	}

	public static void recalculateExtents(ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();
		boolean hasEditableGeos = 0 < modelView.getEditableGeosets().size();

		JPanel messagePanel = new JPanel(new MigLayout());
		messagePanel.add(new JLabel("This will calculate the extents of all model components. Proceed?"), "wrap");
		messagePanel.add(new JLabel("(It may destroy existing extents)"), "wrap");

		SmartButtonGroup buttonGroup2 = new SmartButtonGroup();
		buttonGroup2.addJRadioButton("Consider all geosets for calculation", null);
		JRadioButton jRadioButton = buttonGroup2.addJRadioButton("Consider current editable geosets for calculation", null);
		jRadioButton.setEnabled(hasEditableGeos);
		if(!hasEditableGeos){
				jRadioButton.setToolTipText("No editable geosets found");
		}
		buttonGroup2.setSelectedIndex(0);

		messagePanel.add(buttonGroup2.getButtonPanel(), "wrap");

		int userChoice = JOptionPane.showConfirmDialog(
				ProgramGlobals.getMainPanel(), messagePanel,
				"Recalculate Extents",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (userChoice == JOptionPane.YES_OPTION) {
			EditableModel model = modelHandler.getModel();

			RecalculateExtentsAction recalculateExtentsAction;
			if (buttonGroup2.getSelectedIndex() == 0) {
				recalculateExtentsAction = new RecalculateExtentsAction(model, model.getGeosets());
			} else {
				recalculateExtentsAction = new RecalculateExtentsAction(model, modelView.getEditableGeosets());
			}

			modelHandler.getUndoManager().pushAction(recalculateExtentsAction.redo());
		}
		ProgramGlobals.getMainPanel().repaint();
	}
}
