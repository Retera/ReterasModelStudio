package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TSpline;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ManualTransformPanel extends JPanel {

	private ModelHandler modelHandler;
	private ModelEditorManager modelEditorManager;
	private JPanel movePanel;
	private JPanel scalePanel;
	private JPanel rotatePanel;
	private ShrinkFattenPanel shrinkFattenPanel;
	private JPanel selectionPanel;
	JLabel selectedVerts = new JLabel();
	JLabel selectedNodes = new JLabel();
	JLabel selectedCams = new JLabel();
	JLabel selectionCenter = new JLabel();

	public ManualTransformPanel() {
		super(new MigLayout("hidemode 2, ins 0, gap 0"));
		movePanel = getMovePanel();
		scalePanel = getScalePanel();
		rotatePanel = getRotatePanel();

		add(getSelectionInfoPanel(), "spanx, wrap");
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
		ModelStructureChangeListener.changeListener.addSelectionListener(this, this::updateSelectionPanel);
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

	public ManualTransformPanel setModel(ModelHandler modelHandler, ModelEditorManager modelEditorManager) {
		this.modelHandler = modelHandler;
		this.modelEditorManager = modelEditorManager;
		shrinkFattenPanel.setModel(modelHandler);
		return this;
	}

	public ManualTransformPanel setModelPanel(ModelPanel modelPanel) {
		if (modelPanel != null) {
			this.modelHandler = modelPanel.getModelHandler();
			this.modelEditorManager = modelPanel.getModelEditorManager();

		} else {
			this.modelHandler = null;
			this.modelEditorManager = null;
		}
		shrinkFattenPanel.setModel(modelHandler);
		updateSelectionPanel();
		return this;
	}

	public ManualTransformPanel setAnimationState(boolean isAnimating){
		shrinkFattenPanel.setVisible(!isAnimating);
		return this;
	}

	JPanel getSelectionInfoPanel(){
		JPanel selectionPanel = new JPanel(new MigLayout("gap 0", "[][][grow]"));
		selectionPanel.add(new JLabel("Selection:"), "wrap");
		selectionPanel.add(new JLabel("Vertices: "), "");
		selectionPanel.add(selectedVerts, "right, wrap");
		selectionPanel.add(new JLabel("Nodes: "), "");
		selectionPanel.add(selectedNodes, "right, wrap");
		selectionPanel.add(new JLabel("Camera Nodes: "), "");
		selectionPanel.add(selectedCams, "right, wrap");
		selectionPanel.add(new JLabel("Center: "), "wrap");
		selectionPanel.add(selectionCenter, "spanx 3");
		return selectionPanel;
	}
	void updateSelectionPanel(){
		if(modelHandler != null){
			selectedVerts.setText("" + modelHandler.getModelView().getSelectedVertices().size());
			selectedNodes.setText("" + modelHandler.getModelView().getSelectedIdObjects().size());
			selectedCams.setText("" + modelHandler.getModelView().getSelectedCameraNodes().size());
			selectionCenter.setText("" + modelHandler.getModelView().getSelectionCenter());
		}
	}

	JPanel getMovePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(0, 0, 0), "Move X:", "Move Y:", "Move Z:");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Move");
		button.addActionListener(e -> move(spinners.getValue()));
		inputPanel.add(button, "wrap");

		Vec3SpinnerArray spinners2 = new Vec3SpinnerArray(new Vec3(0, 0, 0), "New Position X:", "New Position Y:", "New Position Z:");
		inputPanel.add(spinners2.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button2 = new JButton("Move to");
		button2.addActionListener(e -> moveTo(spinners2.getValue()));
		inputPanel.add(button2);
		return inputPanel;
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
		JPanel inputPanel = new JPanel(new MigLayout("gap 0, hidemode 1"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(1, 1, 1), "Scale X:", "Scale Y:", "Scale Z:");
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JCheckBox customOrigin = new JCheckBox("Custom Origin");
		inputPanel.add(customOrigin, "wrap");
		Vec3SpinnerArray centerSpinners = getCenterSpinners();
		customOrigin.addActionListener(e -> centerSpinners.setEnabled(customOrigin.isSelected()));
		inputPanel.add(centerSpinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Scale");
		button.addActionListener(e -> scale(spinners, customOrigin, centerSpinners));
		inputPanel.add(button, "wrap");

		shrinkFattenPanel = new ShrinkFattenPanel();
		inputPanel.add(shrinkFattenPanel);
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
