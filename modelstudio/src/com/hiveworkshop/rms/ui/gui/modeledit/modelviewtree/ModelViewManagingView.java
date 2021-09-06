package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;

import javax.swing.*;

public class ModelViewManagingView extends ModelDependentView {
	ModelViewManagingTree modelViewManagingTree;
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
		modelViewManagingTree = new ModelViewManagingTree();
		modelViewManagingTree2 = new ComponentThingTree();

		setComponent(modelEditingTreePane);
	}

	@Override
	public ModelViewManagingView setModelPanel(ModelPanel modelPanel){
		this.modelPanel = modelPanel;
		if(modelPanel == null) {
//			modelViewManagingTree = null;
			modelEditingTreePane.setViewportView(jPanel);
		} else {
//			modelViewManagingTree = modelPanel.getModelEditingTree();
//			modelEditingTreePane.setViewportView(modelViewManagingTree);
//			modelViewManagingTree = new ModelViewManagingTree().setModel(modelPanel.getModelHandler(), modelPanel.getModelEditorManager());
//			modelViewManagingTree.setModel(modelPanel.getModelHandler());
			modelViewManagingTree2.setModel(modelPanel.getModelHandler());

//			modelEditingTreePane.setViewportView(modelViewManagingTree);
			modelEditingTreePane.setViewportView(modelViewManagingTree2);
		}
//		reload();
		System.out.println("name: " + name + ", panel: " + modelPanel);
		return this;
	}

	@Override
	public ModelViewManagingView reload() {
		if (modelPanel != null) {
//			modelViewManagingTree.reloadFromModelView().repaint();
			modelViewManagingTree2.setModel(modelPanel.getModelHandler()).repaint();
		}
//		if (modelViewManagingTree != null) {
//			modelViewManagingTree.reloadFromModelView().repaint();
//		}
		return this;
	}
}
