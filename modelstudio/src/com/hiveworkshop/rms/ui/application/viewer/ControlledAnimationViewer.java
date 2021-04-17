package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ControlledAnimationViewer extends JPanel implements AnimationControllerListener {
	private ModelView modelView;
	private final AnimatedPerspectiveViewport perspectiveViewport;
	ComPerspRenderEnv renderEnv;

	public ControlledAnimationViewer(final ModelView modelView, final ProgramPreferences programPreferences, final boolean doDefaultCamera) {
		this.modelView = modelView;
		try {
			renderEnv = new ComPerspRenderEnv();
			modelView.setVetoOverrideParticles(true);
			RenderModel renderModel = new RenderModel(modelView.getModel(), modelView);
			perspectiveViewport = new AnimatedPerspectiveViewport(modelView, renderModel, programPreferences, renderEnv, doDefaultCamera);
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
			renderEnv.setAnimationTime(0);
			renderEnv.setLive(true);
		} catch (final LWJGLException e) {
			throw new RuntimeException(e);
		}
		setLayout(new BorderLayout());
		add(perspectiveViewport, BorderLayout.CENTER);
	}

	public void setModel(final ModelView modelView) {
		this.modelView = modelView;
		perspectiveViewport.setModel(modelView);
		reload();
	}

	public void setTitle(final String title) {
		setBorder(BorderFactory.createTitledBorder(title));
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		// perspectiveViewport.repaint();
		perspectiveViewport.paint(perspectiveViewport.getGraphics());
	}

	public void reloadAllTextures() {
		perspectiveViewport.reloadAllTextures();
	}

	public void reload() {
		perspectiveViewport.reloadTextures();
	}

	@Override
	public void setAnimation(final Animation animation) {
//		perspectiveViewport.setAnimation(animation);
		renderEnv.setAnimation(animation);
	}

	public void setAnimationTime(int time) {
//		perspectiveViewport.setAnimationTime(time);
		renderEnv.setAnimationTime(time);
	}

	@Override
	public void playAnimation() {
//		perspectiveViewport.setAnimationTime(0);
		renderEnv.setLive(true);
		renderEnv.setAnimationTime(0);

	}

	@Override
	public void setLoop(final LoopType loopType) {
//		perspectiveViewport.setLoopType(loopType);
		renderEnv.setLoopType(loopType);
	}

	@Override
	public void setSpeed(final float speed) {
//		perspectiveViewport.setAnimationSpeed(speed);
		renderEnv.setAnimationSpeed(speed);
	}

	public Animation getCurrentAnimation() {
//		return perspectiveViewport.getCurrentAnimation();
		return renderEnv.getCurrentAnimation();
	}

//	public void setSpawnParticles(final boolean b) {
//		perspectiveViewport.setSpawnParticles(b);
//	}

	@Override
	public void setLevelOfDetail(final int levelOfDetail) {
		perspectiveViewport.setLevelOfDetail(levelOfDetail);
	}

	public BufferedImage getBufferedImage() {
		return perspectiveViewport.getBufferedImage();
	}
}
