package com.hiveworkshop.rms.ui.gui.modeledit.creator;


import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;

import javax.swing.*;

public class ModelingCreatorToolsView extends ModelDependentView {
	private final CreatorModelingPanel creatorModelingPanel;
//	public ModelingCreatorToolsView() {
//		super("Modeling", null, new JPanel());
//		creatorModelingPanel = new CreatorModelingPanel(ProgramGlobals.getMainPanel().getViewportListener());
//		this.setComponent(creatorModelingPanel);
//	}
public ModelingCreatorToolsView() {
	super("Modeling", null, new JPanel());
	creatorModelingPanel = new CreatorModelingPanel();
	this.setComponent(creatorModelingPanel);
}

	public CreatorModelingPanel getCreatorModelingPanel() {
		return creatorModelingPanel;
	}

	public void reloadAnimationList() {
		creatorModelingPanel.reloadAnimationList();
	}

	public void setAnimationModeState(boolean animationModeState) {
		creatorModelingPanel.setAnimationModeState(animationModeState);
	}

	@Override
	public ModelingCreatorToolsView setModelPanel(ModelPanel modelPanel){
		creatorModelingPanel.setModelPanel(modelPanel);
		return this;
	}

}
