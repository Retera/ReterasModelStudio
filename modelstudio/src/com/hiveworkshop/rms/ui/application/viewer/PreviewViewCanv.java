package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportCanvas;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import com.hiveworkshop.rms.util.TinyToggleButton;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class PreviewViewCanv extends ModelDependentView {
	private final JSplitPane splitPane;
	private final PreviewPanelCanv previewPanel;
	private final JScrollPane scrollingPane;

	TinyToggleButton renderTextures;
	TinyToggleButton wireFrame;
	TinyToggleButton showNormals;
	TinyToggleButton show3dVerts;

	public PreviewViewCanv() {
		super("Preview", null, new JPanel());
		previewPanel = new PreviewPanelCanv();
		scrollingPane = new JScrollPane(previewPanel.getAnimationController());
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, scrollingPane);
		this.setComponent(splitPane);
		splitPane.setDividerLocation(0.8);

		renderTextures =    getButton("\u26FE", true, b -> previewPanel.setRenderTextures(b));
		wireFrame =         getButton("\u2342", false, b -> previewPanel.setWireFrame(b));
		show3dVerts =       getButton("\u26DA", false, b -> previewPanel.setShow3dVerts(b));
		showNormals =       getButton("\u23CA", false, b -> previewPanel.setShowNormals(b));
//		renderTextures =    getButton("texture", true, b -> displayPanel.setRenderTextures(b));
//		wireFrame =         getButton("wireframe", false, b -> displayPanel.setWireFrame(b));
//		show3dVerts =       getButton("verts", true, b -> displayPanel.setShow3dVerts(b));
//		showNormals =       getButton("normals", false, b -> displayPanel.setShowNormals(b));

		getCustomTitleBarComponents().add(renderTextures);
		getCustomTitleBarComponents().add(wireFrame);
		getCustomTitleBarComponents().add(show3dVerts);
		getCustomTitleBarComponents().add(showNormals);
	}

	@Override
	public PreviewViewCanv setModelPanel(ModelPanel modelPanel) {
		if (modelPanel == null) {
			previewPanel.setModel(null, false, null);
		} else {
//			previewPanel.setModel(modelPanel.getModelHandler(), true, modelPanel.getViewportActivityManager());
			previewPanel.setModel(modelPanel.getModelHandler(), true, null);
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


	Color onC = new Color(255, 255, 255);
	Color offC = new Color(100, 100, 100);
	private TinyToggleButton getButton(String text, boolean initial, Consumer<Boolean> boolConsumer){
		TinyToggleButton button = new TinyToggleButton(text, onC, offC, boolConsumer);
		button.setOn(initial);
		boolConsumer.accept(initial);
		return button;

	}
}
