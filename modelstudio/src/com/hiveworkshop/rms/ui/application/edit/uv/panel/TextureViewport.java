package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.*;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.BetterAWTGLCanvas;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.*;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class TextureViewport extends BetterAWTGLCanvas {
	private RenderModel renderModel;
	private ModelView modelView;
	private TimeEnvironmentImpl renderEnv;

	private TextureThing textureThing;

	private final CameraHandler cameraHandler;
	private Timer paintTimer;
	private boolean mouseInBounds = false;
	private boolean texLoaded = false;

	private Class<? extends Throwable> lastThrownErrorClass;
	private boolean wantReload = false;
	private boolean wantReloadAll = false;
	private int popupCount = 0;
	private final ProgramPreferences programPreferences;

	private long lastExceptionTimeMillis = 0;
	private int levelOfDetail = -1;

	private float xRatio;
	private float yRatio;
	private final MouseListenerThing mouseAdapter;
	//	private final KeylistenerThing keyAdapter;
	private final GridPainter gridPainter;

	private final UVRenderer geosetRenderer;

	ExtLog currentExt = new ExtLog(new Vec3(0, 0, 0), new Vec3(0, 0, 0), 0);
	ExtLog modelExtent = new ExtLog(new Vec3(0, 0, 0), new Vec3(0, 0, 0), 0);

	public TextureViewport() throws LWJGLException {
		super();
		this.programPreferences = ProgramGlobals.getPrefs();
		cameraHandler = new CameraHandler(this);
//		cameraHandler = new CameraHandler(null);
		gridPainter = new GridPainter(cameraHandler);

		geosetRenderer = new UVRenderer(cameraHandler, programPreferences);

		mouseAdapter = new MouseListenerThing(cameraHandler, programPreferences);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);
//		keyAdapter = new KeylistenerThing(cameraHandler, programPreferences, this);
//		addKeyListener(keyAdapter);

		setBackground(ProgramGlobals.getEditorColorPrefs().getColor(ColorThing.BACKGROUND_COLOR));
		setMinimumSize(new Dimension(200, 200));


		paintTimer = new Timer(16, e -> {
			repaint();
			if (!isShowing()) {
				paintTimer.stop();
			}
		});
		paintTimer.start();
//		Timer clickTimer = new Timer(16, e -> cameraHandler.clickTimerAction());
	}

	public TextureViewport setModel(ModelView modelView, RenderModel renderModel, boolean loadDefaultCamera) {
		this.renderModel = renderModel;
		if (renderModel != null) {
			this.modelView = modelView;
			EditableModel model = modelView.getModel();
			textureThing = new TextureThing(model, programPreferences);
			geosetRenderer.updateModel(renderModel, modelView, textureThing);
			cameraHandler.setViewportCamera(2, 0, 0, -90, 90);
			cameraHandler.translate(.5, -.5);
//			cameraHandler.loadDefaultCameraFor();
		} else {
			renderEnv = null;
		}
//		geosetRenderer.updateModel(renderModel, modelView, textureThing);
		return this;
	}

	public TextureThing getTextureThing() {
		return textureThing;
	}

	public void setLevelOfDetail(int levelOfDetail) {
		this.levelOfDetail = levelOfDetail;
	}

	public CameraHandler getCameraHandler() {
		return cameraHandler;
	}

	public MouseListenerThing getMouseListenerThing() {
		return mouseAdapter;
	}

	public TextureViewport setAllowRotation(boolean allow) {
		cameraHandler.setAllowRotation(allow);
		return this;
	}

	public TextureViewport setAllowToggleOrtho(boolean allow) {
		cameraHandler.setAllowToggleOrtho(allow);
		return this;
	}

	public TextureViewport toggleOrtho() {
		cameraHandler.toggleOrtho();
		return this;
	}

	public void translate(final double a, final double b) {
		cameraHandler.translate(a, b);
	}

	protected void forceReloadTextures() {
		texLoaded = true;
		if (textureThing != null) {
			textureThing.reMakeTextureMap();
		}
	}


	@Override
	protected void exceptionOccurred(final LWJGLException exception) {
		super.exceptionOccurred(exception);
		exception.printStackTrace();
	}

	@Override
	public void initGL() {
		try {
			if ((programPreferences == null) || programPreferences.textureModels()) {
				forceReloadTextures();
			}
		} catch (final Throwable e) {
			JOptionPane.showMessageDialog(null, "initGL failed because of this exact reason:\n"
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new RuntimeException(e);
		}
		// JAVA 9+ or maybe WIN 10 allow ridiculous virtual pixes, this combination of
		// old library code and java std library code give me a metric for the ridiculous ratio:
		xRatio = (float) (Display.getDisplayMode().getWidth() / Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		yRatio = (float) (Display.getDisplayMode().getHeight() / Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		// These ratios will be wrong and users will see corrupted visuals
		// (bad scale, only fits part of window, etc)
		// if they are using Windows 10 differing UI scale per monitor.
		// I don't think I have an API to query that information yet, though.
	}

	@Override
	public void paintGL() {
		paintGL(true);
	}

	public void paintGL(final boolean autoRepainting) {
		setSize(getParent().getSize());
		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
			System.out.println("not rendering :O");
			if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 100) {
				System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
			}
			return;
		}
		reloadIfNeeded();
		try {
			glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
			enableGlThings(GL_DEPTH_TEST, GL_COLOR_MATERIAL, GL_LIGHTING, GL_LIGHT0, GL_LIGHT1, GL_NORMALIZE);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDepthMask(true);
			if ((programPreferences != null) && (programPreferences.getPerspectiveBackgroundColor() != null)) {
				float[] colorComponents = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.BACKGROUND_COLOR);
				glClearColor(colorComponents[0], colorComponents[1], colorComponents[2], autoRepainting ? 1.0f : 1.0f);
			} else {
				glClearColor(.3f, .3f, .3f, autoRepainting ? 1.0f : 1.0f);
			}
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

//			glMatrixMode(GL_MODELVIEW);
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();

			cameraHandler.setUpCamera();

			FloatBuffer ambientColor = BufferUtils.createFloatBuffer(4);
			ambientColor.put(0.6f).put(0.6f).put(0.6f).put(1f).flip();
			glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientColor);
//			addLamp(0.8f, 40.0f, 200.0f, 80.0f, GL_LIGHT0);
//			addLamp(0.2f, -100.0f, 200.5f, 0.5f, GL_LIGHT1);
			addLamp(0.8f, 80.0f, 40.0f, 200.0f, GL_LIGHT0);
			addLamp(0.2f, 0.5f, -100.0f, 200.5f, GL_LIGHT1);

			if (programPreferences != null && programPreferences.showPerspectiveGrid()) {
				gridPainter.paintGrid();
			}

//				geosetRenderer.doRender(programPreferences.textureModels(), programPreferences.viewMode() == 0, programPreferences.showNormals(), programPreferences.show3dVerts());
			geosetRenderer.doRender();

//				drawUglyTestLine();

			cameraMarkerPainter();
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
			if (autoRepainting) {
				paintAndUpdate();
			}
		} catch (final Throwable e) {
			paintTimer.stop();
			lastExceptionTimeMillis = System.currentTimeMillis();
			if ((lastThrownErrorClass == null) || (lastThrownErrorClass != e.getClass())) {
				lastThrownErrorClass = e.getClass();
				popupCount++;
				if (popupCount < 10) {
					ExceptionPopup.display(e);
				}
			}
			System.out.println("Failed to render");
			throw new RuntimeException(e);
		}
	}

	private void drawUglyTestLine() {
		glPolygonMode(GL_FRONT_FACE, GL_FILL);
		glColor4f(1, 0, 0, 1);
		glBegin(GL_LINES);
		Vec3 RT = new Vec3(100, 0, 0).transform(cameraHandler.getViewPortAntiRotMat2());

		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(RT.x, RT.y, RT.z);
//				GL11.glVertex3f(LT.x, LT.y, LT.z);
		glEnd();
	}


	private void reloadIfNeeded() {
		if (wantReloadAll) {
			wantReloadAll = false;
			wantReload = false;// If we just reloaded all, no need to reload some.
			try {
				initGL();// Re-overwrite textures
			} catch (final Exception e) {
				e.printStackTrace();
				ExceptionPopup.display("Error loading textures:", e);
			}
		} else if (wantReload) {
			wantReload = false;
			try {
				forceReloadTextures();
			} catch (final Exception e) {
				e.printStackTrace();
				ExceptionPopup.display("Error loading new texture:", e);
			}
		} else if (!texLoaded && ((programPreferences == null) || programPreferences.textureModels())) {
			forceReloadTextures();
		}
	}

	private void paintAndUpdate() {
		try {
//			System.out.println("swapping buffers");
			swapBuffers();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		if (isShowing() && !paintTimer.isRunning()) {
			paintTimer.restart();
		} else if (!isShowing() && paintTimer.isRunning()) {
			paintTimer.stop();
		}
	}

	private void addLamp(float lightValue, float x, float y, float z, int glLight) {
		FloatBuffer lightColor = BufferUtils.createFloatBuffer(4);
		lightColor.put(lightValue).put(lightValue).put(lightValue).put(1f).flip();
		FloatBuffer lightPos = BufferUtils.createFloatBuffer(4);
		lightPos.put(x).put(y).put(z).put(1f).flip();
		glLight(glLight, GL_DIFFUSE, lightColor);
		glLight(glLight, GL_POSITION, lightPos);
	}


	public boolean renderTextures() {
		return texLoaded && ((programPreferences == null) || programPreferences.textureModels());
	}

	public void reloadTextures() {
		wantReload = true;
	}

	public void reloadAllTextures() {
		wantReloadAll = true;
	}

	public void zoom(double amount) {
		cameraHandler.zoom(amount);
	}

	public double getZoomAmount() {
		return cameraHandler.getZoom();
	}

	private void enableGlThings(int... thing) {
		for (int t : thing) {
			glEnable(t);
		}
	}

	private void disableGlThings(int... thing) {
		for (int t : thing) {
			glDisable(t);
		}
	}

	Vec2 ccc = null;
	Vec3 ccc2 = null;

	private void cameraMarkerPainter() {
		if (mouseAdapter.isSelecting()) {
			CubePainter.paintRekt(mouseAdapter.getStartPGeo(), mouseAdapter.getEndPGeo1(), mouseAdapter.getEndPGeo2(), mouseAdapter.getEndPGeo3(), cameraHandler);
		}
	}
}
