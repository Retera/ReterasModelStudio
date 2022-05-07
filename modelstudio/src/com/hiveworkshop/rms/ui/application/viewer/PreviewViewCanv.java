package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportCanvas;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;

import javax.swing.*;

public class PreviewViewCanv extends ModelDependentView {
	private final JSplitPane splitPane;
	private final PreviewPanelCanv previewPanel;
	private final JScrollPane scrollingPane;

	public PreviewViewCanv() {
		super("Preview", null, new JPanel());
		previewPanel = new PreviewPanelCanv();
		scrollingPane = new JScrollPane(previewPanel.getAnimationController());
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, scrollingPane);
		this.setComponent(splitPane);
		splitPane.setDividerLocation(0.8);
	}

	@Override
	public PreviewViewCanv setModelPanel(ModelPanel modelPanel) {
		if (modelPanel == null) {
			previewPanel.setModel(null, false, null);
		} else {
			previewPanel.setModel(modelPanel.getModelHandler(), true, modelPanel.getViewportActivityManager());
		}
		splitPane.setDividerLocation(0.8);
		reload();
		return this;
	}

	public ViewportCanvas getPerspectiveViewport() {
		if (previewPanel != null){
			return previewPanel.getPerspectiveViewport();
		}
		return null;
	}

	@Override
	public PreviewViewCanv reload() {
		if (previewPanel != null) {
			previewPanel.reloadRepaint();
		}
		return this;
	}
}
