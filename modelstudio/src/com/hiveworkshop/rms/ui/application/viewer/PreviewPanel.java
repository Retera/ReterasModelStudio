package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.OldRenderer.PerspectiveViewport;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PreviewPanel extends JPanel {
	private final PerspectiveViewport perspectiveViewport;
	private TimeEnvironmentImpl renderEnv;
	private final AnimationController animationController;

	public PreviewPanel() {
		try {
			perspectiveViewport = new PerspectiveViewport();
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
			animationController = new AnimationController(this::setLevelOfDetail);
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
		setLayout(new BorderLayout());
		add(perspectiveViewport, BorderLayout.CENTER);
	}

	public PreviewPanel setModel(ModelHandler modelHandler, boolean doDefaultCamera, ViewportActivityManager activityManager) {
		System.out.println("PreviewPanel#setModel");
		if (modelHandler != null) {
			RenderModel previewRenderModel = modelHandler.getPreviewRenderModel();
			previewRenderModel.setVetoOverrideParticles(true);
			renderEnv = previewRenderModel.getTimeEnvironment();
			renderEnv.setAnimationTime(0);
			renderEnv.setLive(true);

			perspectiveViewport.setModel(modelHandler.getModelView(), previewRenderModel, doDefaultCamera);
//			perspectiveViewport.getCameraHandler().setActivityManager(activityManager);
			perspectiveViewport.getMouseListenerThing().setActivityManager(activityManager);

			animationController.setModel(previewRenderModel, renderEnv.getCurrentAnimation(), true);
		} else {
			animationController.setModel(null, null, true);
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

	public void reloadAllTextures() {
		perspectiveViewport.reloadAllTextures();
	}

	public PreviewPanel reload() {
		perspectiveViewport.reloadTextures();
		return this;
	}

	public PreviewPanel reloadRepaint() {
		animationController.reload().repaint();
		reload().repaint();
		return this;
	}

	public PerspectiveViewport getPerspectiveViewport() {
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
		perspectiveViewport.setLevelOfDetail(levelOfDetail);
	}

	public BufferedImage getBufferedImage() {
		return ViewportRenderExporter.getBufferedImage(perspectiveViewport);
	}

}
