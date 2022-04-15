package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.editor.render3d.RenderParticleEmitter2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.blp.GPUReadyTexture;
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
import org.lwjgl.opengl.GL12;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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

	private boolean wireframe;
	private static final ShaderManager shaderManager = new ShaderManager();

	Class<? extends Throwable> lastThrownErrorClass;
	private final ProgramPreferences programPreferences;

	private long lastExceptionTimeMillis = 0;

	private float backgroundRed, backgroundBlue, backgroundGreen;

	private int levelOfDetail;
	private final CameraManager cameraManager;

	MouseThingi mouseThingi;
	GeosetRenderThing geosetRenderThing;
	GridPainter2 gridPainter2;

	private TextureThing textureThing;

	private float xRatio;
	private float yRatio;

	boolean wantReload = false;
	boolean wantReloadAll = false;

	int popupCount = 0;
	private MouseListenerThing mouseAdapter;
	private KeylistenerThing keyAdapter;

	public AnimatedPerspectiveViewport() throws LWJGLException {
		super();
		this.programPreferences = ProgramGlobals.getPrefs();
		// Dimension 1 and Dimension 2, these specify which dimensions to
		// display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z
		// dimensions
		//
		// Viewport border
		// setBorder(BorderFactory.createBevelBorder(1));
		setBackground(programPreferences == null ? new Color(80, 80, 80) : programPreferences.getPerspectiveBackgroundColor());
		setMinimumSize(new Dimension(200, 200));
		// add(Box.createHorizontalStrut(200));
		// add(Box.createVerticalStrut(200));
		// setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
//		cameraManager = new PortraitCameraManager();
		cameraManager = new CameraManager(this);

//		mouseThingi = new MouseThingi(cameraManager);
//		addMouseListener(mouseThingi);
//		addMouseMotionListener(mouseThingi);
//		addMouseWheelListener(mouseThingi);

		mouseAdapter = new MouseListenerThing(cameraManager, programPreferences);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);
		keyAdapter = new KeylistenerThing(cameraManager, programPreferences, null);
//		keyAdapter = new KeylistenerThing(cameraHandler, programPreferences, this);
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
			textureThing = new TextureThing(model, programPreferences);
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
			textureThing.reMakeTextureMap();

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


	public boolean renderTextures() {
		return texLoaded && ((programPreferences == null) || programPreferences.textureModels());
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
		ShaderPipeline bonePipeline = shaderManager.getOrCreateBoneMarkerShaderPipeline();
		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
			return;
		}
		reloadIfNeeded();
		try {
			updateRenderModel();

			if ((programPreferences != null) && (programPreferences.viewMode() == 0)) {
				pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			} else if ((programPreferences == null) || (programPreferences.viewMode() == 1)) {
				pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			}
//			pipeline.glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
			pipeline.glViewport(0, 0, (int) (getWidth()), (int) (getHeight()));
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDepthMask(true);
			pipeline.glEnableIfNeeded(GL11.GL_COLOR_MATERIAL);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHT0);
			pipeline.glEnableIfNeeded(GL11.GL_LIGHT1);
			pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			if (renderTextures()) {
				pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
			}
			GL11.glClearColor(backgroundRed, backgroundGreen, backgroundBlue, autoRepainting ? 1.0f : 0.0f);

			pipeline.glMatrixMode(GL11.GL_PROJECTION);
			pipeline.glLoadIdentity();

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			pipeline.glMatrixMode(GL11.GL_MODELVIEW);
			pipeline.glLoadIdentity();

			pipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());

			setUpLights(pipeline);

			if (programPreferences != null && programPreferences.showPerspectiveGrid()) {
			}

			if (renderTextures()) {
				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
			}
			geosetRenderThing.render(pipeline);


			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);



			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			if (showNormals) {
				ShaderPipeline normPipeline = shaderManager.getOrCreateNormPipeline();
				normPipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
//				normPipeline.glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
//				normPipeline.glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
				normPipeline.glViewport(0, 0, (int) (getWidth()), (int) (getHeight()));
				normPipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
				normPipeline.glMatrixMode(GL11.GL_PROJECTION);
				normPipeline.glLoadIdentity();
				normPipeline.glMatrixMode(GL11.GL_MODELVIEW);
				normPipeline.glLoadIdentity();
				normPipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());
				geosetRenderThing.drawNormals(normPipeline);
			}
			if (show3dVerts) {
				ShaderPipeline vertPipeline = shaderManager.getOrCreateVertPipeline();
				vertPipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
//				vertPipeline.glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
				vertPipeline.glViewport(0, 0, (int) (getWidth()), (int) (getHeight()));
				vertPipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
				vertPipeline.glMatrixMode(GL11.GL_PROJECTION);
				vertPipeline.glLoadIdentity();
				vertPipeline.glMatrixMode(GL11.GL_MODELVIEW);
				vertPipeline.glLoadIdentity();
				vertPipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());
				geosetRenderThing.drawVerts(vertPipeline);
			}
			if (true) {
				bonePipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
//				bonePipeline.glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
				bonePipeline.glViewport(0, 0, (int) (getWidth()), (int) (getHeight()));
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

				bonePipeline.glBegin(GL11.GL_POINTS);
//			pipeline.glColor3f(1f, 1f, 3f);

				Vec4 colorHeap = new Vec4(0f, .0f, 1f, 1f);
				// if( wireframe.isSelected() )
				for (IdObject v : modelView.getVisibleIdObjects()) {
					if(modelView.shouldRender(v)){
						RenderNode2 renderNode = renderModel.getRenderNode(v);

						bonePipeline.addVert(renderNode.getPivot(), Vec3.Z_AXIS, colorHeap, Vec2.ORIGIN, colorHeap, Vec3.ZERO);
					}
				}
				bonePipeline.glEnd();
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

	private void paintSelectionBox() {
//		CubePainter.paintCameraLookAt(cameraHandler);
		if (mouseAdapter.isSelecting()) {
			ShaderPipeline selectionPipeline = shaderManager.getOrCreateSelectionPipeline();
			selectionPipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			selectionPipeline.glViewport(0, 0, (int) (getWidth()), (int) (getHeight()));
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

				selectionPipeline.glBegin(GL11.GL_LINES);

				selectionPipeline.addVert(mouseAdapter.getStart(), Vec3.Z_AXIS, new Vec4(), new Vec2(), new Vec4(1, 0,0,1), Vec3.ZERO);
				selectionPipeline.addVert(mouseAdapter.getEnd(), Vec3.Z_AXIS, new Vec4(), new Vec2(), new Vec4(1, 0,0,1), Vec3.ZERO);

				selectionPipeline.glEnd();
			}
//			CubePainter.paintRekt(mouseAdapter.getStartPGeo(), mouseAdapter.getEndPGeo1(), mouseAdapter.getEndPGeo2(), mouseAdapter.getEndPGeo3(), cameraHandler);
			System.out.println("is selecting!");
		}
	}
	private void paintGrid() {
//		CubePainter.paintCameraLookAt(cameraHandler);
		if (programPreferences.showPerspectiveGrid()) {
			ShaderPipeline pipeline = shaderManager.getOrCreateGridPipeline();
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			pipeline.glViewport(0, 0, (int) (getWidth()), (int) (getHeight()));
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

				gridPainter2.paintGrid(pipeline);
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
		if (renderTextures()) {
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


	private void setRenderColor(SimpleDiffuseShaderPipeline pipeline, GeosetAnim geosetAnim, float geosetAnimVisibility, Layer layer) {
		if (renderModel.getTimeEnvironment().getCurrentSequence() != null) {
			float layerVisibility = layer.getRenderVisibility(renderModel.getTimeEnvironment());
			float alphaValue = geosetAnimVisibility * layerVisibility;
			Vec3 renderColor = null;
			if (geosetAnim != null) {
				renderColor = geosetAnim.getRenderColor(renderModel.getTimeEnvironment());

			}
			if (renderColor != null) {
				if (layer.getFilterMode() == FilterMode.ADDITIVE) {
					pipeline.glColor4f(renderColor.z * alphaValue, renderColor.y * alphaValue, renderColor.x * alphaValue, alphaValue);
				} else {
					pipeline.glColor4f(renderColor.z, renderColor.y, renderColor.x, alphaValue);
				}
			} else {
				pipeline.glColor4f(1f, 1f, 1f, alphaValue);
			}

		} else {
			pipeline.glColor4f(1f, 1f, 1f, 1f);
		}
	}

//	public void bindLayer(final ParticleEmitter2 particle2, final Bitmap tex, final Integer texture) {
//		bindTexture(tex, texture);
//		switch (particle2.getFilterMode()) {
//			case BLEND:
//				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
//				GL11.glEnable(GL11.GL_BLEND);
//				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//				break;
//			case ADDITIVE:
//				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
//				GL11.glEnable(GL11.GL_BLEND);
//				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//				break;
//			case ALPHAKEY:
//				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
//				GL11.glEnable(GL11.GL_BLEND);
//				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//				break;
//			case MODULATE:
//				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
//				GL11.glEnable(GL11.GL_BLEND);
//				GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
//				break;
//			case MODULATE2X:
//				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
//				GL11.glEnable(GL11.GL_BLEND);
//				GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
//				break;
//		}
//		if (particle2.getUnshaded()) {
//			pipeline.glDisableIfNeeded(GL11.GL_LIGHTING);
//		}
//		else {
//			pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
//		}
//	}

	public static int loadTexture(SimpleDiffuseShaderPipeline pipeline, GPUReadyTexture texture, Bitmap bitmap, boolean alpha, int formatVersion) {
		if (texture == null) {
			return -1;
		}
		ByteBuffer buffer = texture.getBuffer();
		// You now have a ByteBuffer filled with the color data of each pixel.
		// Now just create a texture ID and bind it. Then you can load it using
		// whatever OpenGL method you want, for example:

		int textureID = GL11.glGenTextures(); // Generate texture ID
		pipeline.prepareToBindTexture();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); // Bind texture ID

		// Setup wrap mode
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, bitmap.isWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, bitmap.isWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);

		// Setup texture scaling filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		// Send texel data to OpenGL
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, texture.getWidth(), texture.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		// Return the texture ID so we can bind it later again
		return textureID;
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