package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.model.nodepanels.AnimationChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.CollapsablePanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ModelingCreatorToolsView extends ModelDependentView {
	private final SelectionInfoPanel selectionPanel;
	private final CreatorModelingPanel creatorModelingPanel;
	private final ManualTransformPanel transformPanel;
	private final JPanel modelingPanel;
	private final JPanel animationPanel;
	private final AnimationChooser animationChooser;
	private ModelHandler modelHandler;

	public ModelingCreatorToolsView() {
		super("Modeling", null, new JPanel());
		selectionPanel = new SelectionInfoPanel();
		creatorModelingPanel = new CreatorModelingPanel();
		transformPanel = new ManualTransformPanel();
		animationChooser = new AnimationChooser(true, true, false);
//		animationPanel = new JPanel(new MigLayout("ins 0, fill", "[grow]", "[][grow]"));
		animationPanel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow]", "[][]"));
		animationPanel.add(new JLabel("Animation"), "wrap");
		animationPanel.add(animationChooser);


		JPanel panel = new JPanel(new MigLayout("fill, ins 0, gap 0, hidemode 2", "", "[top][top][top][top][top, grow]"));
		panel.add(animationPanel, "growx, spanx, wrap");
		animationPanel.setVisible(false);

		panel.add(getCP("Selection", selectionPanel), "top, growx, spanx, wrap");
		panel.add(getCP("Transform", transformPanel), "top, growx, spanx, wrap");
		panel.add(getCP("Add", creatorModelingPanel), "top, growx, spanx, wrap");


		panel.add(new JPanel(), "top, growx, growy, spanx, wrap");
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		// cheat to not show scrollbars while keeping the scroll pane scrollable
		JPanel tempPanel = new JPanel();
		tempPanel.add(scrollPane.getHorizontalScrollBar());
		tempPanel.add(scrollPane.getVerticalScrollBar());

		modelingPanel = panel;
		this.setComponent(scrollPane);
	}

	public static View getTitledView(String title, JPanel panel) {
		return new View(title, null, panel);
	}
	private CollapsablePanel getCP(String title, JPanel panel) {
		return new CollapsablePanel(title, panel);
	}

	public CreatorModelingPanel getCreatorModelingPanel() {
		return creatorModelingPanel;
	}

	public void reloadAnimationList() {
		creatorModelingPanel.updateAnimationList();
		animationChooser.updateAnimationList();
	}

	public void setAnimationModeState(boolean animationModeState) {
		creatorModelingPanel.setAnimationModeState(animationModeState);
		transformPanel.setAnimationState(animationModeState);
		if (animationModeState){
			animationPanel.setVisible(true);
		} else {
			animationPanel.setVisible(false);
		}
		repaint();
	}

	@Override
	public ModelingCreatorToolsView setModelPanel(ModelPanel modelPanel){
		selectionPanel.setModelPanel(modelPanel);
		creatorModelingPanel.setModelPanel(modelPanel);
		transformPanel.setModelPanel(modelPanel);
		if(modelPanel != null){
			modelHandler = modelPanel.getModelHandler();
			animationChooser.setModel(modelHandler.getModel(), modelHandler.getRenderModel());
		} else {
			animationChooser.setModel(null, null);
			modelHandler = null;
		}
		return this;
	}

}
