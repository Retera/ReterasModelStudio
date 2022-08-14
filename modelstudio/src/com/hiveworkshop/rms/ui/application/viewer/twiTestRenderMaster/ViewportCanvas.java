package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.viewer.KeylistenerThing;
import com.hiveworkshop.rms.ui.application.viewer.MouseListenerThing;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.CameraManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.PortraitCameraManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.ViewportHelpers;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class ViewportCanvas extends SmarterAWTGLCanvas {
	private final CameraManager cameraManager;
	private final ViewportSettings viewportSettings = new ViewportSettings();
	private final MouseListenerThing mouseAdapter;
	private final KeylistenerThing keyAdapter;
	private BufferFiller bufferFiller;
	private Timer paintTimer;
	private long exceptionTimeout = 0;
	private boolean doPaint = true;

	public ViewportCanvas(ProgramPreferences programPreferences) throws LWJGLException {
		this(programPreferences, false);
	}
	public ViewportCanvas(ProgramPreferences programPreferences, boolean portrait) throws LWJGLException {
		super();
		if(portrait){
			cameraManager = new PortraitCameraManager(this);
		} else {

			cameraManager = new CameraManager(this);
		}
		mouseAdapter = new MouseListenerThing(cameraManager, programPreferences);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);

		keyAdapter = new KeylistenerThing(cameraManager, programPreferences);
		addKeyListener(keyAdapter);

		paintTimer = new Timer(16, e -> {
//		paintTimer = new Timer(200, e -> {
			repaint();
			if (!isShowing()) {
				paintTimer.stop();
			}

		});
		paintTimer.start();
	}
	public CameraManager getCameraHandler() {
		return cameraManager;
	}

	public ViewportCanvas loadDefaultCameraFor(double extent) {
		cameraManager.loadDefaultCameraFor(extent);
		return this;
	}

	public ViewportCanvas setModel(RenderModel renderModel) {
		if(renderModel != null){
			ExtLog extents = renderModel.getModel().getExtents();
			cameraManager.loadDefaultCameraFor(ViewportHelpers.getBoundsRadius(renderModel.getTimeEnvironment(), extents));
			bufferFiller = renderModel.getBufferFiller();
		}
		return this;
	}


	public ShaderManager getShaderManager() {
		return bufferFiller.getShaderManager();
	}


	public void setCamera(final Camera camera) {
		if(cameraManager instanceof PortraitCameraManager){
//			((PortraitCameraManager)cameraManager).setModelInstance(renderModel, camera);
		}
	}

	@Override
	public void initGL() {
//		System.out.println("initGL");
		if(bufferFiller != null){
			bufferFiller.initGL();
		}
	}

	@Override
	public void paintGL() {
		setSize(getParent().getSize());
		if(doPaint && bufferFiller != null){
			if(bufferConsumer != null){
				final Consumer<ByteBuffer> bc = bufferConsumer;
				ByteBuffer pixels = bufferFiller.paintGL2(cameraManager, viewportSettings, getWidth(), getHeight());
				SwingUtilities.invokeLater(() -> {
					bc.accept(pixels);
				});
				bufferConsumer = null;
			}
			bufferFiller.paintCanvas(viewportSettings, cameraManager, mouseAdapter, this);
			try {
				swapBuffers();
			} catch (LWJGLException e) {
				e.printStackTrace();
				paintTimer.stop();
				doPaint = false;
				exceptionTimeout = System.currentTimeMillis() + 2000L;
			}
		}

		if (isShowing() && !paintTimer.isRunning()) {
			if(exceptionTimeout < System.currentTimeMillis()){
				doPaint = true;
				paintTimer.restart();
			}
		} else if (!isShowing() && paintTimer.isRunning()) {
			paintTimer.stop();
		}
	}

	Consumer<ByteBuffer> bufferConsumer;
	public void setPixelBufferListener(Consumer<ByteBuffer> bufferConsumer){
		this.bufferConsumer = bufferConsumer;
	}


	@Override
	public void update(Graphics g) {
		paint(g);
	}

	public ViewportSettings getViewportSettings() {
		return viewportSettings;
	}

	public KeylistenerThing getKeyAdapter() {
		return keyAdapter;
	}

	public MouseListenerThing getMouseAdapter() {
		return mouseAdapter;
	}

	public ViewportCanvas setLevelOfDetail(int levelOfDetail){
		bufferFiller.setLevelOfDetail(levelOfDetail);
		return this;
	}
}
