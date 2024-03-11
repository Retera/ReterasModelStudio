package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportPanel;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportCanvas;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import com.hiveworkshop.rms.util.TinyToggleButton;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class PreviewViewCanv extends ModelDependentView {
	private final JSplitPane splitPane;
	private final ViewportPanel viewportPanel;
	private final JScrollPane scrollingPane;
	private final AnimationController animationController;

	TinyToggleButton renderTextures;
	TinyToggleButton wireFrame;
	TinyToggleButton showNormals;
	TinyToggleButton show3dVerts;

	public PreviewViewCanv() {
		super("Preview", null, new JPanel());
		viewportPanel = new ViewportPanel(false, false);
		animationController = new AnimationController(viewportPanel.getViewport()::setLevelOfDetail);
		scrollingPane = new JScrollPane(animationController);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewportPanel, scrollingPane);
		this.setComponent(splitPane);
		splitPane.setDividerLocation(0.8);

		renderTextures =    getButton("\u26FE", true, b -> viewportPanel.setRenderTextures(b));
		wireFrame =         getButton("\u2342", false, b -> viewportPanel.setWireFrame(b));
		show3dVerts =       getButton("\u26DA", false, b -> viewportPanel.setShow3dVerts(b));
		showNormals =       getButton("\u23CA", false, b -> viewportPanel.setShowNormals(b));
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
			viewportPanel.setModel(null, null, true);
			animationController.setModel(null, null, true);
		} else {
//			previewPanel.setModel(modelPanel.getModelHandler(), true, modelPanel.getViewportActivityManager());
			ModelHandler modelHandler = modelPanel.getModelHandler();
			RenderModel previewRenderModel = modelHandler.getPreviewRenderModel();
//			previewRenderModel.setVetoOverrideParticles(true);
			TimeEnvironmentImpl renderEnv = previewRenderModel.getTimeEnvironment();
			renderEnv.setAnimationTime(0);
			renderEnv.setLive(true);
			viewportPanel.setModel(previewRenderModel, null, true);
			animationController.setModel(previewRenderModel, renderEnv.getCurrentAnimation(), true);
		}
		splitPane.setDividerLocation(0.8);
		reload();
		return this;
	}

	@Override
	public PreviewViewCanv preferencesUpdated(){
		if(viewportPanel != null){
			viewportPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
		}
		return this;
	}

	@Override
	public PreviewViewCanv reload() {
		if (viewportPanel != null) {
			animationController.reload().repaint();
			viewportPanel.reload().repaint();
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

	public ViewportCanvas getPerspectiveViewport() {
		if (viewportPanel != null){
			return viewportPanel.getViewport();
		}
		return null;
	}
}
