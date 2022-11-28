package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TSpline;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ManualUVTransformPanel extends JPanel {

	private ModelHandler modelHandler;
	private TVertexEditorManager tVertexEditorManager;
	private JPanel movePanel;
	private JPanel scalePanel;
	private JPanel rotatePanel;
//	private ShrinkFattenPanel shrinkFattenPanel;

	public ManualUVTransformPanel() {
		super(new MigLayout("hidemode 2, ins 0, gap 0"));
		movePanel = getMovePanel();
		scalePanel = getScalePanel();
		rotatePanel = getRotatePanel();

		add(movePanel);
		add(scalePanel);
		add(rotatePanel);

		scalePanel.setVisible(false);
		rotatePanel.setVisible(false);

		boolean temp = false;
//		boolean temp = true;
		if (temp) {
			TSpline tSpline = new TSpline();
			add(tSpline, "newline, spanx, growy");
		}

		ProgramGlobals.getActionTypeGroup().addToolbarButtonListener(this::showCorrectPanel);
	}

	public void showCorrectPanel(ModelEditorActionType3 type3) {
		switch (type3) {

			case TRANSLATION, EXTRUDE, EXTEND -> {
				movePanel.setVisible(true);
				scalePanel.setVisible(false);
				rotatePanel.setVisible(false);
			}
			case ROTATION, SQUAT -> {
				movePanel.setVisible(false);
				scalePanel.setVisible(false);
				rotatePanel.setVisible(true);
			}
			case SCALING -> {
				movePanel.setVisible(false);
				scalePanel.setVisible(true);
				rotatePanel.setVisible(false);
			}
		}
	}

	public ManualUVTransformPanel setModel(ModelHandler modelHandler, TVertexEditorManager modelEditorManager) {
		this.modelHandler = modelHandler;
		this.tVertexEditorManager = modelEditorManager;
//		shrinkFattenPanel.setModel(modelHandler);
		return this;
	}

	public ManualUVTransformPanel setModelPanel(ModelPanel modelPanel) {
		if (modelPanel != null) {
			this.modelHandler = modelPanel.getModelHandler();
			this.tVertexEditorManager = modelPanel.getUvModelEditorManager();

		} else {
			this.modelHandler = null;
			this.tVertexEditorManager = null;
		}
//		shrinkFattenPanel.setModel(modelHandler);
		return this;
	}

	public ManualUVTransformPanel setAnimationState(boolean isAnimating){
//		shrinkFattenPanel.setVisible(!isAnimating);
		return this;
	}


	JPanel getMovePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
		Vec2SpinnerArray spinners = new Vec2SpinnerArray(new Vec2(0, 0), "Move X:", "Move Y:");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Move");
		button.addActionListener(e -> move(spinners));
		inputPanel.add(button, "wrap");

		Vec2SpinnerArray spinners2 = new Vec2SpinnerArray(new Vec2(0, 0), "New Position X:", "New Position Y:");
		inputPanel.add(spinners2.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button2 = new JButton("Move to");
		button2.addActionListener(e -> moveTo(spinners2));
		inputPanel.add(button2);
		return inputPanel;
	}

	private void move(Vec2SpinnerArray spinners) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			modelHandler.getUndoManager().pushAction(tVertexEditorManager.getModelEditor().translate(spinners.getVec3Value()).redo());
		}
	}

	JPanel getRotatePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
		Vec1SpinnerArray spinners = new Vec1SpinnerArray(0.0f, "Rotate degrees:");
//		Vec2SpinnerArray spinners = new Vec2SpinnerArray(new Vec2(0, 0, 0), "Rotate X degrees (around axis facing front):", "Rotate Y degrees (around axis facing left):", "Rotate Z degrees (around axis facing up):");
//		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(0, 0, 0), "Rotate X (degrees)", "Rotate Y (degrees):", "Rotate Z (degrees):");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JCheckBox customOrigin = new JCheckBox("Custom Origin");
		inputPanel.add(customOrigin, "wrap");
		Vec2SpinnerArray centerSpinners = getCenterSpinners();
		customOrigin.addActionListener(e -> centerSpinners.setEnabled(customOrigin.isSelected()));
		inputPanel.add(centerSpinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Rotate");
		button.addActionListener(e -> rotate(spinners, customOrigin, centerSpinners));
		inputPanel.add(button);
		return inputPanel;
	}

	private void rotate(Vec1SpinnerArray spinners, JCheckBox customOrigin, Vec2SpinnerArray centerSpinners) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 center;
			if (customOrigin.isSelected()) {
				center = centerSpinners.getVec3Value();
			} else {
//				center = modelHandler.getModelView().getSelectionCenter();
				center = new Vec3().set(modelHandler.getModelView().getTSelectionCenter(),0);
			}
//			Vec2 selectionCenter = modelHandler.getModelView().getSelectionCenter();
			modelHandler.getUndoManager().pushAction(tVertexEditorManager.getModelEditor().rotate(center, new Vec3(0, 0, -spinners.getValue())).redo());
		}
	}
	private void rotate(Vec3SpinnerArray spinners, JCheckBox customOrigin, Vec2SpinnerArray centerSpinners) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 center;
			if (customOrigin.isSelected()) {
				center = centerSpinners.getVec3Value();
			} else {
//				center = modelHandler.getModelView().getSelectionCenter();
				center = new Vec3().set(modelHandler.getModelView().getTSelectionCenter(),0);
			}
//			Vec2 selectionCenter = modelHandler.getModelView().getSelectionCenter();
			modelHandler.getUndoManager().pushAction(tVertexEditorManager.getModelEditor().rotate(center, spinners.getValue()).redo());
		}
	}

	private void rotate(Vec2SpinnerArray spinners, JCheckBox customOrigin, Vec2SpinnerArray centerSpinners) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 center;
			if (customOrigin.isSelected()) {
				center = centerSpinners.getVec3Value();
			} else {
//				center = modelHandler.getModelView().getSelectionCenter();
				center = new Vec3().set(modelHandler.getModelView().getTSelectionCenter(),0);
			}
//			Vec2 selectionCenter = modelHandler.getModelView().getSelectionCenter();
			modelHandler.getUndoManager().pushAction(tVertexEditorManager.getModelEditor().rotate(center, spinners.getVec3Value()).redo());
		}
	}

	private void moveTo(Vec2SpinnerArray spinners) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
//			Vec3 selectionCenter = modelHandler.getModelView().getSelectionCenter();
			Vec3 selectionCenter = new Vec3().set(modelHandler.getModelView().getTSelectionCenter(),0);
			modelHandler.getUndoManager()
					.pushAction(tVertexEditorManager.getModelEditor().setPosition(selectionCenter, spinners.getVec3Value()).redo());
		}
	}

	JPanel getScalePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0, hidemode 1"));
		Vec2SpinnerArray spinners = new Vec2SpinnerArray(new Vec2(1, 1), "Scale X:", "Scale Y:");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JCheckBox customOrigin = new JCheckBox("Custom Origin");
		inputPanel.add(customOrigin, "wrap");
		Vec2SpinnerArray centerSpinners = getCenterSpinners();
		customOrigin.addActionListener(e -> centerSpinners.setEnabled(customOrigin.isSelected()));
		inputPanel.add(centerSpinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Scale");
		button.addActionListener(e -> scale(spinners, customOrigin, centerSpinners));
		inputPanel.add(button, "wrap");

//		shrinkFattenPanel = new ShrinkFattenPanel();
//		inputPanel.add(shrinkFattenPanel);
		return inputPanel;
	}

	private Vec2SpinnerArray getCenterSpinners() {
		Vec2SpinnerArray centerSpinners = new Vec2SpinnerArray("Center X:", "Center Y:");
		centerSpinners.setEnabled(false);
		return centerSpinners;
	}

	private void scale(Vec2SpinnerArray spinners, JCheckBox customOrigin, Vec2SpinnerArray centerSpinners) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 center;
			if (customOrigin.isSelected()) {
				center = centerSpinners.getVec3Value();
			} else {
//				center = modelHandler.getModelView().getSelectionCenter();
				center = new Vec3().set(modelHandler.getModelView().getTSelectionCenter(),0);
			}
			modelHandler.getUndoManager()
					.pushAction(tVertexEditorManager.getModelEditor().scale(center, spinners.getVec3Value()).redo());
		}
	}
}
