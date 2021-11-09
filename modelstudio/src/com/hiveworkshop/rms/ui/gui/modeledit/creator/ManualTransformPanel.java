package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ManualTransformPanel extends JPanel {

	private ModelHandler modelHandler;
	private ModelEditorManager modelEditorManager;
	private JPanel movePanel;
	//	private JPanel moveToPanel;
	private JPanel scalePanel;
	private JPanel rotatePanel;

	public ManualTransformPanel() {
		super(new MigLayout("hidemode 2, ins 0, gap 0"));
		movePanel = getMovePanel();
//		moveToPanel = getMoveToPanel();
		scalePanel = getScalePanel();
		rotatePanel = getRotatePanel();

		add(movePanel);
//		add(moveToPanel);
		add(scalePanel);
		add(rotatePanel);

//		movePanel.setVisible(false);
//		moveToPanel.setVisible(false);
		scalePanel.setVisible(false);
		rotatePanel.setVisible(false);

		ProgramGlobals.getActionTypeGroup().addToolbarButtonListener(this::showCorrectPanel);
	}

	public void showCorrectPanel(ModelEditorActionType3 type3) {
		switch (type3) {

			case TRANSLATION, EXTRUDE, EXTEND -> {
				movePanel.setVisible(true);
//				moveToPanel.setVisible(false);
				scalePanel.setVisible(false);
				rotatePanel.setVisible(false);
			}
			case ROTATION, SQUAT -> {
				movePanel.setVisible(false);
//				moveToPanel.setVisible(false);
				scalePanel.setVisible(false);
				rotatePanel.setVisible(true);
			}
			case SCALING -> {
				movePanel.setVisible(false);
//				moveToPanel.setVisible(false);
				scalePanel.setVisible(true);
				rotatePanel.setVisible(false);
			}
		}
	}

	public ManualTransformPanel setModel(ModelHandler modelHandler, ModelEditorManager modelEditorManager) {
		this.modelHandler = modelHandler;
		this.modelEditorManager = modelEditorManager;
		return this;
	}


	JPanel getMovePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(0, 0, 0), "Move X:", "Move Y:", "Move Z:");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Move");
		button.addActionListener(e -> move(spinners));
		inputPanel.add(button, "wrap");

		Vec3SpinnerArray spinners2 = new Vec3SpinnerArray(new Vec3(0, 0, 0), "New Position X:", "New Position Y:", "New Position Z:");
		inputPanel.add(spinners2.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button2 = new JButton("Move to");
		button2.addActionListener(e -> moveTo(spinners2));
		inputPanel.add(button2);
		return inputPanel;
	}

	JPanel getMovePanel1() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(0, 0, 0), "Move X:", "Move Y:", "Move Z:");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Move");
		button.addActionListener(e -> move(spinners));
		inputPanel.add(button);
		return inputPanel;
	}

	private void move(Vec3SpinnerArray spinners) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			modelHandler.getUndoManager().pushAction(modelEditorManager.getModelEditor().translate(spinners.getValue()).redo());
		}
	}

	JPanel getRotatePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
//		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(0, 0, 0), "Rotate X degrees (around axis facing front):", "Rotate Y degrees (around axis facing left):", "Rotate Z degrees (around axis facing up):");
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(0, 0, 0), "Rotate X (degrees)", "Rotate Y (degrees):", "Rotate Z (degrees):");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JCheckBox customOrigin = new JCheckBox("Custom Origin");
		inputPanel.add(customOrigin, "wrap");
		Vec3SpinnerArray centerSpinners = getCenterSpinners();
		customOrigin.addActionListener(e -> centerSpinners.setEnabled(customOrigin.isSelected()));
		inputPanel.add(centerSpinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Rotate");
		button.addActionListener(e -> rotate(spinners, customOrigin, centerSpinners));
		inputPanel.add(button);
		return inputPanel;
	}

	private void rotate(Vec3SpinnerArray spinners, JCheckBox customOrigin, Vec3SpinnerArray centerSpinners) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 center;
			if (customOrigin.isSelected()) {
				center = centerSpinners.getValue();
			} else {
				center = modelHandler.getModelView().getSelectionCenter();
			}
//			Vec3 selectionCenter = modelHandler.getModelView().getSelectionCenter();
			modelHandler.getUndoManager().pushAction(modelEditorManager.getModelEditor().rotate(center, spinners.getValue()).redo());
		}
	}

	JPanel getMoveToPanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(0, 0, 0), "New Position X:", "New Position Y:", "New Position Z:");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Move to");
		button.addActionListener(e -> moveTo(spinners));
		inputPanel.add(button);
		return inputPanel;
	}

	private void moveTo(Vec3SpinnerArray spinners) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 selectionCenter = modelHandler.getModelView().getSelectionCenter();
			modelHandler.getUndoManager()
					.pushAction(modelEditorManager.getModelEditor().setPosition(selectionCenter, spinners.getValue()).redo());
		}
	}

	JPanel getScalePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(1, 1, 1), "Scale X:", "Scale Y:", "Scale Z:");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JCheckBox customOrigin = new JCheckBox("Custom Origin");
		inputPanel.add(customOrigin, "wrap");
		Vec3SpinnerArray centerSpinners = getCenterSpinners();
		customOrigin.addActionListener(e -> centerSpinners.setEnabled(customOrigin.isSelected()));
		inputPanel.add(centerSpinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Scale");
		button.addActionListener(e -> scale(spinners, customOrigin, centerSpinners));
		inputPanel.add(button);
		return inputPanel;
	}

	private Vec3SpinnerArray getCenterSpinners() {
		Vec3SpinnerArray centerSpinners = new Vec3SpinnerArray("Center X:", "Center Y:", "Center Z:");
		centerSpinners.setEnabled(false);
		return centerSpinners;
	}

	private void scale(Vec3SpinnerArray spinners, JCheckBox customOrigin, Vec3SpinnerArray centerSpinners) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 center;
			if (customOrigin.isSelected()) {
				center = centerSpinners.getValue();
			} else {
				center = modelHandler.getModelView().getSelectionCenter();
			}
			modelHandler.getUndoManager()
					.pushAction(modelEditorManager.getModelEditor().scale(center, spinners.getValue()).redo());
		}
	}
}
