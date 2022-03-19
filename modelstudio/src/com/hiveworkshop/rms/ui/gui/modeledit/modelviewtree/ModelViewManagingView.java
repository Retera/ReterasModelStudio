package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;

import javax.swing.*;

public class ModelViewManagingView extends ModelDependentView {
	ComponentThingTree modelViewManagingTree2;
	JScrollPane modelEditingTreePane;
	ModelPanel modelPanel;
	JPanel jPanel;
	String name;

	public ModelViewManagingView() {
		super("Outliner", null, new JPanel());
		this.name = "Outliner";
		jPanel = new JPanel();
		jPanel.add(new JLabel("..."));
		modelEditingTreePane = new JScrollPane(jPanel);
		modelViewManagingTree2 = new ComponentThingTree();

		setComponent(modelEditingTreePane);
	}

	@Override
	public ModelViewManagingView setModelPanel(ModelPanel modelPanel){
		this.modelPanel = modelPanel;
		if(modelPanel == null) {
			modelEditingTreePane.setViewportView(jPanel);
		} else {
			modelViewManagingTree2.setModel(modelPanel.getModelHandler());
			modelEditingTreePane.setViewportView(modelViewManagingTree2);
			modelViewManagingTree2.expandMeshNode();
		}
		System.out.println("name: " + name + ", panel: " + modelPanel);
		return this;
	}

	@Override
	public ModelViewManagingView reload() {
		if (modelPanel != null) {
			modelViewManagingTree2.reloadTree().repaint();
		}
		return this;
	}
}
