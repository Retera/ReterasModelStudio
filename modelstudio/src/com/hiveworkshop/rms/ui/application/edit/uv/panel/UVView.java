package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class UVView extends ModelDependentView {
	UVPanel panel;
	JPanel dudPanel;
	public UVView(String s) {
		super(s, null, new JPanel());
		panel = new UVPanel();
		dudPanel = new JPanel(new MigLayout());
		this.setComponent(dudPanel);
	}

	@Override
	public UVView setModelPanel(ModelPanel modelPanel) {
		if(modelPanel == null){

			this.setComponent(dudPanel);
		} else {
			this.setComponent(panel.setModel(modelPanel.getModelHandler()).getMenuHolderPanel());
		}
		return null;
	}

	@Override
	public UVView reload() {
		panel.repaint();
		return this;
	}
}
