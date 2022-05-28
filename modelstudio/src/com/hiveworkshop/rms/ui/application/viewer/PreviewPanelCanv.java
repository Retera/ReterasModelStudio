package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportCanvas;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportSettings;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PreviewPanelCanv extends JPanel {
	private final ViewportCanvas perspectiveViewport;
	private final ViewportSettings viewportSettings;
	private TimeEnvironmentImpl renderEnv;
	private final AnimationController animationController;

	public PreviewPanelCanv() {
		try {
			perspectiveViewport = new ViewportCanvas(ProgramGlobals.getPrefs());
			viewportSettings = perspectiveViewport.getViewportSettings();
			viewportSettings.setShowNodes(false);
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
			animationController = new AnimationController(this::setLevelOfDetail);
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
		setLayout(new BorderLayout());
		add(perspectiveViewport, BorderLayout.CENTER);
	}

	public PreviewPanelCanv setModel(ModelHandler modelHandler, boolean doDefaultCamera, ViewportActivityManager activityManager) {
		System.out.println("PreviewPanel#setModel");
		if (modelHandler != null) {
			RenderModel previewRenderModel = modelHandler.getPreviewRenderModel();
			previewRenderModel.setVetoOverrideParticles(true);
			renderEnv = previewRenderModel.getTimeEnvironment();
			renderEnv.setAnimationTime(0);
			renderEnv.setLive(true);

//			perspectiveViewport.setModel(modelHandler.getModelView(), previewRenderModel, doDefaultCamera);
			perspectiveViewport.setModel(modelHandler.getModelView(), previewRenderModel, true);
			perspectiveViewport.getMouseAdapter().setActivityManager(activityManager);

			animationController.setModel(modelHandler, true, renderEnv.getCurrentAnimation());
		} else {
			animationController.setModel(null, true, null);
			perspectiveViewport.setModel(null, null, doDefaultCamera);
			renderEnv = null;
		}
		return this;
	}

	public void setTitle(String title) {
		setBorder(BorderFactory.createTitledBorder(title));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

//	public void reloadAllTextures() {
//		perspectiveViewport.reloadAllTextures();
//	}

	public PreviewPanelCanv reload() {
//		perspectiveViewport.reloadTextures();
		return this;
	}

	public PreviewPanelCanv reloadRepaint() {
		animationController.reload().repaint();
		reload().repaint();
		return this;
	}

	public ViewportCanvas getPerspectiveViewport() {
		return perspectiveViewport;
	}
	public AnimationController getAnimationController() {
		return animationController;
	}

	public Animation getCurrentAnimation() {
		if (renderEnv != null) {
			return renderEnv.getCurrentAnimation();
		}
		return null;
	}

	public void setLevelOfDetail(int levelOfDetail) {
//		perspectiveViewport.setLevelOfDetail(levelOfDetail);
	}

	public BufferedImage getBufferedImage() {
//		return ViewportRenderExporter.getBufferedImage(perspectiveViewport);
		return null;
	}



	public PreviewPanelCanv setRenderTextures(boolean renderTextures) {
		viewportSettings.setRenderTextures(renderTextures);
		return this;
	}

	public PreviewPanelCanv setWireFrame(boolean wireFrame) {
		viewportSettings.setWireFrame(wireFrame);
		return this;
	}

	public PreviewPanelCanv setShowNormals(boolean showNormals) {
		viewportSettings.setShowNormals(showNormals);
		return this;
	}

	public PreviewPanelCanv setShow3dVerts(boolean show3dVerts) {
		viewportSettings.setShow3dVerts(show3dVerts);
		return this;
	}

	public PreviewPanelCanv setOrtho(boolean ortho){
		perspectiveViewport.getCameraManager().setOrtho(ortho);
		return this;
	}

}
