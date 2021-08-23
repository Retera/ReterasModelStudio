package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class DisplayViewUgg extends ModelDependentView {
	DisplayPanel displayPanel;
	ModelPanel modelPanel;
	String name;
	JPanel dudPanel;

	public DisplayViewUgg(String s) {
		super(s, null, new JPanel());
		this.name = s;
		dudPanel = new JPanel(new MigLayout());
		displayPanel = new DisplayPanel();
		this.setComponent(dudPanel);
	}

	@Override
	public DisplayViewUgg setModelPanel(ModelPanel modelPanel){
		this.modelPanel = modelPanel;
//		if(displayPanel == null){
//			displayPanel = new DisplayPanel();
//		}
		if (modelPanel == null) {
			this.setComponent(dudPanel);
//			displayPanel = null;
			displayPanel.setModel(null, null);
		} else {
//			displayPanel = modelPanel.getDisplayPanel(name, (byte) 1, (byte) 2);
//			displayPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
//			this.setComponent(displayPanel);
//			displayPanel =  new DisplayPanel();
			displayPanel.setModel(modelPanel.getModelHandler(), modelPanel.getViewportActivityManager());
			displayPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
			this.setComponent(displayPanel);
		}
		System.out.println("name: " + name + ", panel: " + modelPanel);
		return this;
	}

	@Override
	public DisplayViewUgg preferencesUpdated(){
		if(displayPanel != null){
			displayPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
		}
		return this;
	}

	@Override
	public DisplayViewUgg reload() {
		if (modelPanel != null) {
			displayPanel.reload().repaint();
		}
		return this;
	}
}
