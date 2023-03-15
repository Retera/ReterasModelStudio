package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TSpline;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ManualTransformPanel extends JPanel {

	private ModelHandler modelHandler;
	private ModelEditorManager modelEditorManager;
	private final JPanel movePanel;
	private final JPanel scalePanel;
	private final JPanel rotatePanel;
	private ShrinkFattenPanel shrinkFattenPanel;
	private JPanel selectionPanel;
	private final JLabel selectedVerts = new JLabel();
	private final JLabel selectedNodes = new JLabel();
	private final JLabel selectedCams = new JLabel();
	private final JLabel selectionCenter = new JLabel();

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
			selectionCenter.setText("" + modelEditorManager.getSelectionView().getCenter());
		}
	}

	JPanel getMovePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0", "", "[]3[]10[]3[]"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray("Move X:", "Move Y:", "Move Z:").setLabelWrap(false);
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button = new JButton("Move");
		button.addActionListener(e -> move(spinners.getValue()));
		inputPanel.add(button, "wrap");

		Vec3SpinnerArray spinners2 = new Vec3SpinnerArray("New X:", "New Y:", "New Z:").setLabelWrap(false);
		inputPanel.add(spinners2.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JButton button2 = new JButton("Move to");
		button2.addActionListener(e -> moveTo(spinners2.getValue()));
		inputPanel.add(button2, "split");

		JButton toSelection = new JButton("»«");
		toSelection.setToolTipText("Set position to center of selection");
		inputPanel.add(toSelection, "wrap");
		toSelection.addActionListener(e -> spinners2.setValues(getCurrCenter()));
		return inputPanel;
	}

	JPanel getRotatePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0", "", "[]3[]3[]3[]"));
//		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(0, 0, 0), "Rotate X degrees (around axis facing front):", "Rotate Y degrees (around axis facing left):", "Rotate Z degrees (around axis facing up):");
		Vec3SpinnerArray spinners = new Vec3SpinnerArray("Rotate X:", "Rotate Y:", "Rotate Z:").setLabelWrap(false);
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JPanel originPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		JCheckBox customOrigin = new JCheckBox("Custom Origin");
		originPanel.add(customOrigin, "split");
		JButton toSelection = new JButton("»«");
		toSelection.setToolTipText("Set origin to center of selection");
		originPanel.add(toSelection, "wrap");

		Vec3SpinnerArray centerSpinners = getCenterSpinners();
		customOrigin.addActionListener(e -> centerSpinners.setEnabled(customOrigin.isSelected()));
		originPanel.add(centerSpinners.setSpinnerWrap(true).spinnerPanel(), "wrap");
		toSelection.addActionListener(e -> centerSpinners.setValues(getCurrCenter()));

		inputPanel.add(originPanel, "wrap");

		JButton button = new JButton("Rotate");
		button.addActionListener(e -> rotate(spinners.getValue(), customOrigin.isSelected(), centerSpinners.getValue()));
		inputPanel.add(button);
		return inputPanel;
	}

	JPanel getScalePanel() {
		JPanel inputPanel = new JPanel(new MigLayout("gap 0, hidemode 1", "", "[]3[]3[]3[]"));
		Vec3SpinnerArray spinners = new Vec3SpinnerArray(new Vec3(1, 1, 1), "Scale X:", "Scale Y:", "Scale Z:").setLabelWrap(false);
		inputPanel.add(spinners.setSpinnerWrap(true).spinnerPanel(), "wrap");

		JPanel originPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		JCheckBox customOrigin = new JCheckBox("Custom Origin");
		originPanel.add(customOrigin, "split");
		JButton toSelection = new JButton("»«");
		toSelection.setToolTipText("Set origin to center of selection");
		originPanel.add(toSelection, "wrap");

		Vec3SpinnerArray centerSpinners = getCenterSpinners();
		customOrigin.addActionListener(e -> centerSpinners.setEnabled(customOrigin.isSelected()));
		originPanel.add(centerSpinners.setSpinnerWrap(true).spinnerPanel(), "wrap");
		toSelection.addActionListener(e -> centerSpinners.setValues(getCurrCenter()));

		inputPanel.add(originPanel, "wrap");

		JButton button = new JButton("Scale");
		button.addActionListener(e -> scale(spinners.getValue(), customOrigin.isSelected(), centerSpinners.getValue()));
		inputPanel.add(button, "wrap");

		shrinkFattenPanel = new ShrinkFattenPanel();
		inputPanel.add(shrinkFattenPanel);
		return inputPanel;
	}

	private Vec3SpinnerArray getCenterSpinners() {
		Vec3SpinnerArray centerSpinners = new Vec3SpinnerArray("Center X:", "Center Y:", "Center Z:").setLabelWrap(false);
		centerSpinners.setEnabled(false);
		return centerSpinners;
	}

	private void rotate(Vec3 rot, boolean customOrigin, Vec3 customCenter) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 center = customOrigin ? customCenter : getCurrCenter();
			modelHandler.getUndoManager().pushAction(modelEditorManager.getModelEditor().rotate(center, rot, new Mat4()).redo());
		}
	}


	private void move(Vec3 dist) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			modelHandler.getUndoManager().pushAction(modelEditorManager.getModelEditor().translate(dist, new Mat4()).redo());
		}
	}

	private void moveTo(Vec3 pos) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 selectionCenter = getCurrCenter();
			modelHandler.getUndoManager().pushAction(modelEditorManager.getModelEditor().setPosition(selectionCenter, pos).redo());
		}
	}

	private void scale(Vec3 scale, boolean customOrigin, Vec3 customCenter) {
		if (modelHandler != null && !modelHandler.getModelView().isEmpty()) {
			Vec3 center = customOrigin ? customCenter : getCurrCenter();
			modelHandler.getUndoManager().pushAction(modelEditorManager.getModelEditor().scale(center, scale, new Mat4()).redo());
		}
	}

	private Vec3 getCurrCenter(){
		if(modelHandler == null){
			return Vec3.ZERO;
		} else {
			return modelEditorManager.getSelectionView().getCenter();
		}
	}
}
