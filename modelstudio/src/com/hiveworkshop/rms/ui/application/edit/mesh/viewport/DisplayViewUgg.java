package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class DisplayViewUgg extends View {
	DisplayPanel displayPanel;
	ModelPanel modelPanel;
	String name;

	public DisplayViewUgg(String s, Icon icon, Component component) {
		super(s, icon, component);
		this.name = s;
	}
	public DisplayViewUgg(String s) {
		super(s, null, new JPanel());
		this.name = s;
	}

	public DisplayViewUgg setModelPanel(ModelPanel modelPanel){
		this.modelPanel = modelPanel;
		if(modelPanel == null){
			displayPanel = null;
			this.setComponent(new JPanel());
		} else {
			displayPanel = modelPanel.getDisplayPanel(name, (byte) 1, (byte) 2);
			displayPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
		}
		return this;
	}
}
