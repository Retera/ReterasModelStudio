package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TSpline;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class SelectionInfoPanel extends JPanel {

	private ModelHandler modelHandler;
	private ModelEditorManager modelEditorManager;
	private final JLabel selectedVerts = new JLabel();
	private final JLabel selectedNodes = new JLabel();
	private final JLabel selectedCams = new JLabel();
	private final JLabel selectionCenter = new JLabel();

	public SelectionInfoPanel() {
//		super(new MigLayout("hidemode 2, ins 0, gap 0"));
		super(new MigLayout("gap 0, ins 2 5", "[][][grow]"));

		add(new JLabel("Vertices: "), "");
		add(selectedVerts, "right, wrap");
		add(new JLabel("Nodes: "), "");
		add(selectedNodes, "right, wrap");
		add(new JLabel("Camera Nodes: "), "");
		add(selectedCams, "right, wrap");
		add(new JLabel("Center: "), "wrap");
		add(selectionCenter, "spanx 3");

//		add(new JLabel("Vertices: "), "");
//		add(selectedVerts, "right, growx, wrap");
//		add(new JLabel("Nodes: "), "");
//		add(selectedNodes, "right, growx, wrap");
//		add(new JLabel("Camera Nodes: "), "");
//		add(selectedCams, "right, growx, wrap");
//		add(new JLabel("Center: "), "wrap");
//		add(selectionCenter, "spanx 3");


		boolean temp = false;
//		boolean temp = true;
		if (temp) {
			TSpline tSpline = new TSpline();
			add(tSpline, "newline, spanx, growy");
		}

		ModelStructureChangeListener.changeListener.addSelectionListener(this, this::updateSelectionPanel);
	}

	public SelectionInfoPanel setModel(ModelHandler modelHandler, ModelEditorManager modelEditorManager) {
		this.modelHandler = modelHandler;
		this.modelEditorManager = modelEditorManager;
		return this;
	}

	public SelectionInfoPanel setModelPanel(ModelPanel modelPanel) {
		if (modelPanel != null) {
			this.modelHandler = modelPanel.getModelHandler();
			this.modelEditorManager = modelPanel.getModelEditorManager();

		} else {
			this.modelHandler = null;
			this.modelEditorManager = null;
		}
		updateSelectionPanel();
		return this;
	}

	void updateSelectionPanel(){
		if(modelHandler != null){
			selectedVerts.setText("" + modelHandler.getModelView().getSelectedVertices().size());
			selectedNodes.setText("" + modelHandler.getModelView().getSelectedIdObjects().size());
			selectedCams.setText("" + modelHandler.getModelView().getSelectedCameraNodes().size());
			selectionCenter.setText("" + modelEditorManager.getSelectionView().getCenter());
			selectionCenter.setToolTipText("" + modelEditorManager.getSelectionView().getCenter());
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
