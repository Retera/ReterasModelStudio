package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.editor.render3d.RenderParticleEmitter2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.KeylistenerThing;
import com.hiveworkshop.rms.ui.application.viewer.MouseListenerThing;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.application.viewer.ViewportHelpers;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.BetterAWTGLCanvas;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glEnable;

public class AnimatedPerspectiveViewport extends BetterAWTGLCanvas {
	private ModelView modelView;
	private RenderModel renderModel;
	private TimeEnvironmentImpl renderEnv;
	private Timer paintTimer;

	private boolean texLoaded = false;

	private boolean renderTextures = true;
	private boolean wireFrame = false;
	private boolean showNormals = false;
	private boolean show3dVerts = false;

	private static final ShaderManager shaderManager = new ShaderManager();

	Class<? extends Throwable> lastThrownErrorClass;
	private final ProgramPreferences programPreferences;

	private long lastExceptionTimeMillis = 0;

	private float backgroundRed, backgroundBlue, backgroundGreen;

	private int levelOfDetail;
	private final CameraManager cameraManager;

	GeosetRenderThing geosetRenderThing;
	GridPainter2 gridPainter2;

	private TextureThing textureThing;

	private float xRatio;
	private float yRatio;

	boolean wantReload = false;
	boolean wantReloadAll = false;

	int popupCount = 0;
	private final MouseListenerThing mouseAdapter;
	private final KeylistenerThing keyAdapter;

	public AnimatedPerspectiveViewport() throws LWJGLException {
		super();
		this.programPreferences = ProgramGlobals.getPrefs();

		setBackground(programPreferences == null ? new Color(80, 80, 80) : programPreferences.getPerspectiveBackgroundColor());
		setMinimumSize(new Dimension(200, 200));
//		cameraManager = new PortraitCameraManager();
		cameraManager = new CameraManager(this);

		mouseAdapter = new MouseListenerThing(cameraManager, programPreferences);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);
		keyAdapter = new KeylistenerThing(cameraManager, programPreferences);
		addKeyListener(keyAdapter);

		geosetRenderThing = new GeosetRenderThing();
		gridPainter2 = new GridPainter2(cameraManager);

//
		loadBackgroundColors();
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

//	public MouseThingi getMouseListenerThing() {
//		return mouseThingi;
//	}


	public TextureThing getTextureThing() {
		return textureThing;
	}

	public MouseListenerThing getMouseListenerThing() {
		return mouseAdapter;
	}

	public CameraManager getPortraitCameraManager() {
		return cameraManager;
	}


	private void loadBackgroundColors() {
		if (getBackground() != null) {
			backgroundRed = getBackground().getRed() / 255f;
			backgroundGreen = getBackground().getGreen() / 255f;
			backgroundBlue = getBackground().getBlue() / 255f;
		}
	}

	public void setModel(ModelView modelView, RenderModel renderModel, boolean loadDefaultCamera) {
		this.renderModel = renderModel;
		this.modelView = modelView;
		if(renderModel != null){
			renderModel.getTimeEnvironment().setSequence(null);
			EditableModel model = modelView.getModel();
			textureThing = new TextureThing(programPreferences);
			renderEnv = renderModel.getTimeEnvironment();

			if (loadDefaultCamera) {
//				cameraManager.setModelInstance(null, null);
				renderEnv.setSequence(ViewportHelpers.findDefaultAnimation(model));
				cameraManager.loadDefaultCameraFor(ViewportHelpers.getBoundsRadius(renderEnv, model.getExtents()));
			}

			renderModel.refreshFromEditor(textureThing);
			reloadAllTextures();
		} else {
			textureThing = null;
			renderEnv = null;
			modelView = null;
		}
//		cameraManager.viewport(0, 0, getWidth() * xRatio, getHeight() * yRatio);
		cameraManager.setOrtho(true);
		geosetRenderThing.setModel(renderModel, modelView, textureThing);
	}

	public AnimatedPerspectiveViewport setRenderTextures(boolean renderTextures) {
		this.renderTextures = renderTextures;
		return this;
	}

	public AnimatedPerspectiveViewport setWireFrame(boolean wireFrame) {
		this.wireFrame = wireFrame;
		return this;
	}

	public AnimatedPerspectiveViewport setShowNormals(boolean showNormals) {
		this.showNormals = showNormals;
		return this;
	}

	public AnimatedPerspectiveViewport setShow3dVerts(boolean show3dVerts) {
		this.show3dVerts = show3dVerts;
		return this;
	}

	public void reloadTextures() {
		wantReload = true;
	}

	public void reloadAllTextures() {
		wantReloadAll = true;
	}

	public void forceReloadTextures() {
		texLoaded = true;
		if (textureThing != null && renderModel != null) {
//			textureThing.reMakeTextureMap(renderModel.getModel());
			textureThing.clearTextureMap();

			renderModel.refreshFromEditor(textureThing);
		}

	}

	public void setLevelOfDetail(final int levelOfDetail) {
		this.levelOfDetail = levelOfDetail;
	}

	@Override
	public void initGL() {
		ShaderPipeline pipeline = shaderManager.getOrCreatePipeline();
		pipeline.onGlobalPipelineSet();
//		NGGLDP.setPipeline(getOrCreatePipeline());
		try {
			if ((programPreferences == null) || programPreferences.textureModels()) {
				forceReloadTextures();
			}
			// JAVA 9+ or maybe WIN 10 allow ridiculous virtual pixes, this combination of
			// old library code and java std library code give me a metric for the
			// ridiculous ratio:
			xRatio = (float) (Display.getDisplayMode().getWidth() / Toolkit.getDefaultToolkit().getScreenSize().getWidth());
			yRatio = (float) (Display.getDisplayMode().getHeight() / Toolkit.getDefaultToolkit().getScreenSize().getHeight());
			// These ratios will be wrong and users will see corrupted visuals (bad scale, only fits part of window,
			// etc) if they are using Windows 10 differing UI scale per monitor. I don't think I have an API
			// to query that information yet, though.
		}
		catch (final Throwable e) {
			JOptionPane.showMessageDialog(null, "initGL failed because of this exact reason:\n"
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void exceptionOccurred(final LWJGLException exception) {
		super.exceptionOccurred(exception);
		exception.printStackTrace();
	}

	@Override
	public void paintGL() {
		paintGL(true);
	}

	public void paintGL(final boolean autoRepainting) {
		setSize(getParent().getSize());
		cameraManager.updateCamera();
		ShaderPipeline pipeline = shaderManager.getOrCreatePipeline();
		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
			return;
		}
		reloadIfNeeded();
		try {
			updateRenderModel();

			if (programPreferences != null && wireFrame) {
				pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			} else {
				pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			}
			pipeline.glViewport(getWidth(), getHeight());
			GL11.glViewport(0, 0, getWidth(), getHeight());
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDepthMask(true);
			pipeline.glEnableIfNeeded(GL11.GL_COLOR_MATERIAL);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHT0);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHT1);
			pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			if (renderTextures) {
				pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
			}
//			GL11.glClearColor(backgroundRed, backgroundGreen, backgroundBlue, autoRepainting ? 1.0f : 0.0f);
			GL11.glClearColor(backgroundRed, backgroundGreen, backgroundBlue, 0.0f);

			pipeline.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			pipeline.glLoadIdentity();

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			pipeline.glMatrixMode(GL11.GL_MODELVIEW);
			pipeline.glLoadIdentity();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();

			pipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());

			setUpLights(pipeline);

			if (renderTextures) {
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
			} else {
				pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			}
			geosetRenderThing.render(pipeline, renderTextures);
			pipeline.doRender(GL11.GL_TRIANGLES);
			renderNodes();


			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);



			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			if (showNormals) {
				renderNormals();
			}
			if (show3dVerts) {
				render3DVerts();
			}

			paintSelectionBox();
			paintGrid();

//			if (programPreferences != null && programPreferences.getRenderParticles()) {
//				renderParticles();
//			}
			GL11.glDepthMask(false);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			if (autoRepainting) {

				paintAndUpdate();
			}
			if(bufferConsumer != null){
//				glfwHideWindow(GLFW_VISIBLE, GLFW_FALSE)
				bufferConsumer.accept(paintGL2());
				bufferConsumer = null;
			}
		}
		catch (final Throwable e) {
			e.printStackTrace();
			lastExceptionTimeMillis = System.currentTimeMillis();
			if ((lastThrownErrorClass == null) || (lastThrownErrorClass != e.getClass())) {
				lastThrownErrorClass = e.getClass();
				popupCount++;
				if (popupCount < 10) {
					ExceptionPopup.display(e);
				}
			}
			throw new RuntimeException(e);
		}
	}
	Consumer<ByteBuffer> bufferConsumer;
	public void setPixelBufferListener(Consumer<ByteBuffer> bufferConsumer){
		this.bufferConsumer = bufferConsumer;
	}
	public ByteBuffer paintGL2() {
		setSize(getParent().getSize());
		cameraManager.updateCamera();
		ShaderPipeline pipeline = shaderManager.getOrCreatePipeline();
		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
			return null;
		}
		reloadIfNeeded();
		try {
			updateRenderModel();

			if (programPreferences != null && wireFrame) {
				pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			} else {
				pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			}
			pipeline.glViewport(getWidth(), getHeight());
			GL11.glViewport(0, 0, getWidth(), getHeight());
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDepthMask(true);
			pipeline.glEnableIfNeeded(GL11.GL_COLOR_MATERIAL);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHT0);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHT1);
			pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			if (renderTextures) {
				pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
			}
//			GL11.glClearColor(backgroundRed, backgroundGreen, backgroundBlue, autoRepainting ? 1.0f : 0.0f);
			GL11.glClearColor(backgroundRed, backgroundGreen, backgroundBlue, 0.0f);

			pipeline.glMatrixMode(GL11.GL_PROJECTION);
			pipeline.glLoadIdentity();

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			pipeline.glMatrixMode(GL11.GL_MODELVIEW);
			pipeline.glLoadIdentity();

			pipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());

			setUpLights(pipeline);

			if (renderTextures) {
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
			} else {
				pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			}
			geosetRenderThing.render(pipeline, renderTextures);
			pipeline.doRender(GL11.GL_TRIANGLES);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			renderNodes();


			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);



			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			if (showNormals) {
				renderNormals();
			}
			if (show3dVerts) {
				render3DVerts();
			}

			paintSelectionBox();
			paintGrid();

//			if (programPreferences != null && programPreferences.getRenderParticles()) {
//				renderParticles();
//			}
			ByteBuffer pixels = ByteBuffer.allocateDirect(getWidth() * getHeight() * 4);

			GL11.glReadPixels(0, 0, getWidth(), getHeight(), GL11.GL_RGBA, GL_UNSIGNED_BYTE, pixels);

			GL11.glDepthMask(false);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			return pixels;
		} catch (final Throwable e) {
			e.printStackTrace();
			lastExceptionTimeMillis = System.currentTimeMillis();
			if ((lastThrownErrorClass == null) || (lastThrownErrorClass != e.getClass())) {
				lastThrownErrorClass = e.getClass();
				popupCount++;
				if (popupCount < 10) {
					ExceptionPopup.display(e);
				}
			}
			throw new RuntimeException(e);
		}
	}

	private void renderNormals() {
		ShaderPipeline normPipeline = shaderManager.getOrCreateNormPipeline();
		normPipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		normPipeline.glViewport(getWidth(), getHeight());
		normPipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
		normPipeline.glMatrixMode(GL11.GL_PROJECTION);
		normPipeline.glLoadIdentity();
		normPipeline.glMatrixMode(GL11.GL_MODELVIEW);
		normPipeline.glLoadIdentity();
		normPipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());
		geosetRenderThing.fillNormalsBuffer(normPipeline);
		normPipeline.doRender(GL11.GL_POINTS);
	}

	private void render3DVerts() {
		ShaderPipeline vertPipeline = shaderManager.getOrCreateVertPipeline();
		vertPipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		vertPipeline.glViewport(getWidth(), getHeight());
		vertPipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
		vertPipeline.glMatrixMode(GL11.GL_PROJECTION);
		vertPipeline.glLoadIdentity();
		vertPipeline.glMatrixMode(GL11.GL_MODELVIEW);
		vertPipeline.glLoadIdentity();
		vertPipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());
		geosetRenderThing.fillVertsBuffer(vertPipeline);
		vertPipeline.doRender(GL11.GL_POINTS);
	}

	private void renderNodes() {
		ShaderPipeline bonePipeline = shaderManager.getOrCreateBoneMarkerShaderPipeline();
		bonePipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		bonePipeline.glViewport(getWidth(), getHeight());
		bonePipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
		bonePipeline.glMatrixMode(GL11.GL_PROJECTION);
		bonePipeline.glLoadIdentity();
		bonePipeline.glMatrixMode(GL11.GL_MODELVIEW);
		bonePipeline.glLoadIdentity();
		bonePipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
		bonePipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		bonePipeline.prepare();
//			pipeline.glColor3f(1f, 1f, 3f);

		Vec4 colorHeap = new Vec4(0f, .0f, 1f, 1f);
		// if( wireframe.isSelected() )
		for (IdObject v : modelView.getVisibleIdObjects()) {
			if(modelView.shouldRender(v)){
				RenderNode2 renderNode = renderModel.getRenderNode(v);

				bonePipeline.addVert(renderNode.getPivot(), Vec3.Z_AXIS, colorHeap, Vec2.ORIGIN, colorHeap, Vec3.ZERO);
			}
		}
		bonePipeline.doRender(GL11.GL_POINTS);
	}

	private void paintSelectionBox() {
//		CubePainter.paintCameraLookAt(cameraHandler);
		if (mouseAdapter.isSelecting()) {
			ShaderPipeline selectionPipeline = shaderManager.getOrCreateSelectionPipeline();
			selectionPipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			selectionPipeline.glViewport(getWidth(), getHeight());
			selectionPipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
			selectionPipeline.glMatrixMode(GL11.GL_PROJECTION);
			selectionPipeline.glLoadIdentity();
			selectionPipeline.glMatrixMode(GL11.GL_MODELVIEW);
			selectionPipeline.glLoadIdentity();
			selectionPipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());

			if(renderModel != null){
				// https://learnopengl.com/Advanced-OpenGL/Geometry-Shader
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
				selectionPipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_TEXTURE_2D);

				selectionPipeline.prepare();

				selectionPipeline.addVert(mouseAdapter.getStart(), Vec3.Z_AXIS, new Vec4(), new Vec2(), new Vec4(1, 0,0,1), Vec3.ZERO);
				selectionPipeline.addVert(mouseAdapter.getEnd(), Vec3.Z_AXIS, new Vec4(), new Vec2(), new Vec4(1, 0,0,1), Vec3.ZERO);

				selectionPipeline.doRender(GL11.GL_LINES);
			}
//			CubePainter.paintRekt(mouseAdapter.getStartPGeo(), mouseAdapter.getEndPGeo1(), mouseAdapter.getEndPGeo2(), mouseAdapter.getEndPGeo3(), cameraHandler);
//			System.out.println("is selecting!");
		}
	}
	private void paintGrid() {
//		CubePainter.paintCameraLookAt(cameraHandler);
		if (programPreferences.showPerspectiveGrid()) {
			ShaderPipeline pipeline = shaderManager.getOrCreateGridPipeline();
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			pipeline.glViewport(getWidth(), getHeight());
			pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
			pipeline.glMatrixMode(GL11.GL_PROJECTION);
			pipeline.glLoadIdentity();
			pipeline.glMatrixMode(GL11.GL_MODELVIEW);
			pipeline.glLoadIdentity();
			pipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());

			if(renderModel != null){
				// https://learnopengl.com/Advanced-OpenGL/Geometry-Shader
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
				pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_TEXTURE_2D);

				gridPainter2.fillGridBuffer(pipeline);
				pipeline.doRender(GL11.GL_LINES);
			}
		}
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

	private void paintAndUpdate() {
		try {
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

	private void reloadIfNeeded() {
		if (wantReloadAll) {
			wantReloadAll = false;
			wantReload = false;// If we just reloaded all, no need to reload some.
			shaderManager.discardPipelines();
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
			}
			catch (final Exception e) {
				e.printStackTrace();
				ExceptionPopup.display("Error loading new texture:", e);
			}
		} else if (!texLoaded && ((programPreferences == null) || programPreferences.textureModels())) {
			forceReloadTextures();
			texLoaded = true;
		}
	}


	private void setUpLights(ShaderPipeline pipeline) {
		FloatBuffer ambientColor = BufferUtils.createFloatBuffer(4);
		ambientColor.put(0.6f).put(0.6f).put(0.6f).put(1f).flip();
		pipeline.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, ambientColor);

		FloatBuffer lightColor0 = BufferUtils.createFloatBuffer(4);
		lightColor0.put(0.8f).put(0.8f).put(0.8f).put(1f).flip();
		FloatBuffer lightPos0 = BufferUtils.createFloatBuffer(4);
		lightPos0.put(40.0f).put(100.0f).put(80.0f).put(1f).flip();
		pipeline.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, lightColor0);
		pipeline.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPos0);

		FloatBuffer lightColor1 = BufferUtils.createFloatBuffer(4);
		lightColor1.put(0.2f).put(0.2f).put(0.2f).put(1f).flip();
		FloatBuffer lightPos1 = BufferUtils.createFloatBuffer(4);
		lightPos1.put(-100.0f).put(100.5f).put(0.5f).put(1f).flip();

		pipeline.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, lightColor1);
		pipeline.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, lightPos1);
	}

	private void renderParticles() {
//		if (renderTextures()) {
		if (renderTextures) {
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			glEnable(GL11.GL_TEXTURE_2D);
		}
		GL11.glDepthMask(false);
		glEnable(GL11.GL_BLEND);
		glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);

//		renderModel.getParticleShader().use();
		for (final RenderParticleEmitter2 particle : renderModel.getRenderParticleEmitters2()) {
//			System.out.println("renderParticles");
			particle.render();
		}


//		renderModel.getParticleShader().use();
//		for (final RenderParticleEmitter2 particle : renderModel.getParticleEmitters2()) {
//			particle.render(renderModel, renderModel.getParticleShader());
//		}
//		for (final RenderRibbonEmitter emitter : renderModel.getRibbonEmitters()) {
//			emitter.render(renderModel, renderModel.getParticleShader());
//		}
	}



//	private final class Particle2TextureInstance implements InternalResource, InternalInstance {
//		private final Bitmap bitmap;
//		private final ParticleEmitter2 particle;
//		private boolean loaded = false;
//
//		public Particle2TextureInstance(final Bitmap bitmap, final ParticleEmitter2 particle) {
//			this.bitmap = bitmap;
//			this.particle = particle;
//		}
//
//		@Override
//		public void setTransformation(final Vec3 worldLocation, final Quat rotation,
//		                              final Vec3 worldScale) {
//		}
//
//		@Override
//		public void setSequence(final int index) {
//
//		}
//
//		@Override
//		public void show() {
//		}
//
//		@Override
//		public void setPaused(final boolean paused) {
//
//		}
//
//		@Override
//		public void move(final Vec3 deltaPosition) {
//
//		}
//
//		@Override
//		public void hide() {
//		}
//
//		@Override
//		public void bind() {
//			if (!loaded) {
//				loadToTexMap((particle.getFilterModeReallyBadReallySlow() == MdlxParticleEmitter2.FilterMode.MODULATE)
//								&& (particle.getFilterModeReallyBadReallySlow() == MdlxParticleEmitter2.FilterMode.MODULATE2X),
//						bitmap);
//				loaded = true;
//			}
//			final Integer texture = textureMap.get(bitmap);
//			bindLayer(particle, bitmap, texture);
//		}
//
//		@Override
//		public InternalInstance addInstance() {
//			return this;
//		}
//
//	}
//
//	private final class RibbonEmitterMaterialInstance implements InternalResource, InternalInstance {
//		private final Material material;
//		private final RibbonEmitter ribbonEmitter;
//		private boolean loaded = false;
//
//		public RibbonEmitterMaterialInstance(final Material material, final RibbonEmitter ribbonEmitter) {
//			this.material = material;
//			this.ribbonEmitter = ribbonEmitter;
//		}
//
//		@Override
//		public void setTransformation(final Vec3 worldLocation, final Quat rotation,
//		                              final Vec3 worldScale) {
//		}
//
//		@Override
//		public void setSequence(final int index) {
//
//		}
//
//		@Override
//		public void show() {
//		}
//
//		@Override
//		public void setPaused(final boolean paused) {
//
//		}
//
//		@Override
//		public void move(final Vec3 deltaPosition) {
//
//		}
//
//		@Override
//		public void hide() {
//		}
//
//		@Override
//		public void bind() {
//			if (!loaded) {
//				for (int i = 0; i < material.getLayers().size(); i++) {
//					final Layer layer = material.getLayers().get(i);
//					if (layer.getTextureBitmap() != null) {
//						loadToTexMap(layer, layer.getTextureBitmap());
//					}
//					if (layer.getTextures() != null) {
//						for (final Bitmap tex : layer.getTextures()) {
//							loadToTexMap(layer, tex);
//						}
//					}
//				}
//				loaded = true;
//			}
//
//			// TODO support multi layer ribbon
//			final Layer layer = material.getLayers().get(0);
//			final Bitmap tex = layer.getRenderTexture(AnimatedPerspectiveViewport.this, model);
//			final Integer texture = textureMap.get(tex);
//			bindLayer(layer, tex, texture, 800, material);
//		}
//
//		@Override
//		public InternalInstance addInstance() {
//			return this;
//		}
//
//	}
//
//	@Override
//	public InternalResource allocateTexture(final Bitmap bitmap, final ParticleEmitter2 textureSource) {
//		return new Particle2TextureInstance(bitmap, textureSource);
//	}
//
//	@Override
//	public InternalResource allocateMaterial(final Material material, final RibbonEmitter ribbonEmitter) {
//		return new RibbonEmitterMaterialInstance(material, ribbonEmitter);
//	}

	public void setSpawnParticles(final boolean b) {
		renderModel.setSpawnParticles(b);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		// This clears the pipeline in case of shaders...
		// It clears out VBO/VAO so that it will auto create new ones.
		// Without this, for example, some stuff tries to use the
		// old VBO/VAO that are no longer valid and we get messed up drawing.
		// super in this case is calling destroy on OpenGL context entirely,
		// that's why. But this method was happening during the app lifecycle of
		// clearing and resetting the UI views when we open a new model.
//		pipeline = null;
//		vertPipeline = null;
//		shaderManager.discardPipelines();
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	public void setCamera(final Camera camera) {
		if(cameraManager instanceof PortraitCameraManager){
			((PortraitCameraManager)cameraManager).setModelInstance(renderModel, camera);
		}
	}

}