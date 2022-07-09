package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.model.nodepanels.AnimationChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ModelingCreatorToolsView extends ModelDependentView {
	private final CreatorModelingPanel creatorModelingPanel;
	private final ManualTransformPanel transformPanel;
	private final TabWindow tabbedPanel;
	private final View transformView;
	private final View addView;
	private final JPanel animationPanel;
	private final AnimationChooser animationChooser;
	private ModelHandler modelHandler;

public ModelingCreatorToolsView() {
	super("Modeling", null, new JPanel());
	creatorModelingPanel = new CreatorModelingPanel();
	transformPanel = new ManualTransformPanel();
	animationChooser = new AnimationChooser(true, true, false);
	animationPanel = new JPanel(new MigLayout("ins 0, fill", "[grow]", "[][grow]"));
	animationPanel.add(animationChooser, "wrap");
	transformView = getTitledView("Transform", transformPanel);
	addView = getTitledView("Add", creatorModelingPanel);
	tabbedPanel = new TabWindow(new DockingWindow[] {addView, transformView});
	tabbedPanel.setSelectedTab(0);
//	tabbedPanel.setC
//	this.setComponent(creatorModelingPanel);
	this.setComponent(tabbedPanel);
}

	public static View getTitledView(String title, JPanel panel) {
		return new View(title, null, panel);
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
			transformView.setComponent(null);
			animationPanel.add(transformPanel);
			this.setComponent(animationPanel);
		} else {
			if(animationPanel.getComponentCount()>1){
				animationPanel.remove(1);
			}
			transformView.setComponent(transformPanel);
			this.setComponent(tabbedPanel);
		}
		repaint();
	}

	@Override
	public ModelingCreatorToolsView setModelPanel(ModelPanel modelPanel){
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
