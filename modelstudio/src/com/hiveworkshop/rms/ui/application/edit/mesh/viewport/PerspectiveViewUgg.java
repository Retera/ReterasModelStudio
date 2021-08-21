package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.viewer.PerspectiveViewport;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class PerspectiveViewUgg extends ModelDependentView {
	PerspDisplayPanel perspDisplayPanel;
	ModelPanel modelPanel;
	String name;
	JPanel dudPanel;

	public PerspectiveViewUgg() {
		super("Perspective", null, new JPanel());
		this.name = "Perspective";
		dudPanel = new JPanel(new MigLayout());
		setComponent(dudPanel);
	}

	@Override
	public PerspectiveViewUgg setModelPanel(ModelPanel modelPanel){
		this.modelPanel = modelPanel;
		if(modelPanel == null){
			this.setComponent(dudPanel);
			perspDisplayPanel = null;
		} else {
//			perspDisplayPanel = modelPanel.getPerspArea();
			perspDisplayPanel = new PerspDisplayPanel("Perspective").setModel(modelPanel.getModelHandler());
			this.setComponent(perspDisplayPanel);
		}
		System.out.println("name: " + name + ", panel: " + modelPanel);
		return this;
	}

	@Override
	public PerspectiveViewUgg reload() {
		if (perspDisplayPanel != null) {
			perspDisplayPanel.reloadTextures();
		}
		return this;
	}

	public PerspDisplayPanel getPerspDisplayPanel() {
		return perspDisplayPanel;
	}
	public PerspectiveViewport getPerspectiveViewport() {
		if (perspDisplayPanel != null){
			return perspDisplayPanel.getViewport();
		}
		return null;
	}

}
