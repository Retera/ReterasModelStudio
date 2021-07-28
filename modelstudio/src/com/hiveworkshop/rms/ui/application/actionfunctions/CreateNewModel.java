package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class CreateNewModel extends ActionFunction{
	public CreateNewModel(){
		super(TextKey.NEW, () -> newModel(), "control N");
	}

	public static void newModel() {
		JPanel newModelPanel = new JPanel();
		newModelPanel.setLayout(new MigLayout("fill, ins 0"));
		newModelPanel.add(new JLabel("Model Name: "), "");
		JTextField newModelNameField = new JTextField("MrNew", 25);
		newModelPanel.add(newModelNameField, "wrap");

		SmartButtonGroup typeGroup = new SmartButtonGroup();
		typeGroup.addJRadioButton("Create Empty", null);
		typeGroup.addJRadioButton("Create Plane", null);
		typeGroup.addJRadioButton("Create Box", null);
		typeGroup.setSelectedIndex(0);
		newModelPanel.add(typeGroup.getButtonPanel());

		MainPanel mainPanel = ProgramGlobals.getMainPanel();

		int userDialogResult = JOptionPane.showConfirmDialog(mainPanel, newModelPanel, "New Model", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (userDialogResult == JOptionPane.OK_OPTION) {
			EditableModel mdl = new EditableModel(newModelNameField.getText());
			if (typeGroup.getButton("Create Box").isSelected()) {
				SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
				JSpinner spinner = new JSpinner(sModel);
				int userChoice = JOptionPane.showConfirmDialog(mainPanel, spinner, "Box: Choose Segments",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (userChoice != JOptionPane.OK_OPTION) {
					return;
				}
				ModelUtils.createBox(mdl, new Vec3(-64, -64, 0), new Vec3(64, 64, 128), ((Number) spinner.getValue()).intValue());
				mdl.setExtents(new ExtLog(128).setDefault());
			} else if (typeGroup.getButton("Create Plane").isSelected()) {
				SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
				JSpinner spinner = new JSpinner(sModel);
				int userChoice = JOptionPane.showConfirmDialog(mainPanel, spinner, "Plane: Choose Segments",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (userChoice != JOptionPane.OK_OPTION) {
					return;
				}
				ModelUtils.createGroundPlane(mdl, new Vec3(64, 64, 0), new Vec3(-64, -64, 0),
						((Number) spinner.getValue()).intValue());
				mdl.setExtents(new ExtLog(128).setDefault());
			}

			ModelHandler modelHandler = new ModelHandler(mdl);
			ModelPanel temp = new ModelPanel(modelHandler,
					mainPanel.getCoordDisplayListener(),
					mainPanel.getViewportTransferHandler(), mainPanel.getViewportListener(), RMSIcons.MDLIcon, false);
			ModelLoader.loadModel(true, true, temp);
		}

	}
}
