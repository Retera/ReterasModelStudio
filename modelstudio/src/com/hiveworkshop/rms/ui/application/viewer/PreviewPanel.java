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

	public PreviewPanel() {
		try {
			perspectiveViewport = new PerspectiveViewport();
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
		setLayout(new BorderLayout());
		add(perspectiveViewport, BorderLayout.CENTER);
	}

	public PreviewPanel setModel(ModelHandler modelHandler, boolean doDefaultCamera, ViewportActivityManager activityManager) {
		System.out.println("PreviewPanel#setModel");
		if (modelHandler != null) {
			modelHandler.getModelView().setVetoOverrideParticles(true);
			RenderModel previewRenderModel = modelHandler.getPreviewRenderModel();
			perspectiveViewport.setModel(modelHandler.getModelView(), previewRenderModel, doDefaultCamera);
			renderEnv = previewRenderModel.getTimeEnvironment();
			renderEnv.setAnimationTime(0);
			renderEnv.setLive(true);
			perspectiveViewport.getCameraHandler().setActivityManager(activityManager);
			perspectiveViewport.getMouseListenerThing().setActivityManager(activityManager);
		} else {
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
//		perspectiveViewport.paint(perspectiveViewport.getGraphics());
	}

	public void reloadAllTextures() {
		perspectiveViewport.reloadAllTextures();
	}

	public PreviewPanel reload() {
		perspectiveViewport.reloadTextures();
		return this;
	}

	public void setAnimation(Animation animation) {
		if (renderEnv != null) {
			renderEnv.setAnimation(animation);
		}
	}

//	public void setAnimationTime(int time) {
//		if(renderEnv != null){
//			renderEnv.setAnimationTime(time);
//		}
//	}

	public void playAnimation() {
		if (renderEnv != null) {
			renderEnv.setRelativeAnimationTime(0);
			renderEnv.setLive(true);
		}
	}

	public void setLoop(PreviewPanel.LoopType loopType) {
		if (renderEnv != null) {
			renderEnv.setLoopType(loopType);
		}
	}

	public void setSpeed(float speed) {
		if(renderEnv != null){
			renderEnv.setAnimationSpeed(speed);
		}
	}

	public Animation getCurrentAnimation() {
		if(renderEnv != null){
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

	public enum LoopType {
		DEFAULT_LOOP, ALWAYS_LOOP, NEVER_LOOP
	}
}
