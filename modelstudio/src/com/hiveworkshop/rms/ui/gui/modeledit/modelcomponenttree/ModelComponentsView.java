package com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ModelComponentsView extends ModelDependentView {
	private final JSplitPane splitPane;
	private final JScrollPane componentBrowserTreePane;
	private ModelComponentBrowserTree modelComponentBrowserTree;
	private ModelPanel modelPanel;
	private final JPanel contentsTreeDummy;
	private final JPanel contentsDummy;

	public ModelComponentsView() {
		super("Model", null, new JPanel());

		contentsTreeDummy = new JPanel(new MigLayout());
		contentsTreeDummy.add(new JLabel("..."));
		contentsDummy = new JPanel(new MigLayout());
		contentsDummy.add(new JLabel("..."));

		componentBrowserTreePane = new JScrollPane(contentsTreeDummy);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, componentBrowserTreePane, contentsDummy);
		this.setComponent(splitPane);
		splitPane.setDividerLocation(0.2);
	}

	@Override
	public ModelComponentsView setModelPanel(ModelPanel modelPanel){
		this.modelPanel = modelPanel;
		if(modelPanel == null){
			componentBrowserTreePane.setViewportView(contentsTreeDummy);
			modelComponentBrowserTree = null;
			splitPane.setRightComponent(contentsDummy);
		} else {
			modelComponentBrowserTree = new ModelComponentBrowserTree(modelPanel.getModelHandler());

			splitPane.setRightComponent(modelComponentBrowserTree.getComponentsPanel());
			componentBrowserTreePane.setViewportView(modelComponentBrowserTree);

		}
		splitPane.setDividerLocation(0.2);
		splitPane.repaint();
		return this;
	}

	@Override
	public ModelComponentsView reload() {
		if (modelComponentBrowserTree != null) {
			modelComponentBrowserTree.reloadFromModelView().repaint();
		}
		return this;
	}
}
