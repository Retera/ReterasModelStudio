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
		JPanel inputPanel = new JPanel(new MigLayout("gap 0", "", "[]3[]10[]3[]"));
		Vec2SpinnerArray spinners = new Vec2SpinnerArray("Move X:", "Move Y:").setLabelWrap(false);
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Move");
		button.addActionListener(e -> move(spinners.getVec3Value()));
		inputPanel.add(button, "wrap");

		Vec2SpinnerArray spinners2 = new Vec2SpinnerArray("New X:", "New Y:").setLabelWrap(false);
		inputPanel.add(spinners2.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button2 = new JButton("Move to");
		button2.addActionListener(e -> moveTo(spinners2.getVec3Value()));
		inputPanel.add(button2);

		JButton toSelection = new JButton("»«");
		toSelection.setToolTipText("Set position to center of selection");
		inputPanel.add(toSelection, "wrap");
		toSelection.addActionListener(e -> spinners2.setValues(getCurrCenter2()));
		return inputPanel;
	}

	JPanel getRotatePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0", "", "[]3[]3[]3[]"));
		Vec1SpinnerArray spinners = new Vec1SpinnerArray(0.0f, "Rotate:");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JPanel originPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		JCheckBox customOrigin = new JCheckBox("Custom Origin");
		originPanel.add(customOrigin, "split");
		JButton toSelection = new JButton("»«");
		toSelection.setToolTipText("Set origin to center of selection");
		originPanel.add(toSelection, "wrap");

		Vec2SpinnerArray centerSpinners = getCenterSpinners();
		customOrigin.addActionListener(e -> centerSpinners.setEnabled(customOrigin.isSelected()));
		originPanel.add(centerSpinners.setSpinnerWrap(true).spinnerPanel(), "wrap");
		toSelection.addActionListener(e -> centerSpinners.setValues(getCurrCenter2()));

		inputPanel.add(originPanel, "wrap");

		JButton button = new JButton("Rotate");
		button.addActionListener(e -> rotate(new Vec3(0,0,spinners.getValue()), customOrigin.isSelected(), centerSpinners.getVec3Value()));
		inputPanel.add(button);
		return inputPanel;
	}

	JPanel getScalePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0, hidemode 1", "", "[]3[]3[]3[]"));
		Vec2SpinnerArray spinners = new Vec2SpinnerArray(new Vec2(1, 1), "Scale X:", "Scale Y:").setLabelWrap(false);
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JPanel originPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		JCheckBox customOrigin = new JCheckBox("Custom Origin");
		originPanel.add(customOrigin, "split");
		JButton toSelection = new JButton("»«");
		toSelection.setToolTipText("Set origin to center of selection");
		originPanel.add(toSelection, "wrap");

		Vec2SpinnerArray centerSpinners = getCenterSpinners();
		customOrigin.addActionListener(e -> centerSpinners.setEnabled(customOrigin.isSelected()));
		originPanel.add(centerSpinners.setSpinnerWrap(true).spinnerPanel(), "wrap");
		toSelection.addActionListener(e -> centerSpinners.setValues(getCurrCenter2()));

		inputPanel.add(originPanel, "wrap");

		JButton button = new JButton("Scale");
		button.addActionListener(e -> scale(spinners.getVec3Value(), customOrigin.isSelected(), centerSpinners.getVec3Value()));
		inputPanel.add(button, "wrap");

//		shrinkFattenPanel = new ShrinkFattenPanel();
//		inputPanel.add(shrinkFattenPanel);
		return inputPanel;
	}

	private Vec2SpinnerArray getCenterSpinners() {
		Vec2SpinnerArray centerSpinners = new Vec2SpinnerArray("Center X:", "Center Y:").setLabelWrap(false);
		centerSpinners.setEnabled(false);
		return centerSpinners;
	}

	private void rotate(Vec3 rot, boolean customOrigin, Vec3 customCenter) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 center = customOrigin ? customCenter : getCurrCenter();
			modelHandler.getUndoManager().pushAction(tVertexEditorManager.getModelEditor().rotate(center, rot, new Mat4()).redo());
		}
	}

	private void move(Vec3 dist) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			modelHandler.getUndoManager().pushAction(tVertexEditorManager.getModelEditor().translate(dist, new Mat4()).redo());
		}
	}

	private void moveTo(Vec3 pos) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 selectionCenter = new Vec3().set(modelHandler.getModelView().getTSelectionCenter(),0);
			modelHandler.getUndoManager()
					.pushAction(tVertexEditorManager.getModelEditor().setPosition(selectionCenter, pos).redo());
		}
	}

	private void scale(Vec3 scale, boolean customOrigin, Vec3 customCenter) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 center = customOrigin ? customCenter : getCurrCenter();
			modelHandler.getUndoManager()
					.pushAction(tVertexEditorManager.getModelEditor().scale(center, scale, new Mat4()).redo());
		}
	}

	private Vec3 getCurrCenter(){
		if(modelHandler == null){
			return Vec3.ZERO;
		} else {
			return new Vec3().set(modelHandler.getModelView().getTSelectionCenter(),0);
		}
	}
	private Vec2 getCurrCenter2(){
		if(modelHandler == null){
			return Vec2.ORIGIN;
		} else {
			return new Vec2().set(modelHandler.getModelView().getTSelectionCenter());
		}
	}
}
