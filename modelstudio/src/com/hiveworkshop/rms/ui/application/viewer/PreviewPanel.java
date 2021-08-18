package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PreviewPanel extends JPanel {
	private final PerspectiveViewport perspectiveViewport;
	TimeEnvironmentImpl renderEnv;
	private ModelHandler modelHandler;

	public PreviewPanel(ModelHandler modelHandler, boolean doDefaultCamera, ViewportActivityManager activityManager) {
		this.modelHandler = modelHandler;
		try {
			modelHandler.getModelView().setVetoOverrideParticles(true);
			RenderModel previewRenderModel = modelHandler.getPreviewRenderModel();
			perspectiveViewport = new PerspectiveViewport(modelHandler.getModelView(), previewRenderModel, doDefaultCamera);
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
			renderEnv = previewRenderModel.getTimeEnvironment();
			renderEnv.setAnimationTime(0);
			renderEnv.setLive(true);
			perspectiveViewport.getCameraHandler().setActivityManager(activityManager);
			perspectiveViewport.getMouseListenerThing().setActivityManager(activityManager);
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
		setLayout(new BorderLayout());
		add(perspectiveViewport, BorderLayout.CENTER);
	}

	public void setTitle(String title) {
		setBorder(BorderFactory.createTitledBorder(title));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		perspectiveViewport.paint(perspectiveViewport.getGraphics());
	}

	public void reloadAllTextures() {
		perspectiveViewport.reloadAllTextures();
	}

	public PreviewPanel reload() {
		perspectiveViewport.reloadTextures();
		return this;
	}

	public void setAnimation(Animation animation) {
		renderEnv.setAnimation(animation);
	}

	public void setAnimationTime(int time) {
		renderEnv.setAnimationTime(time);
	}

	public void playAnimation() {
		renderEnv.setRelativeAnimationTime(0);
		renderEnv.setLive(true);
	}

	public void setLoop(PreviewPanel.LoopType loopType) {
		renderEnv.setLoopType(loopType);
	}

	public void setSpeed(float speed) {
		renderEnv.setAnimationSpeed(speed);
	}

	public Animation getCurrentAnimation() {
		return renderEnv.getCurrentAnimation();
	}

	public void setLevelOfDetail(int levelOfDetail) {
		perspectiveViewport.setLevelOfDetail(levelOfDetail);
	}

	public BufferedImage getBufferedImage() {
		return ViewportRenderExporter.getBufferedImage(perspectiveViewport);
	}

	public enum LoopType {
		DEFAULT_LOOP, ALWAYS_LOOP, NEVER_LOOP
	}
}
