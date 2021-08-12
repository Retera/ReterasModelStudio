package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;

import javax.swing.*;

public class ModelViewManagingView extends ModelDependentView {
//	ModelViewManagingTree modelViewManagingTree;
	JScrollPane modelEditingTreePane;
	ModelPanel modelPanel;
	JPanel jPanel;
	String name;

	public ModelViewManagingView() {
		super("Outliner", null, new JPanel());
		this.name = "Outliner";
		jPanel = new JPanel();
		jPanel.add(new JLabel("..."));
		setComponent(jPanel);
	}

	@Override
	public ModelViewManagingView setModelPanel(ModelPanel modelPanel){
		this.modelPanel = modelPanel;
		if(modelPanel == null){
			this.setComponent(jPanel);
		} else {
//			modelViewManagingTree = new ModelViewManagingTree(modelPanel.getModelHandler(), modelPanel.getModelEditorManager());
//			modelEditingTreePane = new JScrollPane(modelViewManagingTree);
			modelEditingTreePane = modelPanel.getModelEditingTreePane();
			this.setComponent(modelEditingTreePane);
		}
		System.out.println("name: " + name + ", panel: " + modelPanel);
		return this;
	}
}
