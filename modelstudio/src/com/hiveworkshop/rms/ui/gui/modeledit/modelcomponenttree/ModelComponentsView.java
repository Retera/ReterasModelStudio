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
//		contentsDummy.setMinimumSize(new Dimension(200, 200));

		contentPanel = new JPanel(new MigLayout("fill, gap 0, ins 0"));

		componentBrowserTreePane = new JScrollPane(contentsDummy);
//		componentBrowserTreePane.setPreferredSize(new Dimension(220, 300));

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
//			modelComponentBrowserTree.setMinimumSize(new Dimension(220, 300));

			contentPanel.add(modelComponentBrowserTree.getComponentsPanel());
			componentBrowserTreePane.setViewportView(modelComponentBrowserTree);

		}
//		splitPane.revalidate();
		splitPane.setDividerLocation(0.2);
		splitPane.repaint();
		return this;
	}
}
