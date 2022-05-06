package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.CameraManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.GridPainter2;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.application.viewer.ViewportHelpers;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.function.Consumer;

public class BufferFiller {
	private ModelView modelView;
	private RenderModel renderModel;
	private TimeEnvironmentImpl renderEnv;
	private Timer paintTimer;
//	private final ArrayList<ViewportCanvas> canvasList = new ArrayList<>();

	private boolean texLoaded = false;

	private static final ShaderManager shaderManager = new ShaderManager();

	Class<? extends Throwable> lastThrownErrorClass;
	private ProgramPreferences programPreferences;

	private long lastExceptionTimeMillis = 0;

	private float backgroundRed, backgroundBlue, backgroundGreen;

	private int levelOfDetail;

	GeosetBufferFiller geosetBufferFiller;
	GridPainter2 gridPainter2;

	private TextureThing textureThing;

	private float xRatio;
	private float yRatio;

	boolean wantReload = false;
	boolean wantReloadAll = false;

	boolean doUpdate = true;

	int popupCount = 0;

	Color bgColor;

	public BufferFiller(){
		this.programPreferences = ProgramGlobals.getPrefs();

		bgColor = programPreferences == null ? new Color(80, 80, 80) : programPreferences.getPerspectiveBackgroundColor();


		geosetBufferFiller = new GeosetBufferFiller();
		gridPainter2 = new GridPainter2();


//		paintTimer = new Timer(16, e -> {
//	//		paintTimer = new Timer(200, e -> {
////			repaint();
////			if (!isShowing()) {
////				paintTimer.stop();
////			}
//
//		});
//		paintTimer.start();
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
				renderEnv.setSequence(ViewportHelpers.findDefaultAnimation(model));
//				for(ViewportCanvas viewportCanvas : canvasList) {
//					viewportCanvas.getCameraManager().loadDefaultCameraFor(ViewportHelpers.getBoundsRadius(renderEnv, model.getExtents()));
//				}
//				cameraManager.setModelInstance(null, null);
			}

			renderModel.refreshFromEditor(textureThing);
			reloadAllTextures();
		} else {
			textureThing = null;
			renderEnv = null;
			modelView = null;
		}
//		cameraManager.viewport(0, 0, getWidth() * xRatio, getHeight() * yRatio);
//		cameraManager.setOrtho(true);
		geosetBufferFiller.setModel(renderModel, modelView, textureThing);
	}

//	public void updateBuffers(){
//		ShaderPipeline pipeline = shaderManager.getOrCreatePipeline();
//		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
//			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
//			return;
//		}
//		reloadIfNeeded();
//		updateRenderModel();
//		boolean showNormals = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isShowNormals());
//		boolean show3dVerts = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isShow3dVerts());
//		boolean renderTextures = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isRenderTextures());
//		fillBuffers(showNormals, show3dVerts, renderTextures, pipeline);
////		update();
//	}

	private boolean first_run = true;
	public void updateBuffers(boolean showNormals, boolean show3dVerts, boolean renderTextures){
		doUpdate = true;
		this.showNormals = showNormals;
		this.show3dVerts = show3dVerts;
		this.renderTextures = renderTextures;
//		if ( first_run ) {
//			first_run = false;
//			initGL();
//		}
//		ShaderPipeline pipeline = shaderManager.getOrCreatePipeline();
//		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
//			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
//			return;
//		}
//		reloadIfNeeded();
//		updateRenderModel();
////		boolean showNormals = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isShowNormals());
////		boolean show3dVerts = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isShow3dVerts());
////		boolean renderTextures = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isRenderTextures());
//		fillBuffers(showNormals, show3dVerts, renderTextures, pipeline);
////		update();
	}
	boolean showNormals;
	boolean show3dVerts;
	boolean renderTextures;
	private void runUpdate(){
		if(doUpdate){
//			System.out.println("updating");
			doUpdate = false;
//			if ( first_run ) {
//				first_run = false;
//				initGL();
//			}
			ShaderPipeline pipeline = shaderManager.getOrCreatePipeline();
			if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
				System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
				return;
			}
			reloadIfNeeded();
			updateRenderModel();
//		boolean showNormals = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isShowNormals());
//		boolean show3dVerts = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isShow3dVerts());
//		boolean renderTextures = canvasList.stream().anyMatch(canvas -> canvas.getViewportSettings().isRenderTextures());
			fillBuffers(showNormals, show3dVerts, renderTextures, pipeline);
		}
	}


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
//	public void paintGL(final boolean autoRepainting) {
////		setSize(getParent().getSize());
////		cameraManager.updateCamera();
//		ShaderPipeline pipeline = shaderManager.getOrCreatePipeline();
//		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
//			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
//			return;
//		}
//		try {
//
//			for(ViewportCanvas viewportCanvas : canvasList) {
//				paintCanvas(viewportCanvas.getCameraManager(), viewportCanvas.getViewportSettings(), viewportCanvas, autoRepainting, pipeline);
//			}
////			if(bufferConsumer != null){
//////				glfwHideWindow(GLFW_VISIBLE, GLFW_FALSE)
////				bufferConsumer.accept(paintGL2());
////				bufferConsumer = null;
////			}
//		}
//		catch (final Throwable e) {
//			e.printStackTrace();
//			lastExceptionTimeMillis = System.currentTimeMillis();
//			if ((lastThrownErrorClass == null) || (lastThrownErrorClass != e.getClass())) {
//				lastThrownErrorClass = e.getClass();
//				popupCount++;
//				if (popupCount < 10) {
//					ExceptionPopup.display(e);
//				}
//			}
//			throw new RuntimeException(e);
//		}
//	}
	public void paintCanvas(ViewportCanvas viewportCanvas, boolean autoRepainting) {
		runUpdate();
//		System.out.println("painting canvas");
//		setSize(getParent().getSize());
//		cameraManager.updateCamera();
		ShaderPipeline pipeline = shaderManager.getOrCreatePipeline();
		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
			return;
		}
		try {

			paintCanvas(viewportCanvas.getCameraManager(), viewportCanvas.getViewportSettings(), viewportCanvas, autoRepainting, pipeline);

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

	private void fillBuffers(boolean showNormals, boolean show3dVerts, boolean renderTextures, ShaderPipeline pipeline){

		if(renderModel != null){
			geosetBufferFiller.fillBuffer(pipeline, renderTextures);

			RendererThing1.fillNodeBuffer(shaderManager.getOrCreateBoneMarkerShaderPipeline(), modelView, renderModel);

			if (showNormals) {
				geosetBufferFiller.fillNormalsBuffer(shaderManager.getOrCreateNormPipeline());
			}
			if (show3dVerts) {
				geosetBufferFiller.fillVertsBuffer(shaderManager.getOrCreateVertPipeline());
			}

			if (programPreferences.showPerspectiveGrid()) {
				gridPainter2.fillGridBuffer(shaderManager.getOrCreateGridPipeline());
			}

//			if (programPreferences != null && programPreferences.getRenderParticles()) {
//				renderParticles();
//			}
		}
	}

	public void paintCanvas(CameraManager cameraManager, ViewportSettings viewportSettings, ViewportCanvas viewportCanvas, boolean autoRepainting, ShaderPipeline pipeline) {
		if (programPreferences != null && viewportSettings.isWireFrame()) {
			pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		} else {
			pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
		cameraManager.updateCamera();

		pipeline.glViewport(viewportCanvas.getWidth(), viewportCanvas.getHeight());
		GL11.glViewport(0, 0, viewportCanvas.getWidth(), viewportCanvas.getHeight());

		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDepthMask(true);
		pipeline.glEnableIfNeeded(GL11.GL_COLOR_MATERIAL);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHT0);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHT1);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		if (viewportSettings.isRenderTextures()) {
			pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
		} else {
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);

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

		if (viewportSettings.isRenderTextures()) {
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
		} else {
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		}


		if(renderModel != null){
			pipeline.doRender(GL11.GL_TRIANGLES);
			RendererThing1.renderNodes(cameraManager, viewportCanvas, shaderManager.getOrCreateBoneMarkerShaderPipeline());


			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);


			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			if (viewportSettings.isShowNormals()) {
				RendererThing1.renderNormals(cameraManager, viewportCanvas, shaderManager.getOrCreateNormPipeline(), geosetBufferFiller);
			}
			if (viewportSettings.isShow3dVerts()) {
				RendererThing1.render3DVerts(cameraManager, viewportCanvas, shaderManager.getOrCreateVertPipeline());
			}


			RendererThing1.fillSelectionBoxBuffer(viewportCanvas.getMouseAdapter(), shaderManager.getOrCreateSelectionPipeline());
			RendererThing1.paintSelectionBox(cameraManager, viewportCanvas, shaderManager.getOrCreateSelectionPipeline());

			if (programPreferences.showPerspectiveGrid()) {
				RendererThing1.paintGrid(cameraManager, viewportCanvas, shaderManager.getOrCreateGridPipeline());
			}

//			if (programPreferences != null && programPreferences.getRenderParticles()) {
//				renderParticles();
//			}
		}
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);

//		if (autoRepainting) {
//
//			paintCanvas();
//		}
	}
	private void paintCanvas1(CameraManager cameraManager, ViewportSettings viewportSettings, ViewportCanvas viewportCanvas, boolean autoRepainting, ShaderPipeline pipeline) {
		if (programPreferences != null && viewportSettings.isWireFrame()) {
			pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		} else {
			pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
//			pipeline.glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
//			pipeline.glViewport(0, 0, viewportCanvas.getWidth(), viewportCanvas.getHeight());
		pipeline.glViewport(viewportCanvas.getWidth(), viewportCanvas.getHeight());
		GL11.glViewport(0, 0, viewportCanvas.getWidth(), viewportCanvas.getHeight());
//			pipeline.glViewport(0, 0, viewportCanvas.getWidth(), viewportCanvas.getHeight());
		pipeline.glViewport(viewportCanvas.getWidth(), viewportCanvas.getHeight());
		GL11.glViewport(0, 0, viewportCanvas.getWidth(), viewportCanvas.getHeight());
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDepthMask(true);
		pipeline.glEnableIfNeeded(GL11.GL_COLOR_MATERIAL);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHT0);
		pipeline.glEnableIfNeeded(GL11.GL_LIGHT1);
		pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		if (viewportSettings.isRenderTextures()) {
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

		if (viewportSettings.isRenderTextures()) {
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
		} else {
			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		}


		if(renderModel != null){
			geosetBufferFiller.fillBuffer(pipeline, viewportSettings.isRenderTextures());
			pipeline.doRender(GL11.GL_TRIANGLES);

			RendererThing1.fillNodeBuffer(shaderManager.getOrCreateBoneMarkerShaderPipeline(), modelView, renderModel);
			RendererThing1.renderNodes(cameraManager, viewportCanvas, shaderManager.getOrCreateBoneMarkerShaderPipeline());


			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);


			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
			if (viewportSettings.isShowNormals()) {
				geosetBufferFiller.fillNormalsBuffer(shaderManager.getOrCreateNormPipeline());
				RendererThing1.renderNormals(cameraManager, viewportCanvas, shaderManager.getOrCreateNormPipeline(), geosetBufferFiller);
			}
			if (viewportSettings.isShow3dVerts()) {
				geosetBufferFiller.fillVertsBuffer(shaderManager.getOrCreateVertPipeline());
				RendererThing1.render3DVerts(cameraManager, viewportCanvas, shaderManager.getOrCreateVertPipeline());
			}

			RendererThing1.fillSelectionBoxBuffer(viewportCanvas.getMouseAdapter(), shaderManager.getOrCreateSelectionPipeline());
			RendererThing1.paintSelectionBox(cameraManager, viewportCanvas, shaderManager.getOrCreateSelectionPipeline());

			if (programPreferences.showPerspectiveGrid()) {
				gridPainter2.fillGridBuffer(shaderManager.getOrCreateGridPipeline());
				RendererThing1.paintGrid(cameraManager, viewportCanvas, shaderManager.getOrCreateGridPipeline());
			}

//			if (programPreferences != null && programPreferences.getRenderParticles()) {
//				renderParticles();
//			}
		}
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		if (autoRepainting) {

			paintAndUpdate();
		}
	}

	Consumer<ByteBuffer> bufferConsumer;
	public void setPixelBufferListener(Consumer<ByteBuffer> bufferConsumer){
		this.bufferConsumer = bufferConsumer;
	}
//	public ByteBuffer paintGL2() {
//		setSize(getParent().getSize());
//		cameraManager.updateCamera();
//		ShaderPipeline pipeline = shaderManager.getOrCreatePipeline();
//		if ((System.currentTimeMillis() - lastExceptionTimeMillis) < 5000) {
//			System.err.println("AnimatedPerspectiveViewport omitting frames due to avoid Exception log spam");
//			return null;
//		}
//		reloadIfNeeded();
//		try {
//			updateRenderModel();
//
//			if (programPreferences != null && wireFrame) {
//				pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
//			} else {
//				pipeline.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
//			}
////			pipeline.glViewport(0, 0, (int) (getWidth() * xRatio), (int) (getHeight() * yRatio));
//			pipeline.glViewport(0, 0, getWidth(), getHeight());
//			GL11.glEnable(GL11.GL_DEPTH_TEST);
//
//			GL11.glDepthFunc(GL11.GL_LEQUAL);
//			GL11.glDepthMask(true);
//			pipeline.glEnableIfNeeded(GL11.GL_COLOR_MATERIAL);
//			pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
//			pipeline.glEnableIfNeeded(GL11.GL_LIGHT0);
//			pipeline.glEnableIfNeeded(GL11.GL_LIGHT1);
//			pipeline.glEnableIfNeeded(GL11.GL_NORMALIZE);
//			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
//			if (renderTextures) {
//				pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
//			}
////			GL11.glClearColor(backgroundRed, backgroundGreen, backgroundBlue, autoRepainting ? 1.0f : 0.0f);
//			GL11.glClearColor(backgroundRed, backgroundGreen, backgroundBlue, 0.0f);
//
//			pipeline.glMatrixMode(GL11.GL_PROJECTION);
//			pipeline.glLoadIdentity();
//
//			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//			pipeline.glMatrixMode(GL11.GL_MODELVIEW);
//			pipeline.glLoadIdentity();
//
//			pipeline.glSetProjectionMatrix(cameraManager.getViewProjectionMatrix());
//
//			setUpLights(pipeline);
//
//			if (renderTextures) {
//				GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
//				pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
//			} else {
//				pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
//			}
//			geosetRenderThing.render(pipeline, renderTextures);
//			GL11.glDisable(GL11.GL_TEXTURE_2D);
//			renderNodes();
//
//
//			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
//
//
//
//			pipeline.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
//			if (showNormals) {
//				renderNormals();
//			}
//			if (show3dVerts) {
//				render3DVerts();
//			}
//
//			paintSelectionBox();
//			paintGrid();
//
////			if (programPreferences != null && programPreferences.getRenderParticles()) {
////				renderParticles();
////			}
//			ByteBuffer pixels = ByteBuffer.allocateDirect(getWidth() * getHeight() * 4);
//
//			GL11.glReadPixels(0, 0, getWidth(), getHeight(), GL11.GL_RGBA, GL_UNSIGNED_BYTE, pixels);
//
//			GL11.glDepthMask(false);
//			GL11.glEnable(GL11.GL_BLEND);
//			GL11.glDisable(GL11.GL_CULL_FACE);
//			GL11.glEnable(GL11.GL_DEPTH_TEST);
//
//			return pixels;
//		} catch (final Throwable e) {
//			e.printStackTrace();
//			lastExceptionTimeMillis = System.currentTimeMillis();
//			if ((lastThrownErrorClass == null) || (lastThrownErrorClass != e.getClass())) {
//				lastThrownErrorClass = e.getClass();
//				popupCount++;
//				if (popupCount < 10) {
//					ExceptionPopup.display(e);
//				}
//			}
//			throw new RuntimeException(e);
//		}
//	}


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

	private void paintAndUpdate() {
//		try {
//			swapBuffers();
//		} catch (LWJGLException e) {
//			e.printStackTrace();
//		}
//		if (isShowing() && !paintTimer.isRunning()) {
//			paintTimer.restart();
//		} else if (!isShowing() && paintTimer.isRunning()) {
//			paintTimer.stop();
//		}
	}
	private void paintCanvas() {
//		try {
//			swapBuffers();
//		} catch (LWJGLException e) {
//			e.printStackTrace();
//		}
	}
//	private void update() {
//		if (isShowing() && !paintTimer.isRunning()) {
//			paintTimer.restart();
//		} else if (!isShowing() && paintTimer.isRunning()) {
//			paintTimer.stop();
//		}
//	}
//
//	private boolean isShowing(){
//		return true;
//	}

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
}
