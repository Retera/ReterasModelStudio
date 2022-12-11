package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderParticleEmitter2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.*;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.BetterAWTGLCanvas;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

public class PerspectiveViewport extends BetterAWTGLCanvas {
	private RenderModel renderModel;
	private ModelView modelView;
	private TimeEnvironmentImpl renderEnv;

	private TextureThing textureThing;

	private final CameraManager cameraHandler;
	private Timer paintTimer;
	private boolean mouseInBounds = false;
	private boolean texLoaded = false;

	private boolean renderTextures = true;
	private boolean wireFrame = false;
	private boolean showNormals = false;
	private boolean show3dVerts = false;

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
	private final KeylistenerThing keyAdapter;
	private final BoneRenderThing2 boneRenderThing;
	private final BoneRenderThingBuf boneRenderThingBuf;
	private final CameraRenderThing cameraRenderThing;
	private final GridPainter gridPainter;
	private final ParticleRenderer particleRenderer;

	VertexBuffers idObjectBuffers = new VertexBuffers();

	private final GeosetRenderer geosetRenderer;
//	private final GeosetRendererBuf geosetRendererBuf;

	ExtLog currentExt = new ExtLog(Vec3.ZERO, Vec3.ZERO, 0);
	ExtLog modelExtent = new ExtLog(Vec3.ZERO, Vec3.ZERO, 0);

	public PerspectiveViewport() throws LWJGLException {
		super();
		this.programPreferences = ProgramGlobals.getPrefs();
		cameraHandler = new CameraManager(this);
		boneRenderThing = new BoneRenderThing2(cameraHandler);
		boneRenderThingBuf = new BoneRenderThingBuf(cameraHandler);
		cameraRenderThing = new CameraRenderThing();
		gridPainter = new GridPainter(cameraHandler);
		particleRenderer = new ParticleRenderer();

		geosetRenderer = new GeosetRenderer(cameraHandler, programPreferences);
//		geosetRendererBuf = new GeosetRendererBuf(cameraHandler, programPreferences);

		mouseAdapter = new MouseListenerThing(cameraHandler, programPreferences);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);
		keyAdapter = new KeylistenerThing(cameraHandler, programPreferences);
//		keyAdapter = new KeylistenerThing(cameraHandler, programPreferences, this);
		addKeyListener(keyAdapter);

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

	public PerspectiveViewport setModel(ModelView modelView, RenderModel renderModel, boolean loadDefaultCamera) {
		this.renderModel = renderModel;
		if(renderModel != null){
			this.modelView = modelView;
			EditableModel model = modelView.getModel();
			textureThing = new TextureThing(programPreferences);
			renderEnv = renderModel.getTimeEnvironment();

			modelExtent.set(model.getExtents());
			if (loadDefaultCamera) {
				renderEnv.setSequence(ViewportHelpers.findDefaultAnimation(model));
				cameraHandler.loadDefaultCameraFor(getCurrentModelRadius());
			}

			this.renderModel.setCameraHandler(cameraHandler);
			this.renderModel.refreshFromEditor();
//			forceReloadTextures();
			texLoaded = false;
		} else {
			renderEnv = null;
			this.modelView = null;
		}
		geosetRenderer.updateModel(renderModel, modelView, textureThing);
		particleRenderer.setModel(renderModel, textureThing);
//		geosetRendererBuf.updateModel(renderModel, modelView, textureThing);
		return this;
	}


	public PerspectiveViewport setRenderTextures(boolean renderTextures) {
		this.renderTextures = renderTextures;
		return this;
	}

	public PerspectiveViewport setWireFrame(boolean wireFrame) {
		this.wireFrame = wireFrame;
		return this;
	}

	public PerspectiveViewport setShowNormals(boolean showNormals) {
		this.showNormals = showNormals;
		return this;
	}

	public PerspectiveViewport setShow3dVerts(boolean show3dVerts) {
		this.show3dVerts = show3dVerts;
		return this;
	}
	public TextureThing getTextureThing() {
		return textureThing;
	}

	public void setLevelOfDetail(int levelOfDetail) {
		this.levelOfDetail = levelOfDetail;
	}

	public CameraManager getCameraHandler() {
		return cameraHandler;
	}

	public MouseListenerThing getMouseListenerThing() {
		return mouseAdapter;
	}

	public PerspectiveViewport setAllowRotation(boolean allow) {
		cameraHandler.setAllowRotation(allow);
		return this;
	}

	public PerspectiveViewport setAllowToggleOrtho(boolean allow) {
		cameraHandler.setAllowToggleOrtho(allow);
		return this;
	}

	public PerspectiveViewport toggleOrtho() {
		cameraHandler.toggleOrtho();
		return this;
	}

	public double getCurrentModelRadius() {
		setCurrentExtent();
		return ViewportHelpers.getBoundsRadius(renderEnv, modelExtent);
	}

	private void setCurrentExtent() {
		if (renderEnv != null) {
			Animation animation = renderEnv.getCurrentAnimation();
			if ((animation != null) && animation.getExtents() != null) {
				currentExt = animation.getExtents();
			}
		}
		if (currentExt.getMaximumExtent().distance(new Vec3(0, 0, 0)) < 1) {
			currentExt = modelExtent;
		}
	}

	public void translate(final double a, final double b) {
		cameraHandler.translate(a, b);
	}

	protected void forceReloadTextures() {
		texLoaded = true;
		if (textureThing != null && renderModel != null) {
			textureThing.reMakeTextureMap(renderModel.getModel());
			renderModel.refreshFromEditor();
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
			System.out.println("initing GL");
//			geosetRendererBuf.initShaderThing();
			if ((programPreferences == null) || programPreferences.textureModels()) {
				forceReloadTextures();
			}
			System.out.println("GL inited!!!!");
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

	private boolean hasReloadedRenderModel = false;
	FloatBuffer pipelineMatrixBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
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
//			System.out.println("painting");
			if (renderModel != null) {
				hasReloadedRenderModel = false;
				updateRenderModel();
				gluPerspective(45f, (float) getWidth() / (float) getHeight(), 5.0f, 16000.0f);
				glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
				enableGlThings(GL_DEPTH_TEST, GL_COLOR_MATERIAL, GL_LIGHTING, GL_LIGHT0, GL_LIGHT1, GL_NORMALIZE);

				GL11.glDepthFunc(GL11.GL_LEQUAL);
				GL11.glDepthMask(true);
				if ((programPreferences != null) && (programPreferences.getPerspectiveBackgroundColor() != null)) {
					float[] colorComponents = ProgramGlobals.getEditorColorPrefs().getColorComponents(ColorThing.BACKGROUND_COLOR);
					glClearColor(colorComponents[0], colorComponents[1], colorComponents[2], autoRepainting ? 1.0f : 0.0f);
				} else {
					glClearColor(.3f, .3f, .3f, autoRepainting ? 1.0f : 0.0f);
				}
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

				glMatrixMode(GL_PROJECTION);
				glLoadIdentity();
				glMatrixMode(GL_MODELVIEW);
				glLoadIdentity();
				fillMatrixBuffer(pipelineMatrixBuffer, cameraHandler.getViewProjectionMatrix());
				glLoadMatrix(pipelineMatrixBuffer);

				cameraHandler.updateCamera();

				setUpLights();

				if (programPreferences != null && programPreferences.showPerspectiveGrid()) {
//					System.out.println("painting grid!");
					gridPainter.paintGrid();
				}

//				geosetRenderer.doRender(programPreferences.textureModels(), programPreferences.viewMode() == 0, programPreferences.showNormals(), programPreferences.show3dVerts());
				geosetRenderer.doRender(renderTextures,  wireFrame,  showNormals,  show3dVerts);
//				System.out.println("painting geosets!");
//				geosetRendererBuf.doRender(renderTextures,  wireFrame,  showNormals,  show3dVerts);

				if (show3dVerts) {
//					System.out.println("painting nodes!");
					renderNodeStuff();
				}
				if (programPreferences != null && programPreferences.getRenderParticles()) {
//					System.out.println("painting particles!");
					renderParticles();
				}

//				drawUglyTestLine();

//				System.out.println("painting camera!");
				selectBoxPainter();
				GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
				if (autoRepainting) {
//					System.out.println("paintAndUpdate");
					paintAndUpdate();
				}
			}
//			System.out.println("painted!");
		} catch (final Throwable e) {
			paintTimer.stop();
			if (renderModel != null && !hasReloadedRenderModel) {
				hasReloadedRenderModel = true;
				renderModel.refreshFromEditor();
			}
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

	protected void fillMatrixBuffer(FloatBuffer buffer, Mat4 matrix) {
		buffer.clear();
		buffer.put(matrix.m00);
		buffer.put(matrix.m01);
		buffer.put(matrix.m02);
		buffer.put(matrix.m03);
		buffer.put(matrix.m10);
		buffer.put(matrix.m11);
		buffer.put(matrix.m12);
		buffer.put(matrix.m13);
		buffer.put(matrix.m20);
		buffer.put(matrix.m21);
		buffer.put(matrix.m22);
		buffer.put(matrix.m23);
		buffer.put(matrix.m30);
		buffer.put(matrix.m31);
		buffer.put(matrix.m32);
		buffer.put(matrix.m33);
		buffer.flip();
	}

	private void setUpLights() {
		FloatBuffer ambientColor = BufferUtils.createFloatBuffer(4);
		ambientColor.put(0.6f).put(0.6f).put(0.6f).put(1f).flip();
		glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientColor);
//			addLamp(0.8f, 40.0f, 200.0f, 80.0f, GL_LIGHT0);
//			addLamp(0.2f, -100.0f, 200.5f, 0.5f, GL_LIGHT1);
		addLamp(0.8f, 80.0f, 40.0f, 200.0f, GL_LIGHT0);
		addLamp(0.2f, 0.5f, -100.0f, 200.5f, GL_LIGHT1);
	}

	private void drawUglyTestLine() {
		glPolygonMode(GL_FRONT_FACE, GL_FILL);
		glColor4f(1, 0, 0, 1);
		glBegin(GL_LINES);
		Vec3 RT = new Vec3(100, 0, 0).transform(cameraHandler.getViewProjectionMatrix());

		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(RT.x, RT.y, RT.z);
//				GL11.glVertex3f(LT.x, LT.y, LT.z);
		glEnd();
	}

	private void updateRenderModel() {
		if (renderEnv.isLive()) {
//			renderEnv.updateAnimationTime();
//			renderModel.updateNodes(false, programPreferences.getRenderParticles());
			renderModel.updateAnimationTime().updateNodes2(programPreferences.getRenderParticles());
		} else if (ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
//			renderModel.updateNodes(false, programPreferences.getRenderParticles());
//			renderModel.updateNodes2(programPreferences.getRenderParticles());
			renderModel.updateNodes(programPreferences.getRenderParticles());
		}
		if (modelView.isGeosetsVisible()) {
			renderModel.updateGeosets();
		}
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

	private void renderParticles() {
		if (renderTextures()) {
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			glEnable(GL11.GL_TEXTURE_2D);
		}
		GL11.glDepthMask(false);
		enableGlThings(GL_BLEND, GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);

//		renderModel.getParticleShader().use();
		for (final RenderParticleEmitter2 particle : renderModel.getRenderParticleEmitters2()) {
//			System.out.println("renderParticles");
			particleRenderer.render(particle);
		}
	}



	private void renderNodeStuff() {
		GL11.glDepthMask(true);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
//		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glDepthMask(true);
//		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glEnable(GL_SHADE_MODEL);
		GL11.glEnable(GL11.GL_CULL_FACE);
//		disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D, GL_CULL_FACE);
		disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D, GL_SHADE_MODEL);
//		disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);

//		glColor3f(255f, 1f, 255f);
		glColor4f(.7f, .0f, .0f, .4f);

		for (final IdObject idObject : modelView.getVisibleIdObjects()) {
			boneRenderThing.paintBones(modelView, renderModel, idObject);
		}
//		renderIdObjects();

		for (final Camera camera : modelView.getVisibleCameras()) {
			cameraRenderThing.paintCameras(modelView, renderModel, camera);
		}

		GL11.glEnable(GL_SHADE_MODEL);
	}

	private void renderIdObjects(){
		glEnable(GL_CULL_FACE);
		glDisable(GL_BLEND);
		glEnable(GL_DEPTH_TEST);

		int buffCap = modelView.getVisibleIdObjects().size() * boneRenderThingBuf.getQuadsPerObj() * 6;
		idObjectBuffers.ensureCapacity(buffCap);
		idObjectBuffers.resetIndex();

		int objectCount = 0;
		for (final IdObject idObject : modelView.getVisibleIdObjects()) {
			boneRenderThingBuf.paintBones(idObjectBuffers, modelView, renderModel, idObject);
			objectCount++;
		}

		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_NORMAL_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);

		glVertexPointer(3, 0, idObjectBuffers.getPositions());
		glNormalPointer(0, idObjectBuffers.getNormals());
		glColorPointer(4, 0, idObjectBuffers.getColors());

		glDrawArrays(GL_TRIANGLES, 0, objectCount * boneRenderThingBuf.getQuadsPerObj()*6);

		glDisableClientState(GL_VERTEX_ARRAY);
		glDisableClientState(GL_NORMAL_ARRAY);
		glDisableClientState(GL_COLOR_ARRAY);
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

	private void selectBoxPainter() {
//		CubePainter.paintCameraLookAt(cameraHandler);
		if (mouseAdapter.isSelecting()) {
			CubePainter.paintRekt(mouseAdapter.getStartP(), mouseAdapter.getEndP(), cameraHandler);
		}
	}
}
