package com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ModelComponentsView extends ModelDependentView {
	private final JPanel contentPanel;
	private final JSplitPane splitPane;
	private final JScrollPane componentBrowserTreePane;
	private ModelComponentBrowserTree modelComponentBrowserTree;
	private ModelPanel modelPanel;
	private final JPanel contentsDummy;

	public ModelComponentsView() {
		super("Model", null, new JPanel());

		contentsDummy = new JPanel(new MigLayout());
		contentsDummy.add(new JLabel("..."));

		contentPanel = new JPanel(new MigLayout("fill, gap 0, ins 0"));

		componentBrowserTreePane = new JScrollPane(contentsDummy);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, componentBrowserTreePane, contentPanel);
		this.setComponent(splitPane);
		splitPane.setDividerLocation(0.2);
	}

	@Override
	public ModelComponentsView setModelPanel(ModelPanel modelPanel){
		this.modelPanel = modelPanel;
		contentPanel.removeAll();
		if(modelPanel == null){
			componentBrowserTreePane.setViewportView(contentsDummy);
			modelComponentBrowserTree = null;
		} else {
			modelComponentBrowserTree = new ModelComponentBrowserTree(modelPanel.getModelHandler());

			contentPanel.add(modelComponentBrowserTree.getComponentsPanel());
			componentBrowserTreePane.setViewportView(modelComponentBrowserTree);

		}
		splitPane.setDividerLocation(0.2);
		splitPane.repaint();
//		reload();
//		System.out.println("done");
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
