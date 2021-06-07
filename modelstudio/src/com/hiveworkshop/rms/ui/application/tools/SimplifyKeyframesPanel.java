package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.animation.SimplifyKeyframesAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.CheckSpinner;
import com.hiveworkshop.rms.util.FramePopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

public class SimplifyKeyframesPanel extends JPanel {
	float trans = -1;
	float scale = -1;
	float rot = -1;

	CheckSpinner transCheckSpinner;
	CheckSpinner scaleCheckSpinner;
	CheckSpinner rotCheckSpinner;

	public SimplifyKeyframesPanel() {
		super(new MigLayout("fill", "", ""));

		String allowed = "Allowed diff to remove keyframe";
		add(new JLabel("Simplify keyframes of selected nodes"), "wrap");

		transCheckSpinner = new CheckSpinner("Translation keyframes", allowed);
		add(transCheckSpinner, "wrap");
		scaleCheckSpinner = new CheckSpinner("Scaling keyframes", allowed);
		add(scaleCheckSpinner, "wrap");
		rotCheckSpinner = new CheckSpinner("Rotation keyframes", allowed);
		add(rotCheckSpinner, "wrap");

		JButton simplifyButton = new JButton("Simplify");
		simplifyButton.addActionListener(e -> simplify());
		add(simplifyButton, "wrap");
	}

	private void simplify() {
		trans = transCheckSpinner.getValue() == null ? -1 : transCheckSpinner.getValue();
		scale = scaleCheckSpinner.getValue() == null ? -1 : scaleCheckSpinner.getValue();
		rot = rotCheckSpinner.getValue() == null ? -1 : rotCheckSpinner.getValue();
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		SimplifyKeyframesAction action = new SimplifyKeyframesAction(modelPanel.getModelView(), trans, scale, rot);
		modelPanel.getUndoManager().pushAction(action.redo());
	}


	public static void showPopup() {
		JComponent parent = ProgramGlobals.getMainPanel();
		FramePopup.show(new SimplifyKeyframesPanel(), parent, "Simplyfy keyframes");
	}

	public static void showPopup(JComponent parent) {
		FramePopup.show(new SimplifyKeyframesPanel(), parent, "Simplyfy keyframes");
	}

	public static void simplifyKeyframes() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		final int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
				"This is an irreversible process that will lose some of your model data," +
						"\nin exchange for making it a smaller storage size." +
						"\n\nContinue and simplify keyframes?",
				"Warning: Simplify Keyframes", JOptionPane.OK_CANCEL_OPTION);
		if (x == JOptionPane.OK_OPTION && modelPanel != null) {
			final EditableModel currentMDL = modelPanel.getModel();
			simplifyKeyframes(currentMDL);
		} else if (modelPanel != null) {
			JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(),
					"No open model found",
					"No model",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void simplifyKeyframes(EditableModel model) {
		List<AnimFlag<?>> allAnimFlags = model.getAllAnimFlags();

		SimplifyKeyframesAction action = new SimplifyKeyframesAction(allAnimFlags, model, 0.1f);
		ProgramGlobals.getCurrentModelPanel().getUndoManager().pushAction(action.redo());
	}
}
