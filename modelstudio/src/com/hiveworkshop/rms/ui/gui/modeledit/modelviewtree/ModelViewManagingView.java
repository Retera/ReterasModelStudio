package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;

import javax.swing.*;

public class ModelViewManagingView extends ModelDependentView {
//	ModelViewManagingTree modelViewManagingTree;
ModelViewManagingTree modelViewManagingTree;
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

		setComponent(modelEditingTreePane);
	}

	@Override
	public ModelViewManagingView setModelPanel(ModelPanel modelPanel){
		this.modelPanel = modelPanel;
		if(modelPanel == null) {
			modelViewManagingTree = null;
			modelEditingTreePane.setViewportView(jPanel);
		} else {
//			modelViewManagingTree = new ModelViewManagingTree(modelPanel.getModelHandler(), modelPanel.getModelEditorManager());
//			modelEditingTreePane = new JScrollPane(modelViewManagingTree);
			modelViewManagingTree = modelPanel.getModelEditingTree();
			modelEditingTreePane.setViewportView(modelViewManagingTree);
		}
		System.out.println("name: " + name + ", panel: " + modelPanel);
		return this;
	}

	@Override
	public ModelViewManagingView reload() {
		if (modelViewManagingTree != null) {
			modelViewManagingTree.reloadFromModelView().repaint();
		}
		return this;
	}
}
